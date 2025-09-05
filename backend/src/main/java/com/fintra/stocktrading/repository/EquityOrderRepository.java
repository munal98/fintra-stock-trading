package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.enums.OrderSide;
import com.fintra.stocktrading.model.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquityOrderRepository extends JpaRepository<EquityOrder, Integer> {

    List<EquityOrder> findByFinalStatus(OrderStatus finalStatus);
    List<EquityOrder> findByFinalStatusIn(List<OrderStatus> statuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM EquityOrder o WHERE o.orderId = :id")
    Optional<EquityOrder> findByIdForUpdate(@Param("id") Integer id);

    @Query(
            "SELECT o FROM EquityOrder o " +
                    "WHERE o.equity.equityId = :equityId " +
                    "  AND o.finalStatus IN :statuses " +
                    "  AND o.orderSide = :orderSide " +
                    "  AND o.remainingQuantity > 0 " +
                    "ORDER BY " +
                    "  CASE WHEN :orderSide = 'BUY' THEN o.price END DESC, " +
                    "  CASE WHEN :orderSide = 'SELL' THEN o.price END ASC"
    )
    List<EquityOrder> findOrderBookOrdersByEquityIdAndSide(
            @Param("equityId") Integer equityId,
            @Param("statuses") List<OrderStatus> statuses,
            @Param("orderSide") OrderSide orderSide
    );

    @Query(
            "SELECT o FROM EquityOrder o " +
                    "WHERE o.equity.equityId = :equityId " +
                    "  AND o.finalStatus IN :statuses " +
                    "  AND o.orderSide = :orderSide " +
                    "  AND o.remainingQuantity > 0 " +
                    "  AND o.orderId != :excludeOrderId " +
                    "ORDER BY " +
                    "  CASE WHEN :orderSide = 'BUY' THEN o.price END DESC, " +
                    "  CASE WHEN :orderSide = 'SELL' THEN o.price END ASC"
    )
    List<EquityOrder> findOrderBookOrdersByEquityIdAndSideExcludingOrder(
            @Param("equityId") Integer equityId,
            @Param("statuses") List<OrderStatus> statuses,
            @Param("orderSide") OrderSide orderSide,
            @Param("excludeOrderId") Integer excludeOrderId
    );

    @Query(
            "SELECT o FROM EquityOrder o " +
                    "WHERE o.equity.equityId = :equityId " +
                    "  AND o.finalStatus IN :statuses " +
                    "  AND o.orderSide = :orderSide " +
                    "  AND o.remainingQuantity > 0 " +
                    "  AND o.orderId NOT IN :excludeOrderIds " +
                    "ORDER BY " +
                    "  CASE WHEN :orderSide = 'BUY' THEN o.price END DESC, " +
                    "  CASE WHEN :orderSide = 'SELL' THEN o.price END ASC"
    )
    List<EquityOrder> findOrderBookOrdersByEquityIdAndSideExcludingMultipleOrders(
            @Param("equityId") Integer equityId,
            @Param("statuses") List<OrderStatus> statuses,
            @Param("orderSide") OrderSide orderSide,
            @Param("excludeOrderIds") List<Integer> excludeOrderIds
    );
}
