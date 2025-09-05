package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.entity.EquityOrder;

public interface OrderMatchingService {
    
    /**
     * Matches all open orders in the system according to the matching rules.
     * This method is typically called periodically or after significant order book changes.
     * Uses price-time priority algorithm to match BUY and SELL orders.
     *
     * @throws com.fintra.stocktrading.exception.OrderMatchingException if matching fails
     */
    void matchAllOpenOrders();

    /**
     * Attempts to match a single newly created order against existing open counter orders
     * according to the matching rules. For BUY orders, matches against SELL orders and vice versa.
     * Creates EquityOrderMatch and Trade records for successful matches.
     *
     * @param order the newly created order to be matched
     * @throws com.fintra.stocktrading.exception.OrderMatchingException if matching fails
     * @throws IllegalArgumentException if the order is null or in an invalid state for matching
     */
    void matchOrder(EquityOrder order);

    /**
     * Attempts to match a single order by its ID against existing open counter orders.
     * Convenience method that fetches the order by ID and delegates to matchOrder(EquityOrder).
     *
     * @param newOrderId the ID of the newly created order to be matched
     * @throws com.fintra.stocktrading.exception.OrderMatchingException if matching fails
     * @throws IllegalArgumentException if the order ID is invalid or order not found
     */
    void matchOrder(Integer newOrderId);
}
