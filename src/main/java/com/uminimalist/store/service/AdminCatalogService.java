package com.uminimalist.store.service;

import com.uminimalist.store.entity.Category;
import com.uminimalist.store.entity.Product;
import com.uminimalist.store.entity.ProductVariant;
import com.uminimalist.store.entity.User;
import com.uminimalist.store.repository.CategoryRepository;
import com.uminimalist.store.repository.ProductRepository;
import com.uminimalist.store.repository.ProductVariantRepository;
import com.uminimalist.store.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

@Service
public class AdminCatalogService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    public AdminCatalogService(ProductRepository productRepository,
                               ProductVariantRepository productVariantRepository,
                               UserRepository userRepository,
                               CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return productRepository.findAllWithCategoryAndVariants()
                .stream()
                .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findAllByOrderByDisplayOrderAscNameAsc();
    }

    @Transactional(readOnly = true)
    public List<String> getProductTypes() {
        List<String> dbTypes = productRepository.findDistinctProductTypes();
        if (dbTypes == null || dbTypes.isEmpty()) {
            return List.of("T-shirt", "Outerwear", "Shirt", "Pants", "Sweatshirt", "Accessories", "Knitwear", "Shorts");
        }
        return dbTypes;
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public void createCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        Category category = new Category();
        category.setName(name.trim());
        category.setDescription(description != null ? description.trim() : "");
        category.setSlug(generateSlug(name) + "-" + System.currentTimeMillis() % 10000);
        category.setDisplayOrder(10);
        category.setActive(true);
        categoryRepository.save(category);
    }

    @Transactional
    public void toggleCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        category.setActive(!category.isActive());
    }

    @Transactional
    public void createProduct(Long categoryId, String name, String basePriceStr, String productType, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        BigDecimal formattedPrice = parseAndValidatePrice(basePriceStr);
        String validProductType = validateProductType(productType);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        Product product = new Product();
        product.setCategory(category);
        product.setName(name.trim());
        product.setBasePrice(formattedPrice);
        product.setProductType(validProductType);
        product.setDescription(description != null ? description.trim() : "");
        product.setSlug(generateSlug(name) + "-" + System.currentTimeMillis() % 10000);
        product.setCropClass("crop-top");
        product.setNewArrival(true);
        product.setBestSeller(false);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());

        productRepository.save(product);
    }

    @Transactional
    public void updateProduct(Long productId, Long categoryId, String name, String basePriceStr, String productType, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        BigDecimal formattedPrice = parseAndValidatePrice(basePriceStr);
        String validProductType = validateProductType(productType);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        product.setCategory(category);
        product.setName(name.trim());
        product.setBasePrice(formattedPrice);
        product.setProductType(validProductType);
        product.setDescription(description != null ? description.trim() : "");
    }

    private BigDecimal parseAndValidatePrice(String priceStr) {
        if (priceStr == null || priceStr.trim().isEmpty()) {
            throw new IllegalArgumentException("Base price cannot be empty.");
        }
        String cleanStr = priceStr.replaceAll(",", "").trim();
        try {
            BigDecimal price = new BigDecimal(cleanStr);
            if (price.compareTo(BigDecimal.ZERO) <= 0) {
                throw new IllegalArgumentException("Base price must be greater than 0 (e.g. 19.90 or 1,000.50).");
            }
            return price.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid price format. Please enter a valid number (e.g. 19.90 or 1,000.50).");
        }
    }

    private String validateProductType(String inputType) {
        if (inputType == null || inputType.trim().isEmpty()) {
            throw new IllegalArgumentException("Product Type cannot be empty.");
        }
        String trimmed = inputType.trim();
        List<String> dbTypes = getProductTypes();
        for (String dbType : dbTypes) {
            if (dbType.equalsIgnoreCase(trimmed)) {
                return dbType; // Match exact casing from DB
            }
        }
        // If it's a new product type, capitalize the first letter
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }

    @Transactional
    public void createVariant(Long productId, String color, String size, String sku, int stockQuantity) {
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("Color cannot be empty.");
        }
        if (size == null || size.trim().isEmpty()) {
            throw new IllegalArgumentException("Size cannot be empty.");
        }
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be empty.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(color.trim());
        variant.setSize(size.trim());
        variant.setSku(sku.trim().toUpperCase(Locale.ROOT));
        variant.setStockQuantity(stockQuantity);
        variant.setActive(true);

        productVariantRepository.save(variant);
    }

    @Transactional
    public void toggleProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        product.setActive(!product.isActive());
    }

    @Transactional
    public void updateVariantStock(Long variantId, int stockQuantity) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));
        variant.setStockQuantity(Math.max(stockQuantity, 0));
    }

    @Transactional
    public void toggleVariant(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));
        variant.setActive(!variant.isActive());
    }

    @Transactional
    public void toggleUser(Long userId, String currentEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        if (user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new IllegalArgumentException("You cannot disable your own account.");
        }
        user.setActive(!user.isActive());
    }

    private String generateSlug(String text) {
        if (text == null) return "";
        String slug = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
        return slug.isEmpty() ? "item-" + System.currentTimeMillis() : slug;
    }
}
