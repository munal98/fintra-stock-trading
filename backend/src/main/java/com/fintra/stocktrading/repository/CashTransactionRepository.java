package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.CashTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CashTransactionRepository extends JpaRepository<CashTransaction, Integer> {
    List<CashTransaction> findByAccount(Account account);
    List<CashTransaction> findByAccount_AccountId(Long accountId);
    List<CashTransaction> findByTransactionType(String transactionType);
}
