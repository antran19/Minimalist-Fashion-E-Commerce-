package com.uminimalist.store.config;

import com.uminimalist.store.entity.Category;
import com.uminimalist.store.entity.Product;
import com.uminimalist.store.entity.ProductVariant;
import com.uminimalist.store.entity.User;
import com.uminimalist.store.repository.CategoryRepository;
import com.uminimalist.store.repository.ProductRepository;
import com.uminimalist.store.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository,
                           CategoryRepository categoryRepository,
                           ProductRepository productRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Seed Admin
        if (userRepository.findByEmail("admin@uminimalist.com").isEmpty()) {
            User admin = new User(
                    "admin@uminimalist.com",
                    passwordEncoder.encode("admin123"),
                    "Store Admin",
                    "0123456789",
                    "ADMIN");
            userRepository.save(admin);
        }

        // Seed Customer
        if (userRepository.findByEmail("customer@uminimalist.com").isEmpty()) {
            User customer = new User(
                    "customer@uminimalist.com",
                    passwordEncoder.encode("customer123"),
                    "John Doe",
                    "0987654321",
                    "CUSTOMER");
            userRepository.save(customer);
        }

        // Seed Catalog
        if (productRepository.count() == 0) {
            Category men = categoryRepository.findBySlug("men").orElseGet(() -> {
                Category c = new Category();
                c.setSlug("men");
                c.setName("Men");
                c.setDescription("Everyday layers with clean lines.");
                c.setDisplayOrder(1);
                return categoryRepository.save(c);
            });

            Category women = categoryRepository.findBySlug("women").orElseGet(() -> {
                Category c = new Category();
                c.setSlug("women");
                c.setName("Women");
                c.setDescription("Soft essentials for simple routines.");
                c.setDisplayOrder(2);
                return categoryRepository.save(c);
            });

            Category kids = categoryRepository.findBySlug("kids").orElseGet(() -> {
                Category c = new Category();
                c.setSlug("kids");
                c.setName("Kids");
                c.setDescription("Easy pieces for school and weekends.");
                c.setDisplayOrder(3);
                return categoryRepository.save(c);
            });

            createProduct(men, "air-cotton-tee", "Air Cotton Tee", "T-shirt", "Clean daily tee with a soft cotton handfeel.", new BigDecimal("19.90"), "product-tee", true, true,
                    new String[][]{{"AIR-COTTON-TEE-CREAM-S", "Cream", "S", "12"}, {"AIR-COTTON-TEE-CREAM-M", "Cream", "M", "13"}, {"AIR-COTTON-TEE-WHITE-L", "White", "L", "11"}, {"AIR-COTTON-TEE-WHITE-XL", "White", "XL", "12"}});

            createProduct(men, "light-utility-jacket", "Light Utility Jacket", "Outerwear", "A light layer for simple weekday outfits.", new BigDecimal("59.90"), "product-jacket", true, false,
                    new String[][]{{"LIGHT-UTILITY-JACKET-NAVY-S", "Navy", "S", "6"}, {"LIGHT-UTILITY-JACKET-NAVY-M", "Navy", "M", "7"}, {"LIGHT-UTILITY-JACKET-NAVY-L", "Navy", "L", "5"}});

            createProduct(men, "oxford-shirt", "Oxford Shirt", "Shirt", "Crisp button-down shirt for work and weekends.", new BigDecimal("34.90"), "product-shirt", false, true,
                    new String[][]{{"OXFORD-SHIRT-WHITE-S", "White", "S", "7"}, {"OXFORD-SHIRT-WHITE-M", "White", "M", "8"}, {"OXFORD-SHIRT-WHITE-L", "White", "L", "8"}, {"OXFORD-SHIRT-WHITE-XL", "White", "XL", "8"}});

            createProduct(women, "soft-jersey-tee", "Soft Jersey Tee", "T-shirt", "Easy jersey tee with a relaxed everyday shape.", new BigDecimal("19.90"), "product-sage", true, false,
                    new String[][]{{"SOFT-JERSEY-TEE-RED-XS", "Red", "XS", "10"}, {"SOFT-JERSEY-TEE-WHITE-S", "White", "S", "11"}, {"SOFT-JERSEY-TEE-BROWN-M", "Brown", "M", "12"}, {"SOFT-JERSEY-TEE-RED-L", "Red", "L", "9"}});

            createProduct(women, "smart-ankle-pants", "Smart Ankle Pants", "Pants", "Straight ankle pants for repeat outfits.", new BigDecimal("39.90"), "product-pants", false, true,
                    new String[][]{{"SMART-ANKLE-PANTS-BLACK-XS", "Black", "XS", "5"}, {"SMART-ANKLE-PANTS-BROWN-S", "Brown", "S", "5"}, {"SMART-ANKLE-PANTS-WHITE-M", "White", "M", "6"}, {"SMART-ANKLE-PANTS-BLACK-L", "Black", "L", "5"}, {"SMART-ANKLE-PANTS-BLACK-XL", "Black", "XL", "5"}});

            createProduct(women, "everyday-zip-hoodie", "Everyday Zip Hoodie", "Sweatshirt", "A clean zip hoodie for cool mornings.", new BigDecimal("49.90"), "product-hoodie", true, false,
                    new String[][]{{"EVERYDAY-ZIP-HOODIE-BLACK-S", "Black", "S", "4"}, {"EVERYDAY-ZIP-HOODIE-CREAM-M", "Cream", "M", "4"}, {"EVERYDAY-ZIP-HOODIE-RED-L", "Red", "L", "4"}, {"EVERYDAY-ZIP-HOODIE-BLACK-XL", "Black", "XL", "3"}});

            createProduct(men, "linen-blend-shirt", "Linen Blend Shirt", "Shirt", "Light linen blend shirt with natural texture.", new BigDecimal("34.90"), "product-linen", false, false,
                    new String[][]{{"LINEN-BLEND-SHIRT-ORANGE-S", "Orange", "S", "5"}, {"LINEN-BLEND-SHIRT-WHITE-M", "White", "M", "6"}, {"LINEN-BLEND-SHIRT-CREAM-L", "Cream", "L", "6"}, {"LINEN-BLEND-SHIRT-ORANGE-XL", "Orange", "XL", "5"}});

            createProduct(women, "utility-tote", "Utility Tote", "Accessories", "A compact tote for daily carry.", new BigDecimal("14.90"), "product-tote", true, true,
                    new String[][]{{"UTILITY-TOTE-RED-ONE-SIZE", "Red", "One size", "64"}});

            createProduct(kids, "school-day-cardigan", "School Day Cardigan", "Knitwear", "Soft cardigan for school and weekends.", new BigDecimal("29.90"), "product-kids-cardigan", true, false,
                    new String[][]{{"SCHOOL-DAY-CARDIGAN-BLACK-110", "Black", "110", "8"}, {"SCHOOL-DAY-CARDIGAN-DARKGREEN-120", "Dark Green", "120", "9"}, {"SCHOOL-DAY-CARDIGAN-NAVY-130", "Navy", "130", "9"}, {"SCHOOL-DAY-CARDIGAN-BLACK-140", "Black", "140", "8"}});

            createProduct(kids, "easy-cotton-shorts", "Easy Cotton Shorts", "Shorts", "Cotton shorts for warm days and active routines.", new BigDecimal("16.90"), "product-kids-shorts", false, true,
                    new String[][]{{"EASY-COTTON-SHORTS-BLUE-110", "Blue", "110", "7"}, {"EASY-COTTON-SHORTS-GRAY-120", "Gray", "120", "7"}, {"EASY-COTTON-SHORTS-BLUE-130", "Blue", "130", "7"}, {"EASY-COTTON-SHORTS-GRAY-140", "Gray", "140", "7"}});
        }

        // Ensure every product in DB has rich color and full size variants (XS, S, M, L, XL, 2XL, 110, 120, 130, 140)
        for (Product product : productRepository.findAll()) {
            String slug = product.getSlug();

            if (slug.contains("cardigan")) {
                addVariant(product, slug.toUpperCase() + "-BLACK-110", "Black", "110", 15);
                addVariant(product, slug.toUpperCase() + "-DARKGREEN-120", "Dark Green", "120", 18);
                addVariant(product, slug.toUpperCase() + "-NAVY-130", "Navy", "130", 12);
                addVariant(product, slug.toUpperCase() + "-BLACK-140", "Black", "140", 16);
            } else if (slug.contains("tote")) {
                addVariant(product, slug.toUpperCase() + "-PINK-ONE", "Pink", "One size", 25);
                addVariant(product, slug.toUpperCase() + "-RED-ONE", "Red", "One size", 30);
                addVariant(product, slug.toUpperCase() + "-YELLOW-ONE", "Yellow", "One size", 20);
            } else if (slug.contains("hoodie")) {
                addVariant(product, slug.toUpperCase() + "-BLACK-XS", "Black", "XS", 12);
                addVariant(product, slug.toUpperCase() + "-BLACK-S", "Black", "S", 15);
                addVariant(product, slug.toUpperCase() + "-CREAM-M", "Cream", "M", 18);
                addVariant(product, slug.toUpperCase() + "-RED-L", "Red", "L", 12);
                addVariant(product, slug.toUpperCase() + "-CREAM-XL", "Cream", "XL", 14);
                addVariant(product, slug.toUpperCase() + "-BLACK-2XL", "Black", "2XL", 10);
            } else if (slug.contains("jersey") || slug.contains("tee")) {
                addVariant(product, slug.toUpperCase() + "-WHITE-XS", "White", "XS", 12);
                addVariant(product, slug.toUpperCase() + "-BROWN-S", "Brown", "S", 15);
                addVariant(product, slug.toUpperCase() + "-RED-M", "Red", "M", 20);
                addVariant(product, slug.toUpperCase() + "-WHITE-L", "White", "L", 18);
                addVariant(product, slug.toUpperCase() + "-LIGHTBLUE-XL", "Light Blue", "XL", 16);
                addVariant(product, slug.toUpperCase() + "-PINK-2XL", "Pink", "2XL", 10);
            } else if (slug.contains("shorts")) {
                addVariant(product, slug.toUpperCase() + "-BLUE-110", "Blue", "110", 15);
                addVariant(product, slug.toUpperCase() + "-GRAY-120", "Gray", "120", 18);
                addVariant(product, slug.toUpperCase() + "-BLUE-130", "Blue", "130", 14);
                addVariant(product, slug.toUpperCase() + "-GRAY-140", "Gray", "140", 16);
            } else if (slug.contains("pants")) {
                addVariant(product, slug.toUpperCase() + "-BLACK-XS", "Black", "XS", 12);
                addVariant(product, slug.toUpperCase() + "-BLACK-S", "Black", "S", 15);
                addVariant(product, slug.toUpperCase() + "-BROWN-M", "Brown", "M", 18);
                addVariant(product, slug.toUpperCase() + "-WHITE-L", "White", "L", 12);
                addVariant(product, slug.toUpperCase() + "-BLACK-XL", "Black", "XL", 15);
                addVariant(product, slug.toUpperCase() + "-BROWN-2XL", "Brown", "2XL", 10);
            } else if (slug.contains("jacket")) {
                addVariant(product, slug.toUpperCase() + "-GRAY-XS", "Gray", "XS", 10);
                addVariant(product, slug.toUpperCase() + "-GRAY-S", "Gray", "S", 12);
                addVariant(product, slug.toUpperCase() + "-NAVY-M", "Navy", "M", 15);
                addVariant(product, slug.toUpperCase() + "-NAVY-L", "Navy", "L", 14);
                addVariant(product, slug.toUpperCase() + "-GRAY-XL", "Gray", "XL", 16);
            } else if (slug.contains("shirt")) {
                addVariant(product, slug.toUpperCase() + "-CREAM-XS", "Cream", "XS", 10);
                addVariant(product, slug.toUpperCase() + "-CREAM-S", "Cream", "S", 15);
                addVariant(product, slug.toUpperCase() + "-ORANGE-M", "Orange", "M", 18);
                addVariant(product, slug.toUpperCase() + "-WHITE-L", "White", "L", 20);
                addVariant(product, slug.toUpperCase() + "-WHITE-XL", "White", "XL", 15);
                addVariant(product, slug.toUpperCase() + "-BROWN-2XL", "Brown", "2XL", 10);
            } else {
                addVariant(product, slug.toUpperCase() + "-BLACK-XS", "Black", "XS", 10);
                addVariant(product, slug.toUpperCase() + "-BLACK-S", "Black", "S", 12);
                addVariant(product, slug.toUpperCase() + "-BLACK-M", "Black", "M", 15);
                addVariant(product, slug.toUpperCase() + "-WHITE-L", "White", "L", 20);
                addVariant(product, slug.toUpperCase() + "-NAVY-XL", "Navy", "XL", 12);
                addVariant(product, slug.toUpperCase() + "-NAVY-2XL", "Navy", "2XL", 10);
            }

            for (ProductVariant v : product.getVariants()) {
                if (v.getStockQuantity() <= 0) {
                    v.setStockQuantity(15);
                    v.setActive(true);
                }
                if (v.getImageUrl() == null || v.getImageUrl().isBlank()) {
                    v.setImageUrl(resolveColorImagePath(slug, v.getColor()));
                }
            }
            productRepository.save(product);
        }
    }

    private void addVariant(Product product, String sku, String color, String size, int stock) {
        boolean exists = product.getVariants().stream()
                .anyMatch(v -> v.getSku() != null && v.getSku().equalsIgnoreCase(sku));
        if (!exists) {
            ProductVariant v = new ProductVariant();
            v.setProduct(product);
            v.setSku(sku);
            v.setColor(color);
            v.setSize(size);
            v.setStockQuantity(stock);
            v.setActive(true);
            v.setImageUrl(resolveColorImagePath(product.getSlug(), color));
            product.getVariants().add(v);
        }
    }

    private String resolveColorImagePath(String slug, String color) {
        if (slug == null || color == null) return "/images/product-collage.png";
        String normalizedColor = color.trim().toLowerCase(java.util.Locale.ROOT).replace(" ", "-");

        return switch (slug) {
            case "air-cotton-tee" -> switch (normalizedColor) {
                case "light-blue" -> "/images/products/air-cotton-tee-light-blue.png";
                case "pink" -> "/images/products/air-cotton-tee-pink.png";
                case "red" -> "/images/products/soft-jersey-tee-red.jpg";
                case "white" -> "/images/products/soft-jersey-tee-white.jpg";
                case "brown" -> "/images/products/soft-jersey-tee-brown.png";
                default -> "/images/products/air-cotton-tee-cream.png";
            };
            case "light-utility-jacket" -> switch (normalizedColor) {
                case "navy", "blue" -> "/images/products/light-utility-jacket-navy.png";
                default -> "/images/products/light-utility-jacket-gray.png";
            };
            case "oxford-shirt" -> switch (normalizedColor) {
                case "light-blue" -> "/images/products/oxford-shirt-light-blue.jpg";
                case "white" -> "/images/products/oxford-shirt-white.png";
                default -> "/images/products/oxford-shirt-brown.png";
            };
            case "soft-jersey-tee" -> switch (normalizedColor) {
                case "red" -> "/images/products/soft-jersey-tee-red.jpg";
                case "white" -> "/images/products/soft-jersey-tee-white.jpg";
                case "pink" -> "/images/products/air-cotton-tee-pink.png";
                case "light-blue" -> "/images/products/air-cotton-tee-light-blue.png";
                case "sage", "grey", "gray" -> "/images/products/air-cotton-tee-cream.png";
                default -> "/images/products/soft-jersey-tee-brown.png";
            };
            case "smart-ankle-pants" -> switch (normalizedColor) {
                case "brown" -> "/images/products/smart-ankle-pants-brown.jpg";
                case "white" -> "/images/products/smart-ankle-pants-white.jpg";
                default -> "/images/products/smart-ankle-pants-black.png";
            };
            case "everyday-zip-hoodie" -> switch (normalizedColor) {
                case "cream" -> "/images/products/everyday-zip-hoodie-cream.jpg";
                case "red" -> "/images/products/everyday-zip-hoodie-red.jpg";
                case "yellow" -> "/images/products/utility-tote-yellow.jpg";
                default -> "/images/products/everyday-zip-hoodie-black.png";
            };
            case "linen-blend-shirt" -> switch (normalizedColor) {
                case "orange" -> "/images/products/linen-blend-shirt-orange.jpg";
                case "white" -> "/images/products/linen-blend-shirt-white.jpg";
                default -> "/images/products/linen-blend-shirt-cream.png";
            };
            case "utility-tote" -> switch (normalizedColor) {
                case "red" -> "/images/products/utility-tote-red.png";
                case "yellow" -> "/images/products/utility-tote-yellow.jpg";
                default -> "/images/products/utility-tote-pink.jpg";
            };
            case "school-day-cardigan" -> switch (normalizedColor) {
                case "dark-green" -> "/images/products/school-day-cardigan-dark-green.jpg";
                case "navy", "blue" -> "/images/products/school-day-cardigan-navy.png";
                default -> "/images/products/school-day-cardigan-black.jpg";
            };
            case "easy-cotton-shorts" -> switch (normalizedColor) {
                case "gray", "grey" -> "/images/products/easy-cotton-shorts-gray.png";
                default -> "/images/products/easy-cotton-shorts-blue.png";
            };
            default -> "/images/product-collage.png";
        };
    }

    private void createProduct(Category category, String slug, String name, String productType, String description,
                               BigDecimal basePrice, String cropClass, boolean newArrival, boolean bestSeller,
                               String[][] variantData) {
        Product product = new Product();
        product.setCategory(category);
        product.setSlug(slug);
        product.setName(name);
        product.setProductType(productType);
        product.setDescription(description);
        product.setBasePrice(basePrice);
        product.setCropClass(cropClass);
        product.setNewArrival(newArrival);
        product.setBestSeller(bestSeller);
        product.setActive(true);
        product.setCreatedAt(LocalDateTime.now());

        for (String[] v : variantData) {
            ProductVariant variant = new ProductVariant();
            variant.setProduct(product);
            variant.setSku(v[0]);
            variant.setColor(v[1]);
            variant.setSize(v[2]);
            variant.setStockQuantity(Integer.parseInt(v[3]));
            variant.setActive(true);
            variant.setImageUrl(resolveColorImagePath(slug, v[1]));
            product.getVariants().add(variant);
        }
        productRepository.save(product);
    }
}

