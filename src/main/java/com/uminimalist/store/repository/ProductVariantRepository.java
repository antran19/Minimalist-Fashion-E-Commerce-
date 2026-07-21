package com.uminimalist.store.repository;

import com.uminimalist.store.entity.ProductVariant;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    @EntityGraph(attributePaths = {"product", "product.category"})
    Optional<ProductVariant> findFirstByProductSlugAndColorIgnoreCaseAndSizeIgnoreCaseAndActiveTrueAndProductActiveTrue(
            String productSlug,
            String color,
            String size
    );

    @EntityGraph(attributePaths = {"product", "product.category"})
    List<ProductVariant> findBySkuInAndActiveTrueAndProductActiveTrue(Collection<String> skus);

    Optional<ProductVariant> findBySkuIgnoreCase(String sku);
}
