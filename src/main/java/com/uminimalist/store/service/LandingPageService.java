package com.uminimalist.store.service;

import com.uminimalist.store.entity.Category;
import com.uminimalist.store.entity.Product;
import com.uminimalist.store.entity.ProductImage;
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
        List<String> rawSizes = productRepository.findByActiveTrue()
                .stream()
                .flatMap(product -> product.getVariants().stream())
                .filter(ProductVariant::isActive)
                .map(ProductVariant::getSize)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .toList();

        List<String> letterOrder = List.of("XXS", "XS", "S", "M", "L", "XL", "XXL", "2XL", "3XL", "4XL");

        return rawSizes.stream()
                .sorted((s1, s2) -> {
                    String u1 = s1.trim().toUpperCase(java.util.Locale.ROOT);
                    String u2 = s2.trim().toUpperCase(java.util.Locale.ROOT);

                    int idx1 = letterOrder.indexOf(u1);
                    int idx2 = letterOrder.indexOf(u2);

                    if (idx1 != -1 && idx2 != -1) {
                        return Integer.compare(idx1, idx2);
                    }
                    if (idx1 != -1) return -1;
                    if (idx2 != -1) return 1;

                    boolean isNum1 = u1.matches("\\d+");
                    boolean isNum2 = u2.matches("\\d+");

                    if (isNum1 && isNum2) {
                        return Integer.compare(Integer.parseInt(u1), Integer.parseInt(u2));
                    }
                    if (isNum1) return -1;
                    if (isNum2) return 1;

                    return u1.compareTo(u2);
                })
                .toList();
    }

    public List<String> getColors() {
        return productRepository.findByActiveTrue()
                .stream()
                .flatMap(product -> product.getVariants().stream())
                .filter(ProductVariant::isActive)
                .map(ProductVariant::getColor)
                .filter(c -> c != null && !c.isBlank() && !"Default".equalsIgnoreCase(c.trim()))
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

        List<ProductImageView> imageViews = new java.util.ArrayList<>();
        java.util.Set<String> seenColors = new java.util.HashSet<>();
        java.util.Set<String> seenUrls = new java.util.HashSet<>();

        // First add images from product_images table (sorted by isPrimary DESC, displayOrder ASC)
        product.getImages().stream()
                .sorted(java.util.Comparator.comparing(ProductImage::isPrimary).reversed()
                        .thenComparing(ProductImage::getDisplayOrder))
                .forEach(img -> {
                    String colorKey = img.getColor() != null ? img.getColor().trim().toLowerCase(java.util.Locale.ROOT) : "";
                    String urlKey = img.getImageUrl() != null ? img.getImageUrl().trim() : "";
                    if (urlKey.isEmpty()) return;
                    if (seenUrls.contains(urlKey)) return;
                    if (!colorKey.isEmpty() && seenColors.contains(colorKey)) return;

                    if (!colorKey.isEmpty()) seenColors.add(colorKey);
                    seenUrls.add(urlKey);
                    imageViews.add(new ProductImageView(
                            img.getId(),
                            img.getImageUrl(),
                            img.getPublicId(),
                            img.getColor(),
                            img.isPrimary(),
                            img.getDisplayOrder()
                    ));
                });

        // Then fallback to active variants for missing colors
        activeVariants.stream()
                .filter(v -> v.getImageUrl() != null && !v.getImageUrl().isBlank())
                .forEach(v -> {
                    String colorKey = v.getColor() != null ? v.getColor().trim().toLowerCase(java.util.Locale.ROOT) : "";
                    String urlKey = v.getImageUrl().trim();
                    if (seenUrls.contains(urlKey)) return;
                    if (!colorKey.isEmpty() && seenColors.contains(colorKey)) return;

                    if (!colorKey.isEmpty()) seenColors.add(colorKey);
                    seenUrls.add(urlKey);
                    imageViews.add(new ProductImageView(
                            v.getId(),
                            v.getImageUrl(),
                            v.getImagePublicId(),
                            v.getColor(),
                            false,
                            imageViews.size()
                    ));
                });

        String resolvedImagePath = imageViews.stream()
                .filter(ProductImageView::isPrimary)
                .map(ProductImageView::imageUrl)
                .findFirst()
                .orElseGet(() -> imageViews.stream()
                        .map(ProductImageView::imageUrl)
                        .findFirst()
                        .orElseGet(() -> activeVariants.stream()
                                .filter(v -> v.getImageUrl() != null && !v.getImageUrl().isBlank())
                                .map(ProductVariant::getImageUrl)
                                .findFirst()
                                .orElseGet(() -> imagePath(product.getSlug()))));

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
                product.isOnSale(),
                product.getDiscountPercentage() != null ? product.getDiscountPercentage() : 0,
                product.getSalePrice().doubleValue(),
                currencyFormat.format(product.getSalePrice().doubleValue()),
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
