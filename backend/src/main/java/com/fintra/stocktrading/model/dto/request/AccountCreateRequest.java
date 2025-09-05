package com.fintra.stocktrading.model.dto.request;

import com.fintra.stocktrading.model.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new account for an existing customer")
public class AccountCreateRequest {

    @NotNull(message = "Customer ID is required")
    @Schema(description = "ID of the customer to create account for", example = "1")
    private Integer customerId;

    @NotNull(message = "Account type is required")
    @Schema(description = "Type of account to create", example = "INDIVIDUAL")
    private AccountType accountType;
}
