package com.fintra.stocktrading.model.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeDto {
    private Integer tradeId;
    private Integer matchId;
    private Integer equityOrderId;
    private Integer tradeQuantity;
    private BigDecimal price;
    private BigDecimal commission;
    private LocalDateTime transactionTime;
}
