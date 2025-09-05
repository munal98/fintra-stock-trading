package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquityTransferRepository extends JpaRepository<EquityTransfer, Integer> {
    List<EquityTransfer> findByFromAccount(Account fromAccount);
    List<EquityTransfer> findByToAccount(Account toAccount);
    List<EquityTransfer> findByEquity(Equity equity);
    List<EquityTransfer> findByTransferType(String transferType);
}
