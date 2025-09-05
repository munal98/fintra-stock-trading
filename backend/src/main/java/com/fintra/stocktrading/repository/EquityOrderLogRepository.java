package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.entity.EquityOrderLog;
import com.fintra.stocktrading.model.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquityOrderLogRepository extends JpaRepository<EquityOrderLog, Integer> {
    List<EquityOrderLog> findByEquityOrder(EquityOrder equityOrder);
    List<EquityOrderLog> findByEquityOrder_OrderId(Long orderId);
    List<EquityOrderLog> findByOrderStatus(OrderStatus orderStatus);
}
