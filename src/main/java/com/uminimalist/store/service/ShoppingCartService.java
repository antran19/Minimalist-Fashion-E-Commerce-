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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

        if (hasText(customerEmail)) {
            mergeSessionCartToCustomer(session, customerEmail);
            addSkuToCustomerCart(customerEmail, variant, Math.max(quantity, 1));
            return;
        }

        Map<String, Integer> cart = mutableCart(session);
        int requestedQuantity = Math.max(quantity, 1);
        int currentQuantity = cart.getOrDefault(variant.getSku(), 0);
        int nextQuantity = Math.min(currentQuantity + requestedQuantity, variant.getStockQuantity());

        if (nextQuantity > 0) {
            cart.put(variant.getSku(), nextQuantity);
        }
    }

    @Transactional(readOnly = false)
    public void addSku(HttpSession session, String customerEmail, String sku, int quantity) {
        ProductVariant variant = productVariantRepository.findBySkuInAndActiveTrueAndProductActiveTrue(List.of(sku))
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Product variant is not available."));
        if (hasText(customerEmail)) {
            mergeSessionCartToCustomer(session, customerEmail);
            addSkuToCustomerCart(customerEmail, variant, Math.max(quantity, 1));
            return;
        }

        Map<String, Integer> cart = mutableCart(session);
        int nextQuantity = Math.min(cart.getOrDefault(sku, 0) + Math.max(quantity, 1), variant.getStockQuantity());
        if (nextQuantity > 0) {
            cart.put(sku, nextQuantity);
        }
    }

    public void updateItem(HttpSession session, String sku, int quantity) {
        updateItem(session, null, sku, quantity);
    }

    @Transactional(readOnly = false)
    public void updateItem(HttpSession session, String customerEmail, String sku, int quantity) {
        if (hasText(customerEmail)) {
            mergeSessionCartToCustomer(session, customerEmail);
            updateCustomerCartItem(customerEmail, sku, quantity);
            return;
        }

        Map<String, Integer> cart = mutableCart(session);
        if (quantity <= 0) {
            cart.remove(sku);
            return;
        }

        productVariantRepository.findBySkuInAndActiveTrueAndProductActiveTrue(List.of(sku))
                .stream()
                .findFirst()
                .ifPresent(variant -> cart.put(sku, Math.min(quantity, variant.getStockQuantity())));
    }

    public void removeItem(HttpSession session, String sku) {
        removeItem(session, null, sku);
    }

    @Transactional(readOnly = false)
    public void removeItem(HttpSession session, String customerEmail, String sku) {
        if (hasText(customerEmail)) {
            mergeSessionCartToCustomer(session, customerEmail);
            Long userId = userId(customerEmail);
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
            return new CartView(List.of(), 0, currencyFormat.format(0));
        }

        Map<String, ProductVariant> variantsBySku = productVariantRepository.findBySkuInAndActiveTrueAndProductActiveTrue(cart.keySet())
                .stream()
                .collect(Collectors.toMap(ProductVariant::getSku, Function.identity()));

        List<CartItemView> items = cart.entrySet()
                .stream()
                .filter(entry -> variantsBySku.containsKey(entry.getKey()))
                .map(entry -> toCartItemView(variantsBySku.get(entry.getKey()), entry.getValue()))
                .toList();

        double subtotal = items.stream()
                .mapToDouble(item -> item.unitPrice() * item.quantity())
                .sum();
        int itemCount = items.stream()
                .mapToInt(CartItemView::quantity)
                .sum();

        return new CartView(items, itemCount, currencyFormat.format(subtotal));
    }

    public CartView getCartFiltered(HttpSession session, String customerEmail, List<String> filterSkus) {
        CartView fullCart = getCart(session, customerEmail);
        if (filterSkus == null || filterSkus.isEmpty()) {
            return fullCart;
        }
        List<String> upperSkus = filterSkus.stream().map(String::trim).toList();
        List<CartItemView> filteredItems = fullCart.items().stream()
                .filter(item -> upperSkus.contains(item.sku()))
                .toList();

        if (filteredItems.isEmpty()) {
            return new CartView(List.of(), 0, currencyFormat.format(0));
        }

        double subtotal = filteredItems.stream()
                .mapToDouble(item -> item.unitPrice() * item.quantity())
                .sum();
        int itemCount = filteredItems.stream()
                .mapToInt(CartItemView::quantity)
                .sum();

        return new CartView(filteredItems, itemCount, currencyFormat.format(subtotal));
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
            jdbcTemplate.update("DELETE FROM dbo.customer_cart_items WHERE user_id = ?", userId(customerEmail));
        }
        session.removeAttribute(CART_SESSION_KEY);
    }

    @Transactional(readOnly = false)
    public void mergeSessionCartToCustomerAfterRegister(HttpSession session, String customerEmail) {
        if (!hasText(customerEmail)) return;
        mergeSessionCartToCustomer(session, customerEmail);
    }

    @Scheduled(fixedRate = 300_000) // Every 5 minutes
    @Transactional(readOnly = false)
    public void releaseExpiredReservations() {
        jdbcTemplate.update("""
                DELETE FROM dbo.customer_cart_items
                WHERE reserved_until IS NOT NULL AND reserved_until < SYSUTCDATETIME()
                """);
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
        double unitPrice = variant.getProduct().getBasePrice().doubleValue();
        double lineTotal = unitPrice * quantity;

        return new CartItemView(
                variant.getSku(),
                variant.getProduct().getSlug(),
                variant.getProduct().getName(),
                variant.getProduct().getProductType(),
                variant.getColor(),
                variant.getSize(),
                imagePath(variant.getProduct().getSlug()),
                variant.getProduct().getCropClass(),
                quantity,
                variant.getStockQuantity(),
                unitPrice,
                currencyFormat.format(unitPrice),
                currencyFormat.format(lineTotal)
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

    private void addSkuToCustomerCart(String customerEmail, ProductVariant variant, int quantity) {
        Long userId = userId(customerEmail);
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

        // Reserve for 15 minutes
        java.time.LocalDateTime reservedUntil = java.time.LocalDateTime.now().plusMinutes(15);

        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.customer_cart_items
                WHERE user_id = ? AND sku = ?
                """, Integer.class, userId, variant.getSku());
        if (count != null && count > 0) {
            jdbcTemplate.update("""
                    UPDATE dbo.customer_cart_items
                    SET quantity = ?, reserved_until = ?, updated_at = SYSUTCDATETIME()
                    WHERE user_id = ? AND sku = ?
                    """, nextQuantity, reservedUntil, userId, variant.getSku());
        } else {
            jdbcTemplate.update("""
                    INSERT INTO dbo.customer_cart_items (user_id, sku, quantity, reserved_until)
                    VALUES (?, ?, ?, ?)
                    """, userId, variant.getSku(), nextQuantity, reservedUntil);
        }
    }

    private void updateCustomerCartItem(String customerEmail, String sku, int quantity) {
        Long userId = userId(customerEmail);
        if (quantity <= 0) {
            jdbcTemplate.update("DELETE FROM dbo.customer_cart_items WHERE user_id = ? AND sku = ?", userId, sku);
            return;
        }

        productVariantRepository.findBySkuInAndActiveTrueAndProductActiveTrue(List.of(sku))
                .stream()
                .findFirst()
                .ifPresent(variant -> {
                    java.time.LocalDateTime reservedUntil = java.time.LocalDateTime.now().plusMinutes(15);
                    jdbcTemplate.update("""
                        UPDATE dbo.customer_cart_items
                        SET quantity = ?, reserved_until = ?, updated_at = SYSUTCDATETIME()
                        WHERE user_id = ? AND sku = ?
                        """, Math.min(quantity, variant.getStockQuantity()), reservedUntil, userId, sku);
                });
    }

    private Map<String, Integer> readCustomerCart(String customerEmail) {
        Long userId = userId(customerEmail);
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

    private Long userId(String customerEmail) {
        return userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found."))
                .getId();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String imagePath(String slug) {
        return switch (slug) {
            case "air-cotton-tee" -> "/images/products/air-cotton-tee-v2.png";
            case "light-utility-jacket" -> "/images/products/light-utility-jacket-v2.png";
            case "soft-jersey-tee" -> "/images/products/soft-jersey-tee-v2.png";
            case "everyday-zip-hoodie" -> "/images/products/everyday-zip-hoodie-v2.png";
            case "smart-ankle-pants" -> "/images/products/smart-ankle-pants-v2.png";
            case "oxford-shirt" -> "/images/products/oxford-shirt-v2.png";
            case "linen-blend-shirt" -> "/images/products/linen-blend-shirt-v2.png";
            case "utility-tote" -> "/images/products/utility-tote-v2.png";
            case "easy-cotton-shorts" -> "/images/products/easy-cotton-shorts-v2.png";
            case "school-day-cardigan" -> "/images/products/school-day-cardigan-v2.png";
            default -> "/images/product-collage.png";
        };
    }
}
