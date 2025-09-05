package com.fintra.stocktrading.service;

import java.time.LocalDate;

public interface EquityOrderExpireService  {

    /**
     * Expires old orders for the given date.
     * Only processes orders that are PENDING or PARTIALLY_FILLED and dated before the given date.
     * Skips orders that are already EXPIRED (idempotent operation).
     * 
     * @param today current system date
     * @return number of orders that were actually expired
     */
    int expireOldOrders(LocalDate today);

}
