package com.uminimalist.store.repository;

import com.uminimalist.store.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);

    Page<User> findAll(Pageable pageable);

    @Query("select u from User u where lower(u.fullName) like lower(concat('%', :query, '%')) or lower(u.email) like lower(concat('%', :query, '%')) or lower(u.phone) like lower(concat('%', :query, '%'))")
    Page<User> searchUsers(@Param("query") String query, Pageable pageable);
}
