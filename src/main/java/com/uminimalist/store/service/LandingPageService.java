package com.uminimalist.store.service;

import com.uminimalist.store.entity.Category;
import com.uminimalist.store.entity.Product;
import com.uminimalist.store.entity.ProductVariant;
import com.uminimalist.store.model.CategoryView;
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
                        "crop-" + category.getSlug()
                ))
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
        Stream<ProductView> stream = productViews().stream();

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

        return stream.sorted(productComparator(sort)).toList();
    }

    public Optional<ProductView> getProduct(String slug) {
        return productRepository.findBySlugAndActiveTrue(slug).map(this::toProductView);
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
                "Responsive pages built for quick browsing"
        );
    }

    private List<ProductView> productViews() {
        return productRepository.findByActiveTrue()
                .stream()
                .map(this::toProductView)
                .toList();
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

        return new ProductView(
                product.getSlug(),
                product.getName(),
                product.getCategory().getSlug(),
                product.getProductType(),
                price,
                currencyFormat.format(price),
                colors,
                sizes,
                imagePath(product.getSlug()),
                product.getCropClass(),
                stock,
                product.isNewArrival(),
                product.isBestSeller()
        );
    }

    private Comparator<ProductView> productComparator(String sort) {
        return switch (Optional.ofNullable(sort).orElse("")) {
            case "price-asc" -> Comparator.comparingDouble(ProductView::price);
            case "price-desc" -> Comparator.comparingDouble(ProductView::price).reversed();
            case "best" -> Comparator.comparing(ProductView::bestSeller).reversed()
                    .thenComparing(ProductView::name);
            case "new" -> Comparator.comparing(ProductView::isNew).reversed()
                    .thenComparing(ProductView::name);
            default -> Comparator.comparing(ProductView::name);
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
