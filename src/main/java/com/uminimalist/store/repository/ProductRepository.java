package com.uminimalist.store.repository;

import com.uminimalist.store.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "variants", "images"})
    @Query("select distinct p from Product p where p.active = true")
    List<Product> findByActiveTrue();

    @EntityGraph(attributePaths = {"category", "variants", "images"})
    @Query("select distinct p from Product p")
    List<Product> findAllWithCategoryAndVariants();

    @EntityGraph(attributePaths = {"category", "variants", "images"})
    @Query("select p from Product p")
    Page<Product> findAllPaged(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "variants", "images"})
    @Query("select p from Product p where p.active = :active")
    Page<Product> findByActivePaged(@Param("active") boolean active, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "variants", "images"})
    @Query("select p from Product p where lower(p.name) like lower(concat('%', :query, '%')) or lower(p.productType) like lower(concat('%', :query, '%'))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    @EntityGraph(attributePaths = {"category", "variants", "images"})
    @Query("select distinct p from Product p join p.variants v where v.stockQuantity <= 5")
    Page<Product> findByLowStockPaged(Pageable pageable);

    @EntityGraph(attributePaths = {"category", "variants", "images"})
    Optional<Product> findBySlugAndActiveTrue(String slug);

    @Query("select distinct p.productType from Product p order by p.productType")
    List<String> findDistinctProductTypes();
}
