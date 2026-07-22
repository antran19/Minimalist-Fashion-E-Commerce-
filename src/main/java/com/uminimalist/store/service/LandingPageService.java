package com.uminimalist.store.service;

import com.uminimalist.store.entity.Category;
import com.uminimalist.store.entity.Product;
import com.uminimalist.store.entity.ProductVariant;
import com.uminimalist.store.model.CategoryView;
import com.uminimalist.store.model.ProductImageView;
import com.uminimalist.store.model.ProductView;
import com.uminimalist.store.repository.CategoryRepository;
import com.uminimalist.store.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class LandingPageService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public LandingPageService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<CategoryView> getFeaturedCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(category -> new CategoryView(
                        category.getName(),
                        category.getDescription(),
                        "/products?collection=" + category.getSlug(),
                        "crop-" + category.getSlug()))
                .toList();
    }

    public List<ProductView> getNewArrivals() {
        return productViews().stream()
                .filter(ProductView::isNew)
                .filter(product -> !"kids".equalsIgnoreCase(product.collection()))
                .filter(product -> !"Accessories".equalsIgnoreCase(product.category()))
                .limit(8)
                .toList();
    }

    public List<ProductView> getProducts(String query, String collection, String size, String color,
            Double minPrice, Double maxPrice, String sort) {
        List<Product> products = productRepository.findByActiveTrue();

        Stream<Product> stream = products.stream();

        if (hasText(query)) {
            String normalizedQuery = normalize(query);
            stream = stream.filter(product -> normalize(product.getName()).contains(normalizedQuery)
                    || normalize(product.getCategory().getName()).contains(normalizedQuery)
                    || normalize(product.getCategory().getSlug()).contains(normalizedQuery));
        }

        if (hasText(collection)) {
            stream = stream.filter(product -> product.getCategory().getSlug().equalsIgnoreCase(collection));
        }

        // Variant level filter: check if ANY active variant matches size AND color
        if (hasText(color) || hasText(size)) {
            stream = stream.filter(product -> product.getVariants().stream().anyMatch(v -> {
                if (!v.isActive()) return false;
                if (hasText(color) && !v.getColor().equalsIgnoreCase(color)) return false;
                if (hasText(size) && !v.getSize().equalsIgnoreCase(size)) return false;
                return true;
            }));
        }

        if (minPrice != null) {
            stream = stream.filter(product -> product.getBasePrice().doubleValue() >= minPrice);
        }

        if (maxPrice != null) {
            stream = stream.filter(product -> product.getBasePrice().doubleValue() <= maxPrice);
        }

        // Filter by sort option if sort is "new" or "best"
        if ("new".equalsIgnoreCase(sort)) {
            stream = stream.filter(Product::isNewArrival);
        } else if ("best".equalsIgnoreCase(sort)) {
            stream = stream.filter(Product::isBestSeller);
        }

        List<ProductView> views = stream.map(this::toProductView).toList();
        return views.stream().sorted(productComparator(sort)).toList();
    }

    public List<ProductView> getProducts(String query, String collection, String size, String color,
            Double minPrice, Double maxPrice, String sort, Boolean inStockOnly) {
        return getProducts(query, collection, size, color, minPrice, maxPrice, sort);
    }

    public Optional<ProductView> getProduct(String slug) {
        return productRepository.findBySlugAndActiveTrue(slug).map(this::toProductView);
    }

    public java.util.Map<String, Integer> getVariantStockMap(String slug) {
        return productRepository.findBySlugAndActiveTrue(slug)
                .map(product -> product.getVariants().stream()
                        .filter(ProductVariant::isActive)
                        .collect(java.util.stream.Collectors.toMap(
                                v -> (v.getColor() + "_" + v.getSize()).toUpperCase(Locale.ROOT),
                                ProductVariant::getStockQuantity,
                                (existing, replacement) -> existing
                        ))
                ).orElse(java.util.Collections.emptyMap());
    }

    public List<String> getCollections() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc()
                .stream()
                .map(Category::getSlug)
                .toList();
    }

    public List<String> getSizes() {
        return productRepository.findByActiveTrue()
                .stream()
                .flatMap(product -> product.getVariants().stream())
                .filter(ProductVariant::isActive)
                .map(ProductVariant::getSize)
                .distinct()
                .toList();
    }

    public List<String> getColors() {
        return productRepository.findByActiveTrue()
                .stream()
                .flatMap(product -> product.getVariants().stream())
                .filter(ProductVariant::isActive)
                .map(ProductVariant::getColor)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<String> getEssentials() {
        return List.of(
                "Variant-first product detail pages for size and color accuracy",
                "Clear cart totals before checkout",
                "Inventory checks before every order",
                "Responsive pages built for quick browsing");
    }

    private List<ProductView> productViews() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toProductView)
                .toList();
    }

    public List<ProductView> getRelatedProducts(String currentSlug, String category, int limit) {
        List<ProductView> all = productViews();
        List<ProductView> sameCategory = all.stream()
                .filter(p -> !p.slug().equalsIgnoreCase(currentSlug))
                .filter(p -> (p.collection() != null && p.collection().equalsIgnoreCase(category))
                        || (p.category() != null && p.category().equalsIgnoreCase(category)))
                .limit(limit)
                .toList();

        if (sameCategory.size() < limit) {
            List<String> existingSlugs = new java.util.ArrayList<>(sameCategory.stream().map(ProductView::slug).toList());
            existingSlugs.add(currentSlug);

            List<ProductView> others = all.stream()
                    .filter(p -> !existingSlugs.contains(p.slug()))
                    .limit(limit - sameCategory.size())
                    .toList();

            List<ProductView> combined = new java.util.ArrayList<>(sameCategory);
            combined.addAll(others);
            return combined;
        }

        return sameCategory;
    }

    private ProductView toProductView(Product product) {
        List<ProductVariant> activeVariants = product.getVariants()
                .stream()
                .filter(ProductVariant::isActive)
                .toList();

        List<String> colors = activeVariants.stream()
                .map(ProductVariant::getColor)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();

        List<String> sizes = activeVariants.stream()
                .map(ProductVariant::getSize)
                .distinct()
                .toList();

        int stock = activeVariants.stream()
                .mapToInt(ProductVariant::getStockQuantity)
                .sum();

        double price = product.getBasePrice().doubleValue();
        String desc = hasText(product.getDescription())
                ? product.getDescription()
                : "Designed for everyday rotation with a clean shape, simple care, and transparent stock before checkout.";

        List<ProductImageView> imageViews = product.getImages().stream()
                .map(img -> new ProductImageView(
                        img.getId(),
                        img.getImageUrl(),
                        img.getPublicId(),
                        img.getColor(),
                        img.isPrimary(),
                        img.getDisplayOrder()
                ))
                .toList();

        String resolvedImagePath = imageViews.stream()
                .filter(ProductImageView::isPrimary)
                .map(ProductImageView::imageUrl)
                .findFirst()
                .orElseGet(() -> imageViews.stream()
                        .map(ProductImageView::imageUrl)
                        .findFirst()
                        .orElseGet(() -> imagePath(product.getSlug())));

        return new ProductView(
                product.getId(),
                product.getSlug(),
                product.getName(),
                product.getCategory().getSlug(),
                product.getProductType(),
                desc,
                price,
                currencyFormat.format(price),
                colors,
                sizes,
                resolvedImagePath,
                product.getCropClass(),
                stock,
                product.isNewArrival(),
                product.isBestSeller(),
                imageViews,
                product.getCreatedAt()
        );
    }

    private Comparator<ProductView> productComparator(String sort) {
        if (sort == null || sort.isBlank() || sort.equalsIgnoreCase("default")) {
            return Comparator.comparing(p -> p.id() != null ? p.id() : 0L);
        }

        return switch (sort.toLowerCase(Locale.ROOT)) {
            case "price-asc" -> Comparator.comparingDouble(ProductView::price)
                    .thenComparing(p -> p.id() != null ? p.id() : 0L);
            case "price-desc" -> Comparator.comparingDouble(ProductView::price).reversed()
                    .thenComparing(p -> p.id() != null ? p.id() : 0L);
            case "best" -> Comparator.comparing(ProductView::bestSeller).reversed()
                    .thenComparing(p -> p.id() != null ? p.id() : 0L, Comparator.reverseOrder());
            case "new" -> Comparator.comparing(ProductView::isNew).reversed()
                    .thenComparing(p -> p.createdAt() != null ? p.createdAt() : java.time.LocalDateTime.MIN, Comparator.reverseOrder())
                    .thenComparing(p -> p.id() != null ? p.id() : 0L, Comparator.reverseOrder());
            default -> Comparator.comparing(p -> p.id() != null ? p.id() : 0L);
        };
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private String normalize(String value) {
        return value.toLowerCase(Locale.ROOT).trim();
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
