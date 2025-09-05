package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.response.OrderBookResponse;

public interface OrderBookService {

    /**
     * Retrieves the orderbook for a specific equity showing all pending, partially filled, and updated orders
     * grouped as bids (buy orders) and asks (sell orders) with proper sorting.
     *
     * @param equityId the ID of the equity to get orderbook for
     * @return OrderBookResponse containing bids and asks arrays
     * @throws com.fintra.stocktrading.exception.NotFoundException if equity is not found
     */
    OrderBookResponse getOrderBookByEquityId(Integer equityId);

    /**
     * Retrieves the orderbook for a specific equity excluding a specific order ID.
     * Used for removing an order from the orderbook display after it's processed.
     *
     * @param equityId the ID of the equity to get orderbook for
     * @param excludeOrderId the order ID to exclude from the results
     * @return OrderBookResponse containing bids and asks arrays without the excluded order
     * @throws com.fintra.stocktrading.exception.NotFoundException if equity is not found
     */
    OrderBookResponse getOrderBookByEquityIdExcludingOrder(Integer equityId, Integer excludeOrderId);
}
