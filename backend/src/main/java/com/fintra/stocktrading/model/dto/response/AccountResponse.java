package com.fintra.stocktrading.model.dto.response;

import com.fintra.stocktrading.model.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Account information response")
public class AccountResponse {

    @Schema(description = "Account ID", example = "1")
    private Integer accountId;

    @Schema(description = "Account type", example = "INDIVIDUAL")
    private AccountType accountType;

    @Schema(description = "Cash balance information")
    private CashBalanceResponse cashBalance;

    @Schema(description = "Equity holdings with real-time pricing and profit/loss")
    private List<EquityHoldingResponse> equities;
}
