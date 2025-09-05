package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityPriceHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquityPriceHistoryRepository extends JpaRepository<EquityPriceHistory, Integer> {

    List<EquityPriceHistory> findByEquity(Equity equity);

    List<EquityPriceHistory> findByEquityOrderByDataDateDesc(Equity equity);

    @Query("SELECT eph FROM EquityPriceHistory eph WHERE eph.equity = :equity AND eph.dataDate BETWEEN :startDate AND :endDate ORDER BY eph.dataDate DESC")
    List<EquityPriceHistory> findByEquityAndDataDateBetween(@Param("equity") Equity equity,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate);

    @Query("SELECT eph FROM EquityPriceHistory eph WHERE eph.equity = :equity AND eph.dataDate BETWEEN :startDate AND :endDate ORDER BY eph.dataDate DESC")
    Page<EquityPriceHistory> findByEquityAndDataDateBetween(@Param("equity") Equity equity,
                                                           @Param("startDate") LocalDate startDate,
                                                           @Param("endDate") LocalDate endDate,
                                                           Pageable pageable);

    @Query("SELECT eph FROM EquityPriceHistory eph WHERE eph.equity = :equity ORDER BY eph.dataDate DESC LIMIT 1")
    Optional<EquityPriceHistory> findLatestByEquity(@Param("equity") Equity equity);

    @Query("SELECT eph FROM EquityPriceHistory eph WHERE eph.dataDate = :date ORDER BY eph.equity.equityCode")
    List<EquityPriceHistory> findByDataDate(@Param("date") LocalDate date);

    @Query("SELECT eph FROM EquityPriceHistory eph WHERE eph.dataDate = (SELECT MAX(eph2.dataDate) FROM EquityPriceHistory eph2) ORDER BY eph.equity.equityCode")
    List<EquityPriceHistory> findLatestPricesForAllEquities();

    boolean existsByEquityAndDataDate(Equity equity, LocalDate dataDate);

    @Query("SELECT DISTINCT eph.dataDate FROM EquityPriceHistory eph WHERE eph.equity.equityId IN :equityIds AND eph.dataDate BETWEEN :startDate AND :endDate")
    List<LocalDate> findExistingPriceDatesByEquityInRange(@Param("equityIds") List<Integer> equityIds,
                                                         @Param("startDate") LocalDate startDate,
                                                         @Param("endDate") LocalDate endDate);

    @Query("""
        SELECT eph FROM EquityPriceHistory eph 
        WHERE eph.equity IN :equities 
        AND eph.dataDate = (
            SELECT MAX(eph2.dataDate) 
            FROM EquityPriceHistory eph2 
            WHERE eph2.equity = eph.equity
        )
        """)
    List<EquityPriceHistory> findLatestPricesForEquities(@Param("equities") List<Equity> equities);
}
