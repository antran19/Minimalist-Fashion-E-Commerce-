package com.uminimalist.store.repository;

import com.uminimalist.store.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByActiveTrueOrderByDisplayOrderAscNameAsc();

    List<Category> findAllByOrderByDisplayOrderAscNameAsc();

    Page<Category> findAllByOrderByDisplayOrderAscNameAsc(Pageable pageable);

    @Query("select c from Category c where lower(c.name) like lower(concat('%', :query, '%')) or lower(c.slug) like lower(concat('%', :query, '%'))")
    Page<Category> searchCategories(@Param("query") String query, Pageable pageable);

    java.util.Optional<Category> findByNameIgnoreCase(String name);
}
