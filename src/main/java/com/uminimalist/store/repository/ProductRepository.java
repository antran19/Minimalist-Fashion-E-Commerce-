package com.uminimalist.store.repository;

import com.uminimalist.store.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @EntityGraph(attributePaths = {"category", "variants"})
    @Query("select distinct p from Product p where p.active = true")
    List<Product> findByActiveTrue();

    @EntityGraph(attributePaths = {"category", "variants"})
    @Query("select distinct p from Product p")
    List<Product> findAllWithCategoryAndVariants();

    @EntityGraph(attributePaths = {"category", "variants"})
    Optional<Product> findBySlugAndActiveTrue(String slug);

    @Query("select distinct p.productType from Product p order by p.productType")
    List<String> findDistinctProductTypes();
}
