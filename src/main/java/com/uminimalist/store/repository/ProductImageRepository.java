package com.uminimalist.store.repository;

import com.uminimalist.store.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    List<ProductImage> findByProductIdOrderByDisplayOrderAscIdAsc(Long productId);

    List<ProductImage> findByProductIdAndColorIgnoreCaseOrderByDisplayOrderAscIdAsc(Long productId, String color);

    Optional<ProductImage> findFirstByProductIdAndIsPrimaryTrue(Long productId);

    Optional<ProductImage> findFirstByProductIdOrderByDisplayOrderAscIdAsc(Long productId);
}
