package com.uminimalist.store.service;

import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.ReviewView;
import com.uminimalist.store.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class ProductReviewService {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    public ProductReviewService(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void ensureReviewTable() {
        jdbcTemplate.execute("""
                IF OBJECT_ID(N'dbo.product_reviews', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.product_reviews (
                        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                        product_id BIGINT NOT NULL FOREIGN KEY REFERENCES dbo.products(id),
                        user_id BIGINT NOT NULL FOREIGN KEY REFERENCES dbo.users(id),
                        rating INT NOT NULL,
                        comment NVARCHAR(1000) NULL,
                        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
                    );
                END
                """);
    }

    public record ProductRatingStats(double averageRating, int reviewCount, String averageRatingLabel) {}

    public ProductRatingStats getStatsForProduct(String productSlug) {
        ensureReviewTable();
        List<Integer> ratings = jdbcTemplate.query("""
                SELECT r.rating
                FROM dbo.product_reviews r
                JOIN dbo.products p ON p.id = r.product_id
                WHERE p.slug = ? AND p.active = 1
                """, (rs, rowNum) -> rs.getInt("rating"), productSlug);
        if (ratings.isEmpty()) {
            return new ProductRatingStats(0.0, 0, "0.0");
        }
        double avg = ratings.stream().mapToInt(Integer::intValue).average().orElse(0.0);
        return new ProductRatingStats(avg, ratings.size(), String.format(Locale.US, "%.1f", avg));
    }

    public List<ReviewView> findReviewsForProduct(String productSlug) {
        ensureReviewTable();
        return jdbcTemplate.query("""
                SELECT u.full_name, r.rating, r.comment, r.created_at
                FROM dbo.product_reviews r
                JOIN dbo.products p ON p.id = r.product_id
                JOIN dbo.users u ON u.id = r.user_id
                WHERE p.slug = ? AND p.active = 1
                ORDER BY r.created_at DESC
                """, (rs, rowNum) -> new ReviewView(
                        rs.getString("full_name"),
                        rs.getInt("rating"),
                        rs.getString("comment"),
                        rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toLocalDateTime()
                ), productSlug);
    }

    public boolean hasUserReviewed(String email, String productSlug) {
        ensureReviewTable();
        if (email == null || email.isBlank()) {
            return false;
        }
        Integer reviewCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.product_reviews r
                JOIN dbo.products p ON p.id = r.product_id
                JOIN dbo.users u ON u.id = r.user_id
                WHERE u.email = ? AND p.slug = ?
                """, Integer.class, email, productSlug);
        return reviewCount != null && reviewCount > 0;
    }

    public String getReviewEligibilityStatus(String email, String productSlug) {
        ensureReviewTable();
        if (email == null || email.isBlank()) {
            return "NOT_PURCHASED";
        }

        if (hasUserReviewed(email, productSlug)) {
            return "ALREADY_REVIEWED";
        }

        Integer deliveredCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.orders o
                JOIN dbo.order_items oi ON o.id = oi.order_id
                JOIN dbo.product_variants pv ON pv.id = oi.product_variant_id
                JOIN dbo.products p ON p.id = pv.product_id
                WHERE o.customer_email = ? AND p.slug = ?
                  AND o.status = 'DELIVERED'
                """, Integer.class, email, productSlug);

        if (deliveredCount != null && deliveredCount > 0) {
            return "CAN_REVIEW";
        }

        Integer totalPurchasedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM dbo.orders o
                JOIN dbo.order_items oi ON o.id = oi.order_id
                JOIN dbo.product_variants pv ON pv.id = oi.product_variant_id
                JOIN dbo.products p ON p.id = pv.product_id
                WHERE o.customer_email = ? AND p.slug = ?
                  AND o.status NOT IN ('CANCELLED', 'PENDING_PAYMENT')
                """, Integer.class, email, productSlug);

        if (totalPurchasedCount != null && totalPurchasedCount > 0) {
            return "NOT_DELIVERED";
        }

        return "NOT_PURCHASED";
    }

    public boolean canUserReview(String email, String productSlug) {
        return "CAN_REVIEW".equals(getReviewEligibilityStatus(email, productSlug));
    }

    @Transactional
    public void addReview(String email, String productSlug, int rating, String comment) {
        ensureReviewTable();
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        String normalizedComment = comment == null ? "" : comment.trim();
        if (normalizedComment.length() < 3 || normalizedComment.length() > 1000) {
            throw new IllegalArgumentException("Review comment must be between 3 and 1000 characters.");
        }
        if (!canUserReview(email, productSlug)) {
            throw new IllegalArgumentException("You can only review products you have purchased and had delivered, and haven't reviewed yet.");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found."));
        
        Long productId = jdbcTemplate.queryForObject("""
                SELECT id FROM dbo.products WHERE slug = ? AND active = 1
                """, Long.class, productSlug);
        
        if (productId == null) {
            throw new IllegalArgumentException("Product not found.");
        }

        // Check if user already reviewed this product to avoid duplicates
        Integer existingCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM dbo.product_reviews WHERE user_id = ? AND product_id = ?
                """, Integer.class, user.getId(), productId);

        if (existingCount != null && existingCount > 0) {
            throw new IllegalArgumentException("You have already reviewed this product.");
        }

        jdbcTemplate.update("""
                INSERT INTO dbo.product_reviews (product_id, user_id, rating, comment)
                VALUES (?, ?, ?, ?)
                """, productId, user.getId(), rating, normalizedComment);
    }
}
