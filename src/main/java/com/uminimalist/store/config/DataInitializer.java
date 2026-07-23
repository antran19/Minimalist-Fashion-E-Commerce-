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
        if (categoryRepository.count() == 0) {
            Category men = new Category();
            men.setSlug("men");
            men.setName("Men");
            men.setDescription("Everyday layers with clean lines.");
            men.setDisplayOrder(1);
            men = categoryRepository.save(men);

            Category women = new Category();
            women.setSlug("women");
            women.setName("Women");
            women.setDescription("Soft essentials for simple routines.");
            women.setDisplayOrder(2);
            women = categoryRepository.save(women);

            Category kids = new Category();
            kids.setSlug("kids");
            kids.setName("Kids");
            kids.setDescription("Easy pieces for school and weekends.");
            kids.setDisplayOrder(3);
            kids = categoryRepository.save(kids);

            createProduct(men, "air-cotton-tee", "Air Cotton Tee", "T-shirt", "Clean daily tee with a soft cotton handfeel.", new BigDecimal("19.90"), "product-tee", true, true,
                    new String[][]{{"AIR-COTTON-TEE-CREAM-S", "Cream", "S", "12"}, {"AIR-COTTON-TEE-CREAM-M", "Cream", "M", "13"}, {"AIR-COTTON-TEE-WHITE-L", "White", "L", "11"}, {"AIR-COTTON-TEE-WHITE-XL", "White", "XL", "12"}});

            createProduct(men, "light-utility-jacket", "Light Utility Jacket", "Outerwear", "A light layer for simple weekday outfits.", new BigDecimal("59.90"), "product-jacket", true, false,
                    new String[][]{{"LIGHT-UTILITY-JACKET-NAVY-S", "Navy", "S", "6"}, {"LIGHT-UTILITY-JACKET-NAVY-M", "Navy", "M", "7"}, {"LIGHT-UTILITY-JACKET-NAVY-L", "Navy", "L", "5"}});

            createProduct(men, "oxford-shirt", "Oxford Shirt", "Shirt", "Crisp button-down shirt for work and weekends.", new BigDecimal("34.90"), "product-shirt", false, true,
                    new String[][]{{"OXFORD-SHIRT-WHITE-S", "White", "S", "7"}, {"OXFORD-SHIRT-WHITE-M", "White", "M", "8"}, {"OXFORD-SHIRT-WHITE-L", "White", "L", "8"}, {"OXFORD-SHIRT-WHITE-XL", "White", "XL", "8"}});

            createProduct(women, "soft-jersey-tee", "Soft Jersey Tee", "T-shirt", "Easy jersey tee with a relaxed everyday shape.", new BigDecimal("19.90"), "product-sage", true, false,
                    new String[][]{{"SOFT-JERSEY-TEE-SAGE-XS", "Sage", "XS", "10"}, {"SOFT-JERSEY-TEE-SAGE-S", "Sage", "S", "11"}, {"SOFT-JERSEY-TEE-SAGE-M", "Sage", "M", "12"}, {"SOFT-JERSEY-TEE-SAGE-L", "Sage", "L", "9"}});

            createProduct(women, "smart-ankle-pants", "Smart Ankle Pants", "Pants", "Straight ankle pants for repeat outfits.", new BigDecimal("39.90"), "product-pants", false, true,
                    new String[][]{{"SMART-ANKLE-PANTS-BLACK-XS", "Black", "XS", "5"}, {"SMART-ANKLE-PANTS-BLACK-S", "Black", "S", "5"}, {"SMART-ANKLE-PANTS-BLACK-M", "Black", "M", "6"}, {"SMART-ANKLE-PANTS-BLACK-L", "Black", "L", "5"}, {"SMART-ANKLE-PANTS-BLACK-XL", "Black", "XL", "5"}});

            createProduct(women, "everyday-zip-hoodie", "Everyday Zip Hoodie", "Sweatshirt", "A clean zip hoodie for cool mornings.", new BigDecimal("49.90"), "product-hoodie", true, false,
                    new String[][]{{"EVERYDAY-ZIP-HOODIE-GREY-S", "Grey", "S", "4"}, {"EVERYDAY-ZIP-HOODIE-GREY-M", "Grey", "M", "4"}, {"EVERYDAY-ZIP-HOODIE-GREY-L", "Grey", "L", "4"}, {"EVERYDAY-ZIP-HOODIE-GREY-XL", "Grey", "XL", "3"}});

            createProduct(men, "linen-blend-shirt", "Linen Blend Shirt", "Shirt", "Light linen blend shirt with natural texture.", new BigDecimal("34.90"), "product-linen", false, false,
                    new String[][]{{"LINEN-BLEND-SHIRT-NATURAL-S", "Natural", "S", "5"}, {"LINEN-BLEND-SHIRT-NATURAL-M", "Natural", "M", "6"}, {"LINEN-BLEND-SHIRT-NATURAL-L", "Natural", "L", "6"}, {"LINEN-BLEND-SHIRT-NATURAL-XL", "Natural", "XL", "5"}});

            createProduct(women, "utility-tote", "Utility Tote", "Accessories", "A compact tote for daily carry.", new BigDecimal("14.90"), "product-tote", true, true,
                    new String[][]{{"UTILITY-TOTE-RED-ONE-SIZE", "Red", "One size", "64"}});

            createProduct(kids, "school-day-cardigan", "School Day Cardigan", "Knitwear", "Soft cardigan for school and weekends.", new BigDecimal("29.90"), "product-kids-cardigan", true, false,
                    new String[][]{{"SCHOOL-DAY-CARDIGAN-BLUE-110", "Blue", "110", "8"}, {"SCHOOL-DAY-CARDIGAN-BLUE-120", "Blue", "120", "9"}, {"SCHOOL-DAY-CARDIGAN-GREY-130", "Grey", "130", "9"}, {"SCHOOL-DAY-CARDIGAN-GREY-140", "Grey", "140", "8"}});

            createProduct(kids, "easy-cotton-shorts", "Easy Cotton Shorts", "Shorts", "Cotton shorts for warm days and active routines.", new BigDecimal("16.90"), "product-kids-shorts", false, true,
                    new String[][]{{"EASY-COTTON-SHORTS-KHAKI-110", "Khaki", "110", "7"}, {"EASY-COTTON-SHORTS-KHAKI-120", "Khaki", "120", "7"}, {"EASY-COTTON-SHORTS-KHAKI-130", "Khaki", "130", "7"}, {"EASY-COTTON-SHORTS-KHAKI-140", "Khaki", "140", "7"}});
        }
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
            product.getVariants().add(variant);
        }
        productRepository.save(product);
    }
}

