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
@Schema(description = "Cash balance information response")
public class CashBalanceResponse {

    @Schema(description = "Balance ID", example = "1")
    private Integer balanceId;

    @Schema(description = "Free balance amount", example = "1000.50")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.##")
    private BigDecimal freeBalance;

    @Schema(description = "Blocked balance amount", example = "250.00")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.##")
    private BigDecimal blockedBalance;

    @Schema(description = "Total balance (free + blocked)", example = "1250.50")
    @JsonFormat(shape = JsonFormat.Shape.NUMBER, pattern = "#.##")
    private BigDecimal totalBalance;
}
