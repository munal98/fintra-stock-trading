package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.response.EquityOrderHistoryResponse;
import com.fintra.stocktrading.model.entity.EquityOrder;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface EquityOrderHistoryService {
    /**
     * Records an operation (history) event for a given order.
     *
     * @param order            the order entity
     * @param oldOrderQuantity previous order quantity (before change)
     * @param oldPrice         previous price (before change)
     * @param txnTime          event timestamp (nullable, auto-set if null)
     */
    void recordHistory(EquityOrder order, Integer oldOrderQuantity, BigDecimal oldPrice, LocalDateTime txnTime);

    Page<EquityOrderHistoryResponse> getHistoriesView(Long accountId, Long equityId, Pageable pageable);
}
