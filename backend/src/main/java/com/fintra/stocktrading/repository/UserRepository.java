package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT DISTINCT u FROM User u " +
           "LEFT JOIN FETCH u.customers c " +
           "WHERE (:search IS NULL OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(u.role AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<User> findAllUsersWithFilters(
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT u FROM User u " +
           "LEFT JOIN FETCH u.customers c " +
           "WHERE u.id = :userId")
    Optional<User> findByIdWithCustomers(@Param("userId") Integer userId);
}
