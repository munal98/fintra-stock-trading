package com.fintra.stocktrading.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Equity holding information with real-time pricing")
public class EquityHoldingResponse {

    @Schema(description = "Unique identifier of the equity", example = "123")
    private Integer equityId;

    @Schema(description = "Asset code of the equity", example = "GARAN.E")
    private String assetCode;

    @Schema(description = "Full name of the equity", example = "Garanti Bankası A.Ş.")
    private String assetName;

    @Schema(description = "Total quantity owned (free + blocked)", example = "100")
    private Integer totalQuantity;

    @Schema(description = "Average cost per share", example = "50.25")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.##")
    private BigDecimal averageCost;

    @Schema(description = "Current close price from market", example = "55.00")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.##")
    private BigDecimal closePrice;

    @Schema(description = "Profit/Loss percentage", example = "9.52")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.##")
    private BigDecimal profitLossPercentage;
}
