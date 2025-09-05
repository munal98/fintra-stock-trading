package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.CashBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashBalanceRepository extends JpaRepository<CashBalance, Integer> {

    Optional<CashBalance> findByAccount(Account account);
    Optional<CashBalance> findByAccount_AccountId(Integer accountId);
    void deleteByAccount_AccountId(Integer accountId);
}
