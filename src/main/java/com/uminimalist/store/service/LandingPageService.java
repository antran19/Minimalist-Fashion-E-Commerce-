package com.uminimalist.store.service;

import com.uminimalist.store.model.CategoryView;
import com.uminimalist.store.model.ProductView;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

@Service
public class LandingPageService {
    private static final List<ProductView> PRODUCTS = List.of(
            new ProductView("air-cotton-tee", "Air Cotton Tee", "men", "T-shirt", 19.90, "$19.90",
                    List.of("Cream", "White"), List.of("S", "M", "L", "XL"), "product-tee", 48, true, true),
            new ProductView("light-utility-jacket", "Light Utility Jacket", "men", "Outerwear", 59.90, "$59.90",
                    List.of("Navy"), List.of("S", "M", "L"), "product-jacket", 18, true, false),
            new ProductView("oxford-shirt", "Oxford Shirt", "men", "Shirt", 34.90, "$34.90",
                    List.of("White"), List.of("S", "M", "L", "XL"), "product-shirt", 31, false, true),
            new ProductView("soft-jersey-tee", "Soft Jersey Tee", "women", "T-shirt", 19.90, "$19.90",
                    List.of("Sage"), List.of("XS", "S", "M", "L"), "product-sage", 42, true, false),
            new ProductView("smart-ankle-pants", "Smart Ankle Pants", "women", "Pants", 39.90, "$39.90",
                    List.of("Black"), List.of("XS", "S", "M", "L", "XL"), "product-pants", 26, false, true),
            new ProductView("everyday-zip-hoodie", "Everyday Zip Hoodie", "women", "Sweatshirt", 49.90, "$49.90",
                    List.of("Grey"), List.of("S", "M", "L", "XL"), "product-hoodie", 15, true, false),
            new ProductView("linen-blend-shirt", "Linen Blend Shirt", "men", "Shirt", 34.90, "$34.90",
                    List.of("Natural"), List.of("S", "M", "L", "XL"), "product-linen", 22, false, false),
            new ProductView("utility-tote", "Utility Tote", "women", "Accessories", 14.90, "$14.90",
                    List.of("Red"), List.of("One size"), "product-tote", 64, true, true),
            new ProductView("school-day-cardigan", "School Day Cardigan", "kids", "Knitwear", 29.90, "$29.90",
                    List.of("Blue", "Grey"), List.of("110", "120", "130", "140"), "product-kids-cardigan", 34, true, false),
            new ProductView("easy-cotton-shorts", "Easy Cotton Shorts", "kids", "Shorts", 16.90, "$16.90",
                    List.of("Khaki"), List.of("110", "120", "130", "140"), "product-kids-shorts", 28, false, true)
    );

    public List<CategoryView> getFeaturedCategories() {
        return List.of(
                new CategoryView("Men", "Everyday layers with clean lines.", "/products?collection=men", "crop-men"),
                new CategoryView("Women", "Soft essentials for simple routines.", "/products?collection=women", "crop-women"),
                new CategoryView("Kids", "Easy pieces for school and weekends.", "/products?collection=kids", "crop-kids")
        );
    }

    public List<ProductView> getNewArrivals() {
        return PRODUCTS.stream()
                .filter(ProductView::isNew)
                .limit(8)
                .toList();
    }

    public List<ProductView> getProducts(String query, String collection, String size, String color,
                                         Double minPrice, Double maxPrice, String sort) {
        Stream<ProductView> stream = PRODUCTS.stream();

        if (hasText(query)) {
            String normalizedQuery = normalize(query);
            stream = stream.filter(product -> normalize(product.name()).contains(normalizedQuery)
                    || normalize(product.category()).contains(normalizedQuery));
        }

        if (hasText(collection)) {
            stream = stream.filter(product -> product.collection().equalsIgnoreCase(collection));
        }

        if (hasText(size)) {
            stream = stream.filter(product -> product.sizes().stream().anyMatch(option -> option.equalsIgnoreCase(size)));
        }

        if (hasText(color)) {
            stream = stream.filter(product -> product.colors().stream().anyMatch(option -> option.equalsIgnoreCase(color)));
        }

        if (minPrice != null) {
            stream = stream.filter(product -> product.price() >= minPrice);
        }

        if (maxPrice != null) {
            stream = stream.filter(product -> product.price() <= maxPrice);
        }

        Comparator<ProductView> comparator = switch (Optional.ofNullable(sort).orElse("")) {
            case "price-asc" -> Comparator.comparingDouble(ProductView::price);
            case "price-desc" -> Comparator.comparingDouble(ProductView::price).reversed();
            case "best" -> Comparator.comparing(ProductView::bestSeller).reversed()
                    .thenComparing(ProductView::name);
            case "new" -> Comparator.comparing(ProductView::isNew).reversed()
                    .thenComparing(ProductView::name);
            default -> Comparator.comparing(ProductView::name);
        };

        return stream.sorted(comparator).toList();
    }

    public Optional<ProductView> getProduct(String slug) {
        return PRODUCTS.stream()
                .filter(product -> product.slug().equals(slug))
                .findFirst();
    }

    public List<String> getCollections() {
        return PRODUCTS.stream()
                .map(ProductView::collection)
                .distinct()
                .toList();
    }

    public List<String> getSizes() {
        return PRODUCTS.stream()
                .flatMap(product -> product.sizes().stream())
                .distinct()
                .toList();
    }

    public List<String> getColors() {
        return PRODUCTS.stream()
                .flatMap(product -> product.colors().stream())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> getEssentials() {
        return List.of(
                "Variant-first product detail pages for size and color accuracy",
                "Clear cart totals before checkout",
                "Inventory checks before every order",
                "Responsive pages built for quick browsing"
        );
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).trim();
    }
}
