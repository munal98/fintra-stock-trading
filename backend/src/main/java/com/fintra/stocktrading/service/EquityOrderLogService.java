package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.entity.EquityOrderLog;
import com.fintra.stocktrading.model.enums.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface EquityOrderLogService {
    /**
     * Logs a status change event for the given equity order.
     *
     * @param order    the order whose status has changed
     * @param status   the new status of the order
     * @param txnTime  the timestamp of the status change event
     * @param desc     an optional description of the event (may be null)
     * @throws IllegalArgumentException if order or status is null, or if txnTime is null
     */
    void logStatusChange(EquityOrder order, OrderStatus status, LocalDateTime txnTime, String desc);

    /**
     * Retrieves all log entries for the given equity order, ordered by event time ascending.
     *
     * @param order the equity order for which logs are to be retrieved
     * @return list of log entries associated with the order
     * @throws IllegalArgumentException if order is null
     */
    List<EquityOrderLog> getLogsForOrder(EquityOrder order);
}
