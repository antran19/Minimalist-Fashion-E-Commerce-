package com.uminimalist.store.service;

import com.uminimalist.store.entity.ProductVariant;
import com.uminimalist.store.model.CartItemView;
import com.uminimalist.store.model.CartView;
import com.uminimalist.store.repository.ProductVariantRepository;
import com.uminimalist.store.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ShoppingCartService {
    private static final String CART_SESSION_KEY = "uMinimalistCart";

    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public ShoppingCartService(ProductVariantRepository productVariantRepository,
                               UserRepository userRepository,
                               JdbcTemplate jdbcTemplate) {
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureCartTable() {
        jdbcTemplate.execute("""
                IF OBJECT_ID(N'dbo.customer_cart_items', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.customer_cart_items (
                        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        sku NVARCHAR(80) NOT NULL,
                        quantity INT NOT NULL,
                        reserved_until DATETIME2 NULL,
                        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                        updated_at DATETIME2 NULL
                    );
                END
                """);
        addCartColumnIfMissing("reserved_until", "DATETIME2 NULL");
        jdbcTemplate.execute("""
                IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ux_customer_cart_items_user_sku'
                    AND object_id = OBJECT_ID(N'dbo.customer_cart_items'))
                BEGIN
                    CREATE UNIQUE INDEX ux_customer_cart_items_user_sku ON dbo.customer_cart_items(user_id, sku);
                END
                """);
    }

    @Transactional(readOnly = false)
    public void addItem(HttpSession session, String productSlug, String color, String size, int quantity) {
        addItem(session, null, productSlug, color, size, quantity);
    }

    @Transactional(readOnly = false)
    public void addItem(HttpSession session, String customerEmail, String productSlug, String color, String size, int quantity) {
        ProductVariant variant = productVariantRepository
                .findFirstByProductSlugAndColorIgnoreCaseAndSizeIgnoreCaseAndActiveTrueAndProductActiveTrue(productSlug, color, size)
                .orElseThrow(() -> new IllegalArgumentException("Product variant is not available."));

        int currentQuantity = 0;
        if (hasText(customerEmail)) {
            mergeSessionCartToCustomer(session, customerEmail);
            var userIdOpt = userId(customerEmail);
            if (userIdOpt.isEmpty()) throw new IllegalArgumentException("Customer account not found.");
            Long userId = userIdOpt.get();
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COALESCE(MAX(quantity), 0)
                    FROM dbo.customer_cart_items
                    WHERE user_id = ? AND sku = ?
                    """, Integer.class, userId, variant.getSku());
            currentQuantity = count == null ? 0 : count;
        } else {
            Map<String, Integer> cart = mutableCart(session);
            currentQuantity = cart.getOrDefault(variant.getSku(), 0);
        }

        validateQuantityAndStock(variant, currentQuantity, quantity);

        int nextQuantity = currentQuantity + quantity;
        if (hasText(customerEmail)) {
            saveCustomerCartQuantity(customerEmail, variant.getSku(), nextQuantity);
        } else {
            mutableCart(session).put(variant.getSku(), nextQuantity);
        }
    }

    @Transactional(readOnly = false)
    public void addSku(HttpSession session, String customerEmail, String sku, int quantity) {
        ProductVariant variant = productVariantRepository.findBySkuInAndActiveTrueAndProductActiveTrue(List.of(sku))
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product variant is not available."));

        int currentQuantity = 0;
        if (hasText(customerEmail)) {
            mergeSessionCartToCustomer(session, customerEmail);
            var userIdOpt = userId(customerEmail);
            if (userIdOpt.isEmpty()) throw new IllegalArgumentException("Customer account not found.");
            Long userId = userIdOpt.get();
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COALESCE(MAX(quantity), 0)
                    FROM dbo.customer_cart_items
                    WHERE user_id = ? AND sku = ?
                    """, Integer.class, userId, variant.getSku());
            currentQuantity = count == null ? 0 : count;
        } else {
            Map<String, Integer> cart = mutableCart(session);
            currentQuantity = cart.getOrDefault(sku, 0);
        }

        validateQuantityAndStock(variant, currentQuantity, quantity);

        int nextQuantity = currentQuantity + quantity;
        if (hasText(customerEmail)) {
            saveCustomerCartQuantity(customerEmail, variant.getSku(), nextQuantity);
        } else {
            mutableCart(session).put(sku, nextQuantity);
        }
    }

    public void updateItem(HttpSession session, String sku, int quantity) {
        updateItem(session, null, sku, quantity);
    }

    @Transactional(readOnly = false)
    public void updateItem(HttpSession session, String customerEmail, String sku, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1. Use the Remove button to delete items.");
        }

        ProductVariant variant = productVariantRepository.findBySkuIgnoreCase(sku)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product variant."));

        if (!variant.isActive() || !variant.getProduct().isActive()) {
            throw new IllegalArgumentException("Product variant is no longer available.");
        }

        if (variant.getStockQuantity() <= 0) {
            throw new IllegalArgumentException("Product variant is out of stock.");
        }

        if (quantity > variant.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity (" + quantity + ") exceeds available stock (" + variant.getStockQuantity() + ").");
        }

        if (hasText(customerEmail)) {
            mergeSessionCartToCustomer(session, customerEmail);
            var userIdOpt = userId(customerEmail);
            if (userIdOpt.isEmpty()) throw new IllegalArgumentException("Customer account not found.");
            Long userId = userIdOpt.get();
            Integer count = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM dbo.customer_cart_items
                    WHERE user_id = ? AND sku = ?
                    """, Integer.class, userId, variant.getSku());
            if (count == null || count == 0) {
                throw new IllegalArgumentException("Item not found in your cart.");
            }

            jdbcTemplate.update("""
                    UPDATE dbo.customer_cart_items
                    SET quantity = ?, updated_at = SYSUTCDATETIME()
                    WHERE user_id = ? AND sku = ?
                    """, quantity, userId, variant.getSku());
        } else {
            Map<String, Integer> cart = mutableCart(session);
            if (!cart.containsKey(sku)) {
                throw new IllegalArgumentException("Item not found in your cart.");
            }
            cart.put(sku, quantity);
        }
    }

    public void removeItem(HttpSession session, String sku) {
        removeItem(session, null, sku);
    }

    @Transactional(readOnly = false)
    public void removeItem(HttpSession session, String customerEmail, String sku) {
        if (hasText(customerEmail)) {
            mergeSessionCartToCustomer(session, customerEmail);
            var userIdOpt = userId(customerEmail);
            if (userIdOpt.isEmpty()) return;
            Long userId = userIdOpt.get();
            jdbcTemplate.update("DELETE FROM dbo.customer_cart_items WHERE user_id = ? AND sku = ?", userId, sku);
            return;
        }
        mutableCart(session).remove(sku);
    }

    public CartView getCart(HttpSession session) {
        return getCart(session, null);
    }

    @Transactional(readOnly = false)
    public CartView getCart(HttpSession session, String customerEmail) {
        Map<String, Integer> cart;
        if (hasText(customerEmail)) {
            mergeSessionCartToCustomer(session, customerEmail);
            cart = readCustomerCart(customerEmail);
        } else {
            cart = readCart(session);
        }
        if (cart.isEmpty()) {
            return new CartView(List.of(), 0, currencyFormat.format(0), List.of());
        }

        Set<String> searchSkus = new HashSet<>();
        for (String s : cart.keySet()) {
            if (hasText(s)) {
                searchSkus.add(s.trim());
                searchSkus.add(s.trim().toUpperCase(Locale.ROOT));
                searchSkus.add(s.trim().toLowerCase(Locale.ROOT));
            }
        }

        Map<String, ProductVariant> activeVariantsBySku = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (ProductVariant v : productVariantRepository.findBySkuInAndActiveTrueAndProductActiveTrue(searchSkus)) {
            activeVariantsBySku.put(v.getSku(), v);
        }

        List<String> warnings = new java.util.ArrayList<>();
        List<String> removedSkus = new java.util.ArrayList<>();

        for (String sku : cart.keySet()) {
            if (!activeVariantsBySku.containsKey(sku)) {
                removedSkus.add(sku);
            }
        }

        if (!removedSkus.isEmpty()) {
            warnings.add("One or more items in your cart are no longer available and were removed.");
            for (String sku : removedSkus) {
                removeItem(session, customerEmail, sku);
                cart.remove(sku);
            }
        }

        if (cart.isEmpty()) {
            return new CartView(List.of(), 0, currencyFormat.format(0), warnings);
        }

        List<CartItemView> items = new java.util.ArrayList<>();
        for (Map.Entry<String, Integer> entry : cart.entrySet()) {
            ProductVariant variant = activeVariantsBySku.get(entry.getKey());
            if (variant == null) continue;

            int cartQty = entry.getValue();
            int stock = variant.getStockQuantity();

            if (stock <= 0) {
                warnings.add("Item '" + variant.getProduct().getName() + " (" + variant.getColor() + ", " + variant.getSize() + ")' is currently out of stock.");
            } else if (cartQty > stock) {
                warnings.add("Item '" + variant.getProduct().getName() + " (" + variant.getColor() + ", " + variant.getSize() + ")' quantity (" + cartQty + ") exceeds available stock (" + stock + "). Please update quantity.");
            }

            items.add(toCartItemView(variant, cartQty));
        }

        double subtotal = items.stream()
                .filter(item -> !item.outOfStock() && !item.stockExceeded())
                .mapToDouble(item -> item.unitPrice() * item.quantity())
                .sum();
        int itemCount = items.stream()
                .filter(item -> !item.outOfStock() && !item.stockExceeded())
                .mapToInt(CartItemView::quantity)
                .sum();

        return new CartView(items, itemCount, currencyFormat.format(subtotal), warnings);
    }

    public CartView getCartFiltered(HttpSession session, String customerEmail, List<String> filterSkus) {
        CartView fullCart = getCart(session, customerEmail);
        if (filterSkus == null || filterSkus.isEmpty()) {
            return fullCart;
        }
        Set<String> upperSkus = filterSkus.stream()
                .filter(this::hasText)
                .map(s -> s.trim().toUpperCase(Locale.ROOT))
                .collect(Collectors.toSet());
        List<CartItemView> filteredItems = fullCart.items().stream()
                .filter(item -> upperSkus.contains(item.sku().toUpperCase(Locale.ROOT)))
                .toList();

        if (filteredItems.isEmpty()) {
            return new CartView(List.of(), 0, currencyFormat.format(0));
        }

        double subtotal = filteredItems.stream()
                .filter(item -> !item.outOfStock() && !item.stockExceeded())
                .mapToDouble(item -> item.unitPrice() * item.quantity())
                .sum();
        int itemCount = filteredItems.stream()
                .filter(item -> !item.outOfStock() && !item.stockExceeded())
                .mapToInt(CartItemView::quantity)
                .sum();

        return new CartView(filteredItems, itemCount, currencyFormat.format(subtotal));
    }

    public void validateAndPrepareCheckoutSkus(HttpSession session, String customerEmail, List<String> selectedSkus) {
        if (selectedSkus == null || selectedSkus.isEmpty()) {
            throw new IllegalArgumentException("Please select at least one item to checkout.");
        }

        Set<String> uniqueSkus = new HashSet<>();
        for (String sku : selectedSkus) {
            if (sku == null || sku.isBlank()) {
                throw new IllegalArgumentException("One or more selected cart items are invalid or no longer available.");
            }
            if (!uniqueSkus.add(sku.trim())) {
                throw new IllegalArgumentException("Duplicate items selected for checkout.");
            }
        }

        Map<String, Integer> cartMap;
        if (hasText(customerEmail)) {
            cartMap = readCustomerCart(customerEmail);
        } else {
            cartMap = readCart(session);
        }

        Map<String, Integer> caseInsensitiveCartMap = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        caseInsensitiveCartMap.putAll(cartMap);

        for (String sku : uniqueSkus) {
            if (!caseInsensitiveCartMap.containsKey(sku)) {
                throw new IllegalArgumentException("One or more selected cart items are invalid or no longer available.");
            }
        }

        Set<String> searchSkus = new HashSet<>();
        for (String s : uniqueSkus) {
            searchSkus.add(s);
            searchSkus.add(s.toUpperCase(Locale.ROOT));
            searchSkus.add(s.toLowerCase(Locale.ROOT));
        }

        Map<String, ProductVariant> variantsBySku = new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (ProductVariant v : productVariantRepository.findBySkuInAndActiveTrueAndProductActiveTrue(searchSkus)) {
            variantsBySku.put(v.getSku(), v);
        }

        for (String sku : uniqueSkus) {
            ProductVariant variant = variantsBySku.get(sku);
            if (variant == null || !variant.isActive() || !variant.getProduct().isActive()) {
                throw new IllegalArgumentException("One or more selected cart items are invalid or no longer available.");
            }
            int cartQty = caseInsensitiveCartMap.get(sku);
            if (variant.getStockQuantity() <= 0) {
                throw new IllegalArgumentException("Item '" + variant.getProduct().getName() + "' is out of stock.");
            }
            if (cartQty > variant.getStockQuantity()) {
                throw new IllegalArgumentException("Item '" + variant.getProduct().getName() + "' quantity exceeds available stock (" + variant.getStockQuantity() + ").");
            }
        }
    }

    @Transactional(readOnly = false)
    public void removeItems(HttpSession session, String customerEmail, List<String> skus) {
        if (skus == null || skus.isEmpty()) return;
        for (String sku : skus) {
            removeItem(session, customerEmail, sku);
        }
    }

    public int getItemCount(HttpSession session) {
        return getItemCount(session, null);
    }

    public int getItemCount(HttpSession session, String customerEmail) {
        if (hasText(customerEmail)) {
            return readCustomerCart(customerEmail).values()
                    .stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }
        return readCart(session).values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public void clearCart(HttpSession session) {
        clearCart(session, null);
    }

    @Transactional(readOnly = false)
    public void clearCart(HttpSession session, String customerEmail) {
        if (hasText(customerEmail)) {
            userId(customerEmail).ifPresent(uid ->
                jdbcTemplate.update("DELETE FROM dbo.customer_cart_items WHERE user_id = ?", uid));
        }
        session.removeAttribute(CART_SESSION_KEY);
    }

    @Transactional(readOnly = false)
    public void mergeSessionCartToCustomerAfterRegister(HttpSession session, String customerEmail) {
        if (!hasText(customerEmail)) return;
        mergeSessionCartToCustomer(session, customerEmail);
    }



    private void addCartColumnIfMissing(String columnName, String definition) {
        jdbcTemplate.execute("""
                IF COL_LENGTH('dbo.customer_cart_items', '%s') IS NULL
                BEGIN
                    ALTER TABLE dbo.customer_cart_items ADD %s %s;
                END
                """.formatted(columnName, columnName, definition));
    }

    private CartItemView toCartItemView(ProductVariant variant, int quantity) {
        double unitPrice = variant.getProduct().getSalePrice().doubleValue();
        double lineTotal = unitPrice * quantity;
        boolean outOfStock = variant.getStockQuantity() <= 0;
        boolean stockExceeded = quantity > variant.getStockQuantity();

        // Prefer variant-level Cloudinary image, fall back to hardcoded product path
        String resolvedImagePath;
        if (variant.getImageUrl() != null && !variant.getImageUrl().isBlank()) {
            resolvedImagePath = variant.getImageUrl();
        } else {
            resolvedImagePath = imagePath(variant.getProduct().getSlug());
        }

        return new CartItemView(
                variant.getSku(),
                variant.getProduct().getSlug(),
                variant.getProduct().getName(),
                variant.getProduct().getProductType(),
                variant.getColor(),
                variant.getSize(),
                resolvedImagePath,
                variant.getProduct().getCropClass(),
                quantity,
                variant.getStockQuantity(),
                unitPrice,
                currencyFormat.format(unitPrice),
                currencyFormat.format(lineTotal),
                outOfStock,
                stockExceeded
        );
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> readCart(HttpSession session) {
        Object value = session.getAttribute(CART_SESSION_KEY);
        if (value instanceof Map<?, ?> cart) {
            return (Map<String, Integer>) cart;
        }
        return Map.of();
    }

    private Map<String, Integer> mutableCart(HttpSession session) {
        Map<String, Integer> existingCart = new LinkedHashMap<>(readCart(session));
        session.setAttribute(CART_SESSION_KEY, existingCart);
        return existingCart;
    }

    private void mergeSessionCartToCustomer(HttpSession session, String customerEmail) {
        Map<String, Integer> sessionCart = readCart(session);
        if (sessionCart.isEmpty()) {
            return;
        }

        Map<String, ProductVariant> variantsBySku = productVariantRepository.findBySkuInAndActiveTrueAndProductActiveTrue(sessionCart.keySet())
                .stream()
                .collect(Collectors.toMap(ProductVariant::getSku, Function.identity()));

        for (Map.Entry<String, Integer> entry : sessionCart.entrySet()) {
            ProductVariant variant = variantsBySku.get(entry.getKey());
            if (variant != null) {
                addSkuToCustomerCart(customerEmail, variant, entry.getValue());
            }
        }
        session.removeAttribute(CART_SESSION_KEY);
    }

    private void validateQuantityAndStock(ProductVariant variant, int currentQuantity, int requestedQuantity) {
        if (requestedQuantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }
        int stock = variant.getStockQuantity();
        if (stock <= 0) {
            throw new IllegalArgumentException("This item is currently out of stock.");
        }
        if (currentQuantity + requestedQuantity > stock) {
            if (currentQuantity == 0) {
                throw new IllegalArgumentException("Only " + stock + " item(s) are available.");
            } else if (currentQuantity >= stock) {
                throw new IllegalArgumentException("The requested quantity exceeds available stock. You already have the maximum available stock (" + stock + ") in your cart.");
            } else {
                int availableMore = stock - currentQuantity;
                throw new IllegalArgumentException("The requested quantity exceeds available stock. Only " + availableMore + " more item(s) can be added (you already have " + currentQuantity + " in your cart).");
            }
        }
    }

    private void saveCustomerCartQuantity(String customerEmail, String sku, int nextQuantity) {
        var userIdOpt = userId(customerEmail);
        if (userIdOpt.isEmpty()) return;
        Long userId = userIdOpt.get();

        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.customer_cart_items
                WHERE user_id = ? AND sku = ?
                """, Integer.class, userId, sku);
        if (count != null && count > 0) {
            jdbcTemplate.update("""
                    UPDATE dbo.customer_cart_items
                    SET quantity = ?, updated_at = SYSUTCDATETIME()
                    WHERE user_id = ? AND sku = ?
                    """, nextQuantity, userId, sku);
        } else {
            jdbcTemplate.update("""
                    INSERT INTO dbo.customer_cart_items (user_id, sku, quantity)
                    VALUES (?, ?, ?)
                    """, userId, sku, nextQuantity);
        }
    }

    private void addSkuToCustomerCart(String customerEmail, ProductVariant variant, int quantity) {
        var userIdOpt = userId(customerEmail);
        if (userIdOpt.isEmpty()) return;
        Long userId = userIdOpt.get();
        int requestedQuantity = Math.max(quantity, 1);
        Integer currentQuantity = jdbcTemplate.queryForObject("""
                SELECT COALESCE(MAX(quantity), 0)
                FROM dbo.customer_cart_items
                WHERE user_id = ? AND sku = ?
                """, Integer.class, userId, variant.getSku());
        int nextQuantity = Math.min((currentQuantity == null ? 0 : currentQuantity) + requestedQuantity, variant.getStockQuantity());
        if (nextQuantity <= 0) {
            return;
        }

        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.customer_cart_items
                WHERE user_id = ? AND sku = ?
                """, Integer.class, userId, variant.getSku());
        if (count != null && count > 0) {
            jdbcTemplate.update("""
                    UPDATE dbo.customer_cart_items
                    SET quantity = ?, updated_at = SYSUTCDATETIME()
                    WHERE user_id = ? AND sku = ?
                    """, nextQuantity, userId, variant.getSku());
        } else {
            jdbcTemplate.update("""
                    INSERT INTO dbo.customer_cart_items (user_id, sku, quantity)
                    VALUES (?, ?, ?)
                    """, userId, variant.getSku(), nextQuantity);
        }
    }

    private void updateCustomerCartItem(String customerEmail, String sku, int quantity) {
        var userIdOpt = userId(customerEmail);
        if (userIdOpt.isEmpty()) return;
        Long userId = userIdOpt.get();
        if (quantity <= 0) {
            jdbcTemplate.update("DELETE FROM dbo.customer_cart_items WHERE user_id = ? AND sku = ?", userId, sku);
            return;
        }

        productVariantRepository.findBySkuInAndActiveTrueAndProductActiveTrue(List.of(sku))
                .stream()
                .findFirst()
                .ifPresent(variant -> {
                    jdbcTemplate.update("""
                        UPDATE dbo.customer_cart_items
                        SET quantity = ?, updated_at = SYSUTCDATETIME()
                        WHERE user_id = ? AND sku = ?
                        """, Math.min(quantity, variant.getStockQuantity()), userId, sku);
                });
    }

    private Map<String, Integer> readCustomerCart(String customerEmail) {
        var userIdOpt = userId(customerEmail);
        if (userIdOpt.isEmpty()) return new LinkedHashMap<>();
        Long userId = userIdOpt.get();
        ensureCartTable();
        return jdbcTemplate.query("""
                        SELECT sku, quantity
                        FROM dbo.customer_cart_items
                        WHERE user_id = ?
                        ORDER BY updated_at DESC, created_at DESC, id DESC
                        """,
                rs -> {
                    Map<String, Integer> cart = new LinkedHashMap<>();
                    while (rs.next()) {
                        cart.put(rs.getString("sku"), rs.getInt("quantity"));
                    }
                    return cart;
                },
                userId);
    }

    private java.util.Optional<Long> userId(String customerEmail) {
        return userRepository.findByEmail(customerEmail)
                .map(u -> u.getId());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String imagePath(String slug) {
        return switch (slug) {
            case "air-cotton-tee" -> "/images/products/air-cotton-tee-cream.png";
            case "light-utility-jacket" -> "/images/products/light-utility-jacket-gray.png";
            case "soft-jersey-tee" -> "/images/products/soft-jersey-tee-brown.png";
            case "everyday-zip-hoodie" -> "/images/products/everyday-zip-hoodie.png";
            case "smart-ankle-pants" -> "/images/products/smart-ankle-pants-black.png";
            case "oxford-shirt" -> "/images/products/oxford-shirt-brown.png";
            case "linen-blend-shirt" -> "/images/products/linen-blend-shirt-cream.png";
            case "utility-tote" -> "/images/products/utility-tote-pink.jpg";
            case "easy-cotton-shorts" -> "/images/products/easy-cotton-shorts-blue.png";
            case "school-day-cardigan" -> "/images/products/school-day-cardigan-black.jpg";
            default -> "/images/product-collage.png";
        };
    }
}
