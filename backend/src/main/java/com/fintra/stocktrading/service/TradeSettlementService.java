package com.fintra.stocktrading.service;

import java.time.LocalDate;

public interface TradeSettlementService {
    /**
     * Settles trades for the given trade date (T+2 settlement).
     * Only processes trades that are not already SETTLED (idempotent operation).
     * 
     * @param tradeDate the date of trades to settle
     * @return number of trades that were actually settled
     */
    int settleTradesOnDate(LocalDate tradeDate);
}
