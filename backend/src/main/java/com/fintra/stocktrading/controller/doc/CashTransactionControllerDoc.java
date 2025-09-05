package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.request.CashDepositRequest;
import com.fintra.stocktrading.model.dto.request.CashTransferRequest;
import com.fintra.stocktrading.model.dto.request.CashWithdrawRequest;
import com.fintra.stocktrading.model.dto.response.CashTransactionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;

@Tag(name = "Cash Transactions", description = "APIs for customer cash deposit, withdraw, and transfer operations")
@SecurityRequirement(name = "bearerAuth")
public interface CashTransactionControllerDoc {
    @Operation(
            summary = "Deposit cash to customer account",
            description = "Deposit a specified amount into a customer account. Only ADMIN and TRADER roles are authorized."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Deposit successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CashTransactionResponse.class),
                            examples = @ExampleObject(
                                    name = "DepositSuccess",
                                    summary = "Deposit completed",
                                    value = """
                    {
                      "transactionId": 123,
                      "accountId": 45,
                      "amount": 1000.00,
                      "transactionType": "DEPOSIT",
                      "transactionTime": "2025-08-01T12:34:56.000",
                      "newFreeBalance": 4000.00
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or invalid deposit amount",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account or balance not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<CashTransactionResponse> deposit(
            @Valid @RequestBody(description = "Deposit request body", required = true,
                    content = @Content(schema = @Schema(implementation = CashDepositRequest.class),
                            examples = @ExampleObject(
                                    name = "DepositRequest",
                                    value = """
                        {
                          "accountId": 45,
                          "amount": 1000.00
                        }
                        """
                            )))
            CashDepositRequest request
    );

    @Operation(
            summary = "Withdraw cash from customer account",
            description = "Withdraw a specified amount from a customer account. Only ADMIN and TRADER roles are authorized."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Withdraw successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CashTransactionResponse.class),
                            examples = @ExampleObject(
                                    name = "WithdrawSuccess",
                                    summary = "Withdraw completed",
                                    value = """
                    {
                      "transactionId": 124,
                      "accountId": 45,
                      "amount": 500.00,
                      "transactionType": "WITHDRAW",
                      "transactionTime": "2025-08-01T14:00:00.000",
                      "newFreeBalance": 3500.00
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed, insufficient balance or invalid withdraw amount",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account or balance not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<CashTransactionResponse> withdraw(
            @Valid @RequestBody(description = "Withdraw request body", required = true,
                    content = @Content(schema = @Schema(implementation = CashWithdrawRequest.class),
                            examples = @ExampleObject(
                                    name = "WithdrawRequest",
                                    value = """
                        {
                          "accountId": 45,
                          "amount": 500.00
                        }
                        """
                            )))
            CashWithdrawRequest request
    );

    @Operation(
            summary = "Transfer cash between customer accounts",
            description = "Transfer a specified amount from one customer account to another. Only ADMIN and TRADER roles are authorized."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CashTransactionResponse.class),
                            examples = @ExampleObject(
                                    name = "TransferSuccess",
                                    summary = "Transfer completed",
                                    value = """
                    {
                      "transactionId": 200,
                      "accountId": 12,
                      "amount": 100.00,
                      "transactionType": "TRANSFER_OUT",
                      "transactionTime": "2025-08-01T17:15:00.000",
                      "newFreeBalance": 500.00
                    }
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed or insufficient balance",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account or balance not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<CashTransactionResponse> transfer(
            @Valid @RequestBody(description = "Transfer request body", required = true,
                    content = @Content(schema = @Schema(implementation = CashTransferRequest.class),
                            examples = @ExampleObject(
                                    name = "TransferRequest",
                                    value = """
                        {
                          "senderAccountId": 12,
                          "receiverAccountId": 17,
                          "amount": 100.00
                        }
                        """
                            )))
            CashTransferRequest request
    );
}
