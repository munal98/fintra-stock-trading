package com.fintra.stocktrading.model.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EquityPriceResponse {

    private Integer equityId;
    private String ticker;
    private String assetCode;
    private String equityName;
    private String market;
    private BigDecimal openPrice;
    private BigDecimal closePrice;
    private BigDecimal highPrice;
    private BigDecimal lowPrice;
    private LocalDate dataDate;
    private boolean participation;

    public String getAssetCode() {
        if (assetCode != null && assetCode.contains(".")) {
            return assetCode.split("\\.")[0];
        }
        return assetCode;
    }
}
