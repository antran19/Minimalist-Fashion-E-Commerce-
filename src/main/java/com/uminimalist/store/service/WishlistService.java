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
                        SELECT p.slug, p.name, p.product_type, p.base_price
                        FROM dbo.customer_wishlist w
                        JOIN dbo.products p ON p.id = w.product_id
                        WHERE w.user_id = ? AND p.active = 1
                        ORDER BY w.created_at DESC, w.id DESC
                        """,
                (rs, rowNum) -> new WishlistItemView(
                        rs.getString("slug"),
                        rs.getString("name"),
                        rs.getString("product_type"),
                        currencyFormat.format(rs.getBigDecimal("base_price") == null ? BigDecimal.ZERO : rs.getBigDecimal("base_price")),
                        imagePath(rs.getString("slug"))),
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
            throw new IllegalArgumentException("Product not found.");
        }
        return ids.get(0);
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
