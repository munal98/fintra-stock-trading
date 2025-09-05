package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquityStockRepository extends JpaRepository<EquityStock, Integer> {
    Optional<EquityStock> findByAccountAndEquity(Account account, Equity equity);
    Optional<EquityStock> findByAccount_AccountIdAndEquity_EquityId(Long accountId, Integer equityId);
    List<EquityStock> findByAccount(Account account);
    List<EquityStock> findByEquity(Equity equity);

    @Query("SELECT es FROM EquityStock es " +
           "JOIN FETCH es.equity e " +
           "WHERE es.account.accountId = :accountId " +
           "AND (es.freeQuantity + es.blockedQuantity) > 0 " +
           "ORDER BY e.equityCode")
    List<EquityStock> findEquityHoldingsByAccountId(@Param("accountId") Integer accountId);

    @Query("SELECT es FROM EquityStock es " +
           "JOIN FETCH es.equity e " +
           "WHERE es.account.accountId IN :accountIds " +
           "AND (es.freeQuantity + es.blockedQuantity) > 0 " +
           "ORDER BY es.account.accountId, e.equityCode")
    List<EquityStock> findEquityHoldingsByAccountIds(@Param("accountIds") List<Integer> accountIds);
}
