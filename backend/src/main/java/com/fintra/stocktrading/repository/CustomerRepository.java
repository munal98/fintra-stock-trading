package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.Customer;
import com.fintra.stocktrading.model.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    
    List<Customer> findByUser(User user);
    List<Customer> findByUser_Id(Integer userId);

    @Query("SELECT DISTINCT c FROM Customer c " +
           "LEFT JOIN FETCH c.accounts a " +
           "LEFT JOIN FETCH a.cashBalance " +
           "WHERE (:search IS NULL OR " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.tradingPermission AS string)) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.tradingEnabled AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> findAllCustomersWithFilters(
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT DISTINCT c FROM Customer c " +
           "LEFT JOIN FETCH c.accounts a " +
           "LEFT JOIN FETCH a.cashBalance " +
           "WHERE c.user.id = :userId AND c.tradingEnabled = true AND " +
           "(:search IS NULL OR " +
           "LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(CAST(c.tradingPermission AS string)) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Customer> findAssignedCustomersForTrader(
            @Param("userId") Integer userId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("SELECT c FROM Customer c " +
           "LEFT JOIN FETCH c.accounts a " +
           "LEFT JOIN FETCH a.cashBalance " +
           "WHERE c.customerId = :customerId")
    Optional<Customer> findByIdWithAccountsAndCashBalance(@Param("customerId") Integer customerId);

    boolean existsByEmail(String email);
    boolean existsByIdentityNumber(String identityNumber);
    Optional<Customer> findByEmail(String email);
}
