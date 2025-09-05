package com.fintra.stocktrading.model.dto.request;

import com.fintra.stocktrading.model.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountUpdateRequest {

    @NotNull(message = "Account type is required")
    @Schema(description = "Type of account", example = "CORPORATE", required = true)
    private AccountType accountType;
}
