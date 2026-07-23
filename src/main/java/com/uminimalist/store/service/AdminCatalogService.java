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

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import com.uminimalist.store.entity.ProductImage;
import com.uminimalist.store.repository.ProductImageRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AdminCatalogService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProductImageRepository productImageRepository;
    private final CloudinaryService cloudinaryService;
    private final JdbcTemplate jdbcTemplate;

    public AdminCatalogService(ProductRepository productRepository,
                               ProductVariantRepository productVariantRepository,
                               UserRepository userRepository,
                               CategoryRepository categoryRepository,
                               PasswordEncoder passwordEncoder,
                               ProductImageRepository productImageRepository,
                               CloudinaryService cloudinaryService,
                               JdbcTemplate jdbcTemplate) {
        this.productRepository = productRepository;
        this.productVariantRepository = productVariantRepository;
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.passwordEncoder = passwordEncoder;
        this.productImageRepository = productImageRepository;
        this.cloudinaryService = cloudinaryService;
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void ensureVariantImageColumns() {
        jdbcTemplate.execute("""
                IF COL_LENGTH('dbo.product_variants', 'image_url') IS NULL
                BEGIN
                    ALTER TABLE dbo.product_variants ADD image_url NVARCHAR(500) NULL;
                END
                """);
        jdbcTemplate.execute("""
                IF COL_LENGTH('dbo.product_variants', 'image_public_id') IS NULL
                BEGIN
                    ALTER TABLE dbo.product_variants ADD image_public_id NVARCHAR(200) NULL;
                END
                """);
        // Migrate existing product_images to matching variant SKUs by color
        jdbcTemplate.execute("""
                UPDATE pv
                SET pv.image_url = pi.image_url,
                    pv.image_public_id = pi.public_id
                FROM dbo.product_variants pv
                JOIN dbo.product_images pi ON pi.product_id = pv.product_id AND pi.color = pv.color
                WHERE pv.image_url IS NULL
                """);
    }

    @Transactional(readOnly = true)
    public List<Product> getProducts() {
        return productRepository.findAllWithCategoryAndVariants()
                .stream()
                .sorted(Comparator.comparing(Product::getId).reversed())
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

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Product> getProductsPaged(int page, int size, String query, String filter) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id"));
        if (query != null && !query.trim().isEmpty()) {
            return productRepository.searchProducts(query.trim(), pageable);
        }
        if ("active".equalsIgnoreCase(filter)) {
            return productRepository.findByActivePaged(true, pageable);
        } else if ("hidden".equalsIgnoreCase(filter)) {
            return productRepository.findByActivePaged(false, pageable);
        } else if ("low-stock".equalsIgnoreCase(filter)) {
            return productRepository.findByLowStockPaged(pageable);
        }
        return productRepository.findAllPaged(pageable);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<User> getUsersPaged(int page, int size, String query) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), size, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));
        if (query != null && !query.trim().isEmpty()) {
            return userRepository.searchUsers(query.trim(), pageable);
        }
        return userRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Category> getCategoriesPaged(int page, int size, String query) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), size);
        if (query != null && !query.trim().isEmpty()) {
            return categoryRepository.searchCategories(query.trim(), pageable);
        }
        return categoryRepository.findAllByOrderByDisplayOrderAscNameAsc(pageable);
    }

    @Transactional
    public void createCategory(String name, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        String cleanName = name.trim();
        if (cleanName.length() > 120) {
            throw new IllegalArgumentException("Category name must be 120 characters or fewer.");
        }
        categoryRepository.findByNameIgnoreCase(cleanName).ifPresent(c -> {
            throw new IllegalArgumentException("Category '" + cleanName + "' already exists.");
        });

        Category category = new Category();
        category.setName(cleanName);
        category.setDescription(description != null ? description.trim() : "");
        category.setSlug(generateSlug(cleanName) + "-" + System.currentTimeMillis() % 10000);
        category.setDisplayOrder(10);
        category.setActive(true);
        categoryRepository.save(category);
    }

    @Transactional
    public void updateCategory(Long categoryId, String name, String description) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty.");
        }
        String cleanName = name.trim();
        if (cleanName.length() > 120) {
            throw new IllegalArgumentException("Category name must be 120 characters or fewer.");
        }
        categoryRepository.findByNameIgnoreCase(cleanName).ifPresent(existing -> {
            if (!existing.getId().equals(categoryId)) {
                throw new IllegalArgumentException("Category '" + cleanName + "' already exists.");
            }
        });

        category.setName(cleanName);
        category.setDescription(description != null ? description.trim() : "");
        categoryRepository.save(category);
    }

    @Transactional
    public void toggleCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        category.setActive(!category.isActive());
    }

    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));
        if (category.getProducts() != null && !category.getProducts().isEmpty()) {
            throw new IllegalArgumentException("Cannot delete category '" + category.getName() + "' because it contains " + category.getProducts().size() + " product(s). Reassign or delete products first.");
        }
        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Cannot delete category '" + category.getName() + "' because it has associated records.");
        }
    }

    @Transactional
    public void createProduct(Long categoryId, String name, String basePriceStr, String productType, String description, boolean onSale, Integer discountPercentage) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        String cleanName = name.trim();
        if (cleanName.length() > 180) {
            throw new IllegalArgumentException("Product name must be 180 characters or fewer.");
        }
        BigDecimal formattedPrice = parseAndValidatePrice(basePriceStr);
        String validProductType = validateProductType(productType);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        Product product = new Product();
        product.setCategory(category);
        product.setName(cleanName);
        product.setBasePrice(formattedPrice);
        product.setProductType(validProductType);
        product.setDescription(description != null ? description.trim() : "");
        product.setSlug(generateSlug(cleanName) + "-" + System.currentTimeMillis() % 10000);
        product.setCropClass("crop-top");
        product.setNewArrival(true);
        product.setBestSeller(false);
        product.setOnSale(onSale);
        product.setDiscountPercentage(discountPercentage);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());

        productRepository.save(product);
    }

    @Transactional
    public void updateProduct(Long productId, Long categoryId, String name, String basePriceStr, String productType, String description, boolean onSale, Integer discountPercentage) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty.");
        }
        String cleanName = name.trim();
        if (cleanName.length() > 180) {
            throw new IllegalArgumentException("Product name must be 180 characters or fewer.");
        }
        BigDecimal formattedPrice = parseAndValidatePrice(basePriceStr);
        String validProductType = validateProductType(productType);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found."));

        product.setCategory(category);
        product.setName(cleanName);
        product.setBasePrice(formattedPrice);
        product.setProductType(validProductType);
        product.setDescription(description != null ? description.trim() : "");
        product.setOnSale(onSale);
        product.setDiscountPercentage(discountPercentage);
        productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));
        try {
            productRepository.delete(product);
            productRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Cannot delete product '" + product.getName() + "' because it has associated orders or cart items. Disable (hide) the product instead.");
        }
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
            if (price.compareTo(new BigDecimal("1000000000")) >= 0) {
                throw new IllegalArgumentException("Base price must be less than 1,000,000,000.");
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
    public void createVariant(Long productId, String color, String size, String sku, int stockQuantity, MultipartFile imageFile) {
        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("Color cannot be empty.");
        }
        if (size == null || size.trim().isEmpty()) {
            throw new IllegalArgumentException("Size cannot be empty.");
        }
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be empty.");
        }
        String cleanSku = sku.trim().toUpperCase(Locale.ROOT);
        if (cleanSku.length() > 80) {
            throw new IllegalArgumentException("SKU must be 80 characters or fewer.");
        }
        productVariantRepository.findBySkuIgnoreCase(cleanSku).ifPresent(v -> {
            throw new IllegalArgumentException("SKU '" + cleanSku + "' is already in use by another variant.");
        });
        productVariantRepository.findByProductIdAndColorIgnoreCaseAndSizeIgnoreCase(productId, color.trim(), size.trim()).ifPresent(v -> {
            throw new IllegalArgumentException("A variant with Color '" + color.trim() + "' and Size '" + size.trim() + "' already exists for this product. Edit the existing variant instead.");
        });
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setColor(color.trim());
        variant.setSize(size.trim());
        variant.setSku(cleanSku);
        variant.setStockQuantity(stockQuantity);
        variant.setActive(true);

        // Upload variant image to Cloudinary if provided, or inherit image for existing color
        if (imageFile != null && !imageFile.isEmpty()) {
            String folderPath = "uminimalist/products/" + product.getSlug();
            CloudinaryService.UploadResult result = cloudinaryService.uploadImage(imageFile, folderPath);
            variant.setImageUrl(result.secureUrl());
            variant.setImagePublicId(result.publicId());
        } else {
            // Inherit image from existing color variant or product_images if available
            syncInheritedImageForColor(product, variant);
        }

        productVariantRepository.save(variant);

        if (variant.getImageUrl() != null && !variant.getImageUrl().isBlank()) {
            syncVariantImageToProductImagesAndAllVariants(product, variant);
        }
    }

    @Transactional
    public void updateVariant(Long variantId, String color, String size, String sku, int stockQuantity, MultipartFile imageFile) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));

        if (color == null || color.trim().isEmpty()) {
            throw new IllegalArgumentException("Color cannot be empty.");
        }
        if (size == null || size.trim().isEmpty()) {
            throw new IllegalArgumentException("Size cannot be empty.");
        }
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU cannot be empty.");
        }
        String cleanSku = sku.trim().toUpperCase(Locale.ROOT);
        if (cleanSku.length() > 80) {
            throw new IllegalArgumentException("SKU must be 80 characters or fewer.");
        }
        productVariantRepository.findBySkuIgnoreCase(cleanSku).ifPresent(existing -> {
            if (!existing.getId().equals(variantId)) {
                throw new IllegalArgumentException("SKU '" + cleanSku + "' is already in use by another variant.");
            }
        });
        productVariantRepository.findByProductIdAndColorIgnoreCaseAndSizeIgnoreCase(variant.getProduct().getId(), color.trim(), size.trim()).ifPresent(existing -> {
            if (!existing.getId().equals(variantId)) {
                throw new IllegalArgumentException("A variant with Color '" + color.trim() + "' and Size '" + size.trim() + "' already exists for this product.");
            }
        });
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }

        variant.setColor(color.trim());
        variant.setSize(size.trim());
        variant.setSku(cleanSku);
        variant.setStockQuantity(stockQuantity);

        productVariantRepository.save(variant);

        if (imageFile != null && !imageFile.isEmpty()) {
            replaceVariantImage(variantId, imageFile);
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
    public void deleteVariant(Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));
        try {
            productVariantRepository.delete(variant);
            productVariantRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Cannot delete variant '" + variant.getSku() + "' because it is linked to order items. Disable the variant instead.");
        }
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

    @Transactional
    public void createUser(String fullName, String email, String phone, String password, String role, boolean active) {
        String validName = validateFullName(fullName);
        String validEmail = validateEmail(email, null);
        String validPhone = validatePhone(phone);
        String validRole = validateRole(role);
        validatePassword(password, true);

        User user = new User();
        user.setFullName(validName);
        user.setEmail(validEmail);
        user.setPhone(validPhone);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(validRole);
        user.setActive(active);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Transactional
    public void updateUser(Long userId, String fullName, String email, String phone, String newPassword, String role, boolean active, String currentEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        String validName = validateFullName(fullName);
        String validEmail = validateEmail(email, userId);
        String validPhone = validatePhone(phone);
        String validRole = validateRole(role);

        // Safeguards for current logged-in admin account
        if (user.getEmail().equalsIgnoreCase(currentEmail)) {
            if (!active) {
                throw new IllegalArgumentException("You cannot disable your own logged-in admin account.");
            }
            if (!"ADMIN".equals(validRole)) {
                throw new IllegalArgumentException("You cannot demote your own account from ADMIN role.");
            }
        }

        user.setFullName(validName);
        user.setEmail(validEmail);
        user.setPhone(validPhone);
        user.setRole(validRole);
        user.setActive(active);

        if (newPassword != null && !newPassword.trim().isEmpty()) {
            validatePassword(newPassword, false);
            user.setPassword(passwordEncoder.encode(newPassword.trim()));
        }

        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long userId, String currentEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));

        if (user.getEmail().equalsIgnoreCase(currentEmail)) {
            throw new IllegalArgumentException("You cannot delete your own logged-in admin account.");
        }

        try {
            userRepository.delete(user);
            userRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Cannot delete user '" + user.getFullName() + "' because they have linked records (such as orders or addresses). Disable the account instead.");
        }
    }

    private String validateFullName(String fullName) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name is required.");
        }
        String clean = fullName.trim().replaceAll("\\s+", " ");
        if (clean.length() > 120) {
            throw new IllegalArgumentException("Full name must be 120 characters or fewer.");
        }
        return clean;
    }

    private String validateEmail(String email, Long currentUserId) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String cleanEmail = email.trim().toLowerCase(Locale.ROOT);
        if (cleanEmail.length() > 120) {
            throw new IllegalArgumentException("Email must be 120 characters or fewer.");
        }
        if (!cleanEmail.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$")) {
            throw new IllegalArgumentException("Please enter a valid email address (e.g. user@example.com).");
        }
        userRepository.findByEmail(cleanEmail).ifPresent(existing -> {
            if (currentUserId == null || !existing.getId().equals(currentUserId)) {
                throw new IllegalArgumentException("Email '" + cleanEmail + "' is already registered to another user.");
            }
        });
        return cleanEmail;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required.");
        }
        String clean = phone.trim().replaceAll("\\s+", " ");
        if (clean.length() > 20) {
            throw new IllegalArgumentException("Phone number must be 20 characters or fewer.");
        }
        if (!clean.matches("^[0-9+()\\-\\s]{8,20}$")) {
            throw new IllegalArgumentException("Please enter a valid phone number (8 to 20 digits, numbers and +()- spaces allowed).");
        }
        return clean;
    }

    private String validateRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            throw new IllegalArgumentException("Role is required.");
        }
        String cleanRole = role.trim().toUpperCase(Locale.ROOT);
        if (!"ADMIN".equals(cleanRole) && !"CUSTOMER".equals(cleanRole)) {
            throw new IllegalArgumentException("Role must be either ADMIN or CUSTOMER.");
        }
        return cleanRole;
    }

    private void validatePassword(String password, boolean required) {
        if (required && (password == null || password.trim().isEmpty())) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 8 || password.length() > 72) {
                throw new IllegalArgumentException("Password must be between 8 and 72 characters.");
            }
        }
    }

    @Transactional
    public void replaceVariantImage(Long variantId, MultipartFile imageFile) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found."));

        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Please select a valid image file.");
        }

        // Delete old image from Cloudinary
        if (variant.getImagePublicId() != null && !variant.getImagePublicId().isBlank()) {
            try {
                cloudinaryService.deleteImage(variant.getImagePublicId());
            } catch (Exception ignored) {
            }
        }

        // Upload new image
        String folderPath = "uminimalist/products/" + variant.getProduct().getSlug();
        CloudinaryService.UploadResult result = cloudinaryService.uploadImage(imageFile, folderPath);

        variant.setImageUrl(result.secureUrl());
        variant.setImagePublicId(result.publicId());

        productVariantRepository.save(variant);

        syncVariantImageToProductImagesAndAllVariants(variant.getProduct(), variant);
    }

    private void syncInheritedImageForColor(Product product, ProductVariant variant) {
        if (variant.getColor() == null || variant.getColor().isBlank()) return;
        String targetColor = variant.getColor().trim();

        // Check existing variants for the same color
        for (ProductVariant existing : product.getVariants()) {
            if (targetColor.equalsIgnoreCase(existing.getColor()) && existing.getImageUrl() != null && !existing.getImageUrl().isBlank()) {
                variant.setImageUrl(existing.getImageUrl());
                variant.setImagePublicId(existing.getImagePublicId());
                return;
            }
        }

        // Check product_images table for matching color
        for (ProductImage img : product.getImages()) {
            if (targetColor.equalsIgnoreCase(img.getColor()) && img.getImageUrl() != null && !img.getImageUrl().isBlank()) {
                variant.setImageUrl(img.getImageUrl());
                variant.setImagePublicId(img.getPublicId());
                return;
            }
        }
    }

    private void syncVariantImageToProductImagesAndAllVariants(Product product, ProductVariant sourceVariant) {
        if (sourceVariant.getImageUrl() == null || sourceVariant.getImageUrl().isBlank()) return;
        String color = sourceVariant.getColor();

        // 1. Sync all active variants with the same color to share this image
        for (ProductVariant v : product.getVariants()) {
            if (color != null && color.equalsIgnoreCase(v.getColor())) {
                v.setImageUrl(sourceVariant.getImageUrl());
                v.setImagePublicId(sourceVariant.getImagePublicId());
                productVariantRepository.save(v);
            }
        }

        // 2. Sync to product_images table
        boolean hasPrimary = product.getImages().stream().anyMatch(ProductImage::isPrimary);

        ProductImage existingImg = product.getImages().stream()
                .filter(img -> color != null && color.equalsIgnoreCase(img.getColor()))
                .findFirst()
                .orElse(null);

        if (existingImg != null) {
            existingImg.setImageUrl(sourceVariant.getImageUrl());
            existingImg.setPublicId(sourceVariant.getImagePublicId());
            productImageRepository.save(existingImg);
        } else {
            ProductImage newImg = new ProductImage();
            newImg.setProduct(product);
            newImg.setImageUrl(sourceVariant.getImageUrl());
            newImg.setPublicId(sourceVariant.getImagePublicId());
            newImg.setColor(color);
            newImg.setPrimary(!hasPrimary); // Make primary if product has no primary image yet
            newImg.setDisplayOrder(product.getImages().size() + 1);
            productImageRepository.save(newImg);
        }
    }

    @Transactional
    public ProductImage uploadProductImage(Long productId, MultipartFile file, String color, boolean isPrimary) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        String folderPath = "uminimalist/products/" + product.getSlug();
        CloudinaryService.UploadResult uploadResult = cloudinaryService.uploadImage(file, folderPath);

        boolean setPrimary = isPrimary || product.getImages().isEmpty();
        if (setPrimary) {
            for (ProductImage existing : product.getImages()) {
                existing.setPrimary(false);
            }
        }

        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(uploadResult.secureUrl());
        image.setPublicId(uploadResult.publicId());
        String cleanColor = color != null && !color.isBlank() ? color.trim() : null;
        image.setColor(cleanColor);
        image.setPrimary(setPrimary);
        image.setDisplayOrder(product.getImages().size() + 1);

        ProductImage savedImage = productImageRepository.save(image);

        if (cleanColor != null) {
            for (ProductVariant v : product.getVariants()) {
                if (cleanColor.equalsIgnoreCase(v.getColor())) {
                    v.setImageUrl(uploadResult.secureUrl());
                    v.setImagePublicId(uploadResult.publicId());
                    productVariantRepository.save(v);
                }
            }
        }

        return savedImage;
    }

    @Transactional
    public void setPrimaryProductImage(Long productId, Long imageId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        boolean found = false;
        for (ProductImage image : product.getImages()) {
            if (image.getId().equals(imageId)) {
                image.setPrimary(true);
                found = true;
            } else {
                image.setPrimary(false);
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Image not found for this product.");
        }
        productRepository.save(product);
    }

    @Transactional
    public void setPrimaryProductImageByUrl(Long productId, String imageUrl, String color) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found."));

        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("Invalid image URL.");
        }

        for (ProductImage existing : product.getImages()) {
            existing.setPrimary(false);
        }

        ProductImage targetImage = product.getImages().stream()
                .filter(img -> imageUrl.trim().equals(img.getImageUrl()))
                .findFirst()
                .orElse(null);

        if (targetImage != null) {
            targetImage.setPrimary(true);
            productImageRepository.save(targetImage);
        } else {
            ProductImage newPrimary = new ProductImage();
            newPrimary.setProduct(product);
            newPrimary.setImageUrl(imageUrl.trim());
            newPrimary.setColor(color != null && !color.isBlank() ? color.trim() : null);
            newPrimary.setPrimary(true);
            newPrimary.setDisplayOrder(1);
            productImageRepository.save(newPrimary);
        }
    }

    @Transactional
    public void deleteProductImage(Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Image not found."));

        if (image.getPublicId() != null && !image.getPublicId().isBlank()) {
            try {
                cloudinaryService.deleteImage(image.getPublicId());
            } catch (Exception ignored) {
            }
        }

        Product product = image.getProduct();
        boolean wasPrimary = image.isPrimary();
        product.getImages().remove(image);
        productImageRepository.delete(image);

        if (wasPrimary && !product.getImages().isEmpty()) {
            ProductImage firstRemaining = product.getImages().iterator().next();
            firstRemaining.setPrimary(true);
            productImageRepository.save(firstRemaining);
        }
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
