package com.fintra.stocktrading.model.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ExternalPriceRecordDto {

    @JsonProperty("asset_code")
    private String assetCode;

    @JsonProperty("open_price")
    private BigDecimal openPrice;

    @JsonProperty("close_price")
    private BigDecimal closePrice;

    @JsonProperty("high_price")
    private BigDecimal highPrice;

    @JsonProperty("low_price")
    private BigDecimal lowPrice;

    @JsonProperty("data_date")
    private LocalDate dataDate;

    @JsonProperty("record_date")
    private LocalDateTime recordDate;
}
