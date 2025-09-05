package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.response.EquityDistributionDto;
import com.fintra.stocktrading.model.entity.EquityOrder;

import java.math.BigDecimal;
import java.util.List;

public interface EquityDistributionService {
    
    /**
     * Creates an equity distribution record for a settled trade.
     * Used during T+2 settlement process to record equity transfers.
     *
     * @param order the equity order that was settled
     * @param quantity the quantity of shares distributed
     * @param price the price per share at settlement
     * @param side the order side ("BUY" or "SELL")
     */
    void createDistribution(EquityOrder order, Integer quantity, BigDecimal price, String side);
    
    /**
     * Retrieves all equity distribution records in the system.
     *
     * @return list of all equity distributions
     */
    List<EquityDistributionDto> getAllDistributions();
    
    /**
     * Retrieves equity distribution records for a specific order.
     *
     * @param orderId the ID of the equity order
     * @return list of distributions for the specified order
     */
    List<EquityDistributionDto> getDistributionsByOrderId(Integer orderId);
}
