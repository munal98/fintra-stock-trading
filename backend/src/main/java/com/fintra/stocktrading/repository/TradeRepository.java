package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.entity.Trade;
import org.springframework.data.domain.Page;
import com.fintra.stocktrading.model.enums.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Integer> {

    List<Trade> findByMatchId(Integer matchId);
    List<Trade> findByEquityOrder(EquityOrder equityOrder);
    List<Trade> findByEquityOrder_OrderId(Integer orderId);
    List<Trade> findByEquityOrder_Equity_EquityId(Integer equityId);


    @Query("""
    SELECT t
    FROM Trade t
    WHERE t.equityOrder.equity.equityId = :equityId
    AND t.transactionTime BETWEEN :start AND :end
    """)
    Page<Trade> findByEquityIdAndDateRange(@Param("equityId") Integer equityId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           Pageable pageable);

    Optional<Trade> findByMatchIdAndEquityOrder(Integer matchId, EquityOrder equityOrder);

    @Query("""
    SELECT t FROM Trade t 
    WHERE t.status = :status 
    ORDER BY t.transactionTime DESC
    """)
    List<Trade> findByStatus(@Param("status") TradeStatus status);

    @Query("""
    select t from Trade t
    where t.equityOrder.equity.equityId = :equityId and t.price > 0
    order by t.transactionTime desc
    """)
    List<Trade> findLatestNonZeroTrades(@Param("equityId") Integer equityId);
}
