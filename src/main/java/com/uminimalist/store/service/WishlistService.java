package com.uminimalist.store.service;

import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.WishlistItemView;
import com.uminimalist.store.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@Service
public class WishlistService {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public WishlistService(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void ensureWishlistTable() {
        jdbcTemplate.execute("""
                IF OBJECT_ID(N'dbo.customer_wishlist', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.customer_wishlist (
                        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        product_id BIGINT NOT NULL,
                        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
                    );
                END
                """);
    }

    @Transactional
    public void add(String customerEmail, String productSlug) {
        ensureWishlistTable();
        User user = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found."));
        Long productId = findProductId(productSlug);

        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.customer_wishlist
                WHERE user_id = ? AND product_id = ?
                """, Integer.class, user.getId(), productId);

        if (count == null || count == 0) {
            jdbcTemplate.update("""
                    INSERT INTO dbo.customer_wishlist (user_id, product_id)
                    VALUES (?, ?)
                    """, user.getId(), productId);
        }
    }

    @Transactional
    public void remove(String customerEmail, String productSlug) {
        ensureWishlistTable();
        User user = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found."));
        Long productId = findProductId(productSlug);
        jdbcTemplate.update("""
                DELETE FROM dbo.customer_wishlist
                WHERE user_id = ? AND product_id = ?
                """, user.getId(), productId);
    }

    public boolean contains(String customerEmail, String productSlug) {
        ensureWishlistTable();
        return userRepository.findByEmail(customerEmail)
                .map(user -> {
                    Integer count = jdbcTemplate.queryForObject("""
                            SELECT COUNT(*)
                            FROM dbo.customer_wishlist w
                            JOIN dbo.products p ON p.id = w.product_id
                            WHERE w.user_id = ? AND p.slug = ?
                            """, Integer.class, user.getId(), productSlug);
                    return count != null && count > 0;
                })
                .orElse(false);
    }

    public List<WishlistItemView> findForCustomer(String customerEmail) {
        ensureWishlistTable();
        User user = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found."));
        return jdbcTemplate.query("""
                        SELECT p.slug, p.name, p.product_type, p.base_price, p.on_sale, p.discount_percentage,
                               (SELECT TOP 1 pv.image_url
                                FROM dbo.product_variants pv
                                WHERE pv.product_id = p.id AND pv.active = 1 AND pv.image_url IS NOT NULL
                                ORDER BY pv.id ASC) AS variant_image_url
                        FROM dbo.customer_wishlist w
                        JOIN dbo.products p ON p.id = w.product_id
                        WHERE w.user_id = ? AND p.active = 1
                        ORDER BY w.created_at DESC, w.id DESC
                        """,
                (rs, rowNum) -> {
                    String variantImageUrl = rs.getString("variant_image_url");
                    String imgPath;
                    if (variantImageUrl != null && !variantImageUrl.isBlank()) {
                        imgPath = variantImageUrl;
                    } else {
                        imgPath = imagePath(rs.getString("slug"));
                    }
                    
                    boolean onSale = rs.getBoolean("on_sale");
                    int discountPercentage = rs.getInt("discount_percentage");
                    BigDecimal basePrice = rs.getBigDecimal("base_price");
                    if (basePrice == null) basePrice = BigDecimal.ZERO;
                    
                    BigDecimal salePrice = basePrice;
                    if (onSale && discountPercentage > 0) {
                        salePrice = basePrice.multiply(BigDecimal.valueOf(100 - discountPercentage))
                                .divide(BigDecimal.valueOf(100), 2, java.math.RoundingMode.HALF_UP);
                    }
                    
                    return new WishlistItemView(
                            rs.getString("slug"),
                            rs.getString("name"),
                            rs.getString("product_type"),
                            currencyFormat.format(basePrice),
                            imgPath,
                            onSale,
                            discountPercentage,
                            currencyFormat.format(salePrice));
                },
                user.getId());
    }

    private Long findProductId(String productSlug) {
        List<Long> ids = jdbcTemplate.query("""
                        SELECT id
                        FROM dbo.products
                        WHERE slug = ? AND active = 1
                        """,
                (rs, rowNum) -> rs.getLong("id"),
                productSlug);
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("This product is no longer available.");
        }
        return ids.get(0);
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
