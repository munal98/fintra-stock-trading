package com.fintra.stocktrading.model.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioHoldingResponse {
    private Integer equityId;
    private String symbol;
    private BigDecimal freeQty;
    private BigDecimal blockedQty;
    private BigDecimal totalQty;
    private BigDecimal avgCost;
    private BigDecimal costBasis;
    private BigDecimal lastClosePrice;
    private BigDecimal marketValue;
    private BigDecimal unrealizedPnl;
    private BigDecimal unrealizedPnlPct;
}
