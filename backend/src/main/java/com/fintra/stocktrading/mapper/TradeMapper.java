package com.fintra.stocktrading.mapper;

import com.fintra.stocktrading.model.dto.response.TradeDto;
import com.fintra.stocktrading.model.entity.Trade;
import org.springframework.stereotype.Component;

@Component
public class TradeMapper {
    public TradeDto toDto(Trade entity) {
        TradeDto dto = new TradeDto();
        dto.setTradeId(entity.getTradeId());
        dto.setMatchId(entity.getMatchId());
        dto.setEquityOrderId(entity.getEquityOrder().getOrderId());
        dto.setTradeQuantity(entity.getTradeQuantity());
        dto.setPrice(entity.getPrice());
        dto.setCommission(entity.getCommission());
        dto.setTransactionTime(entity.getTransactionTime());
        return dto;
    }
}
