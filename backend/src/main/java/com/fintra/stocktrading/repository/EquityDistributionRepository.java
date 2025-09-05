package com.fintra.stocktrading.repository;

import com.fintra.stocktrading.model.entity.EquityDistribution;
import com.fintra.stocktrading.model.entity.EquityOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquityDistributionRepository extends JpaRepository<EquityDistribution, Integer> {

    List<EquityDistribution> findByEquityOrder(EquityOrder equityOrder);
    List<EquityDistribution> findByEquityOrder_OrderId(Long orderId);
    List<EquityDistribution> findByEquityOrder_OrderId(Integer orderId);
}
