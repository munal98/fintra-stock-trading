package com.fintra.stocktrading.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TradeMatchedEvent {
    private Integer matchId;
    private Integer buyTradeId;
    private Integer sellTradeId;
    private Integer buyOrderId;
    private Integer sellOrderId;
    private int quantity;
    private BigDecimal price;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;
}
