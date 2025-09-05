package com.fintra.stocktrading.model.dto.request;

import com.fintra.stocktrading.model.enums.AccountType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to partially update account information")
public class AccountPatchRequest {

    @Schema(description = "Type of account (optional for partial update)", example = "CORPORATE")
    private AccountType accountType;
}
