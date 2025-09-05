package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.request.EquityOrderRequest;
import com.fintra.stocktrading.model.dto.request.EquityOrderUpdateRequest;
import com.fintra.stocktrading.model.dto.response.EquityOrderResponse;

public interface EquityOrderService {
    /**
     * Creates a new equity order with the specified request data.
     *
     * @param requestDto the order request containing account, equity, side, quantity, price, and expiration info
     * @return the details of the created order (id, account, equity, quantity, price, expiration, status, dates)
     * @throws com.fintra.stocktrading.exception.NotFoundException if account or equity is not found
     * @throws IllegalArgumentException if quantity or price is invalid
     */
    EquityOrderResponse createOrder(EquityOrderRequest requestDto);

    /**
     * Cancels the given order by its ID, if it is not already finalized.
     *
     * @param orderId the ID of the order to cancel
     * @return the updated order details after cancellation
     * @throws com.fintra.stocktrading.exception.NotFoundException if the order is not found
     * @throws com.fintra.stocktrading.exception.BadRequestException if the order is already finalized (filled, cancelled, or expired)
     */
    EquityOrderResponse cancelOrder(Integer orderId);

    /**
     * Retrieves the details of an equity order by its ID.
     *
     * @param orderId the ID of the order to retrieve
     * @return the order details
     * @throws com.fintra.stocktrading.exception.NotFoundException if the order is not found
     */
    EquityOrderResponse getOrderById(Integer orderId);

    /**
     * Updates the details of an existing equity order, if it is not finalized.
     *
     * @param orderId the ID of the order to update
     * @param requestDto the update request containing updated fields (e.g., quantity, price, expiration)
     * @return the updated order details after modification
     * @throws com.fintra.stocktrading.exception.NotFoundException if the order is not found
     * @throws com.fintra.stocktrading.exception.BadRequestException if the order is already finalized (filled, cancelled, or expired)
     * @throws IllegalArgumentException if new quantity or price is invalid
     */
    EquityOrderResponse updateOrder(Integer orderId, EquityOrderUpdateRequest requestDto);

}
