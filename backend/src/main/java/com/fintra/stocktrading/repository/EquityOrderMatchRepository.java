package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.entity.EquityOrderMatch;
import com.fintra.stocktrading.model.enums.OrderSide;
import com.fintra.stocktrading.model.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EquityOrderMatchRepository extends JpaRepository<EquityOrderMatch, Integer> {
    Optional<EquityOrderMatch> findByMatchId(Integer matchId);
    List<EquityOrderMatch> findByBuyOrder(EquityOrder buyOrder);
    List<EquityOrderMatch> findBySellOrder(EquityOrder sellOrder);
    List<EquityOrderMatch> findByMatchTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("""
        SELECT CASE 
            WHEN m.buyOrder.orderId = :orderId THEN m.sellOrder.orderId
            ELSE m.buyOrder.orderId 
        END
        FROM EquityOrderMatch m 
        WHERE m.buyOrder.orderId = :orderId OR m.sellOrder.orderId = :orderId
    """)
    List<Integer> findMatchedOrderIds(@Param("orderId") Integer orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from EquityOrder o where o.orderId = :id")
    Optional<EquityOrder> findByIdForUpdate(@Param("id") Integer id);

    @Query("""
    SELECT o FROM EquityOrder o
     WHERE o.equity = :equity
       AND o.orderSide = :oppositeSide
       AND o.finalStatus IN :openStatuses
       AND (
            (:side = 'BUY'  AND o.price <= :limitPrice)
         OR (:side = 'SELL' AND o.price >= :limitPrice)
       )
       AND o.remainingQuantity > 0
""")
    List<EquityOrder> findOpenOppositeOrdersForMatching(
            @Param("equity") Equity equity,
            @Param("oppositeSide") OrderSide oppositeSide,
            @Param("side") String side,
            @Param("limitPrice") BigDecimal limitPrice,
            @Param("openStatuses") List<OrderStatus> openStatuses
    );

    @Query("""
    SELECT o FROM EquityOrder o
     WHERE o.equity = :equity
       AND o.orderSide = :oppositeSide
       AND o.finalStatus IN :openStatuses
       AND o.remainingQuantity > 0
""")
    List<EquityOrder> findOpenOppositeOrdersIgnoringPrice(
            @Param("equity") Equity equity,
            @Param("oppositeSide") OrderSide oppositeSide,
            @Param("openStatuses") List<OrderStatus> openStatuses
    );
}
