package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Integer> {
    List<Account> findByCustomer(Customer customer);
    List<Account> findByCustomer_CustomerId(Integer customerId);
}
