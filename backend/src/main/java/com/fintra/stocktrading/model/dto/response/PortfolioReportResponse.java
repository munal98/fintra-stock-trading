package com.fintra.stocktrading.model.dto.response;

import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class PortfolioReportResponse {
    private Integer accountId;
    private String userFullName;
    private BigDecimal freeCash;
    private String accountType;
    private BigDecimal blockedCash;
    private BigDecimal totalCash;
    private String identityNumber;

    private List<PortfolioHoldingResponse> holdings;

    private BigDecimal holdingsValue;
    private BigDecimal portfolioValue;
    private BigDecimal totalUnrealizedPnl;
    private BigDecimal totalUnrealizedPnlPct;
}
