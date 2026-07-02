package com.uminimalist.store.service;

import com.uminimalist.store.entity.ProductVariant;
import com.uminimalist.store.model.CartItemView;
import com.uminimalist.store.model.CartView;
import com.uminimalist.store.repository.ProductVariantRepository;
import jakarta.servlet.http.HttpSession;
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
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public ShoppingCartService(ProductVariantRepository productVariantRepository) {
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional(readOnly = false)
    public void addItem(HttpSession session, String productSlug, String color, String size, int quantity) {
        ProductVariant variant = productVariantRepository
                .findFirstByProductSlugAndColorIgnoreCaseAndSizeIgnoreCaseAndActiveTrueAndProductActiveTrue(productSlug, color, size)
                .orElseThrow(() -> new IllegalArgumentException("Product variant is not available."));

        Map<String, Integer> cart = mutableCart(session);
        int requestedQuantity = Math.max(quantity, 1);
        int currentQuantity = cart.getOrDefault(variant.getSku(), 0);
        int nextQuantity = Math.min(currentQuantity + requestedQuantity, variant.getStockQuantity());

        if (nextQuantity > 0) {
            cart.put(variant.getSku(), nextQuantity);
        }
    }

    public void updateItem(HttpSession session, String sku, int quantity) {
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
        mutableCart(session).remove(sku);
    }

    public CartView getCart(HttpSession session) {
        Map<String, Integer> cart = readCart(session);
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

    public int getItemCount(HttpSession session) {
        return readCart(session).values()
                .stream()
                .mapToInt(Integer::intValue)
                .sum();
    }

    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
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

    private String imagePath(String slug) {
        return switch (slug) {
            case "air-cotton-tee" -> "/images/products/air-cotton-tee.png";
            case "light-utility-jacket" -> "/images/products/light-utility-jacket.png";
            case "soft-jersey-tee" -> "/images/products/soft-jersey-tee.png";
            case "everyday-zip-hoodie" -> "/images/products/everyday-zip-hoodie.png";
            case "smart-ankle-pants" -> "/images/products/smart-ankle-pants.png";
            case "school-day-cardigan" -> "/images/kids-campaign.png";
            default -> "/images/product-collage.png";
        };
    }
}
