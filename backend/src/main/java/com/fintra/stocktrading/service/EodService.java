package com.fintra.stocktrading.service;

public interface EodService {
    
    /**
     * Executes the complete End of Day process including:
     * - Expiring old pending orders and restoring blocked balances
     * - Settling T+2 trades with cash/equity transfers
     * - Advancing system date to next business day
     */
    void runEndOfDay();
}
