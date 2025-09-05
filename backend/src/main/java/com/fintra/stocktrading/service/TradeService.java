package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.response.TradeDto;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface TradeService {
    /**
     * Finds a trade by its ID.
     *
     * @param tradeId the ID of the trade
     * @return the trade details as a TradeDto
     * @throws com.fintra.stocktrading.exception.NotFoundException if trade is not found
     */
    TradeDto getTradeById(Integer tradeId);

    /**
     * Returns all trades in the system.
     *
     * @return list of all trades
     */
    List<TradeDto> getAllTrades();

    /**
     * Returns all trades for a specific order.
     *
     * @param orderId the ID of the equity order
     * @return list of trades for that order
     */
    List<TradeDto> getTradesByOrderId(Integer orderId);

    /**
     * Returns all trades for a specific equity (stock).
     *
     * @param equityId the ID of the equity
     * @return list of trades for that equity
     */
    List<TradeDto> getTradesByEquityId(Integer equityId);

    Page<TradeDto> getTradesByEquityIdAndDateRange(Long equityId,
                                                   LocalDateTime start,
                                                   LocalDateTime end,
                                                   Pageable pageable);

    /**
     * Returns all trades with SETTLED status.
     *
     * @return list of settled trades
     */
    List<TradeDto> getSettledTrades();
}
