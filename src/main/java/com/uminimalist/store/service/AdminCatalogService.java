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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminCatalogService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;

    public AdminCatalogService(CategoryRepository categoryRepository,
                               ProductRepository productRepository,
                               ProductVariantRepository productVariantRepository,
                               UserRepository userRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Category> getCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAscNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return productRepository.findAllWithCategoryAndVariants()
                .stream()
                .sorted(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<User> getUsers() {
        return userRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    @Transactional
    public void saveCategory(Category category) {
        if (category.getId() == null) {
            categoryRepository.save(category);
        } else {
            Category existing = categoryRepository.findById(category.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found."));
            existing.setName(category.getName());
            existing.setSlug(category.getSlug());
            existing.setDescription(category.getDescription());
            existing.setDisplayOrder(category.getDisplayOrder());
            existing.setActive(category.isActive());
        }
    }

    @Transactional
    public void saveProduct(Product product) {
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryRepository.findById(product.getCategory().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found."));
            product.setCategory(category);
        }

        if (product.getId() == null) {
            product.setCreatedAt(LocalDateTime.now());
            productRepository.save(product);
        } else {
            Product existing = productRepository.findById(product.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found."));
            existing.setName(product.getName());
            existing.setSlug(product.getSlug());
            existing.setCategory(product.getCategory());
            existing.setProductType(product.getProductType());
            existing.setDescription(product.getDescription());
            existing.setBasePrice(product.getBasePrice());
            existing.setCropClass(product.getCropClass());
            existing.setNewArrival(product.isNewArrival());
            existing.setBestSeller(product.isBestSeller());
            existing.setActive(product.isActive());
        }
    }

    @Transactional
    public void saveVariant(ProductVariant variant) {
        if (variant.getProduct() != null && variant.getProduct().getId() != null) {
            Product product = productRepository.findById(variant.getProduct().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found."));
            variant.setProduct(product);
        }

        if (variant.getId() == null) {
            productVariantRepository.save(variant);
        } else {
            ProductVariant existing = productVariantRepository.findById(variant.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Variant not found."));
            existing.setProduct(variant.getProduct());
            existing.setSku(variant.getSku());
            existing.setColor(variant.getColor());
            existing.setSize(variant.getSize());
            existing.setStockQuantity(variant.getStockQuantity());
            existing.setActive(variant.isActive());
        }
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
}
