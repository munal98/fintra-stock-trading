package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.response.CashBalanceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Cash Balance", description = "APIs for querying cash balances of customer accounts")
@SecurityRequirement(name = "bearerAuth")
public interface CashBalanceControllerDoc {

    @Operation(
            summary = "Get cash balance for account",
            description = "Returns the free, blocked, and total cash balances for the specified account. Accessible to authorized users."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Balance retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = CashBalanceResponse.class),
                    examples = @ExampleObject(
                            name = "BalanceFound",
                            summary = "Balance retrieved",
                            value = """
                            {
                              "accountId": 45,
                              "freeBalance": 9850.00,
                              "blockedBalance": 150.00,
                              "totalBalance": 10000.00,
                              "currency": "TRY",
                              "lastUpdate": "2025-08-06T19:30:12"
                            }
                            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Account or balance not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "AccountNotFound",
                            summary = "Account not found",
                            value = """
                            {
                              "error": "Not Found",
                              "status": 404,
                              "message": "Account with id 45 not found.",
                              "path": "/api/v1/cash-balance/45",
                              "timestamp": "2025-08-06T19:31:00Z"
                            }
                            """
                    )
            )
    )
    ResponseEntity<CashBalanceResponse> getBalance(
            @Parameter(description = "Unique account ID", example = "45")
            Integer accountId
    );
}
