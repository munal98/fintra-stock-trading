package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.dto.request.ExternalTransferToPortfolioRequest;
import com.fintra.stocktrading.model.dto.request.PortfolioExternalTransferRequest;
import com.fintra.stocktrading.model.dto.request.PortfolioTransferRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Equity Transfers", description = "APIs for managing equity transfers between portfolios and external institutions with automatic cost basis tracking")
public interface EquityTransferControllerDoc {

    @Operation(
            summary = "Transfer equity between portfolios",
            description = "Transfers a specified quantity of a stock from one user portfolio to another user's portfolio. " +
                    "The system automatically maintains cost basis tracking using weighted average cost calculations. " +
                    "The sender's average cost is preserved while the receiver's average cost is recalculated based on weighted average."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer completed successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    name = "SuccessfulTransfer",
                                    summary = "Successful portfolio-to-portfolio transfer",
                                    description = "The equity transfer was completed successfully with cost basis tracking",
                                    value = "Portfolio-to-portfolio transfer completed successfully."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed - Invalid request data or insufficient stock quantity",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ValidationError",
                                            summary = "Request validation failed",
                                            description = "Request contains invalid data such as missing required fields or invalid values",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "path": "/api/v1/equity-transfers/portfolio-to-portfolio",
                                              "errors": {
                                                "fromAccountId": "From account ID is required",
                                                "toAccountId": "To account ID is required",
                                                "equityId": "Equity ID is required",
                                                "transferQuantity": "Transfer quantity must be positive"
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "InsufficientQuantity",
                                            summary = "Insufficient stock quantity",
                                            description = "The sender account does not have enough free quantity for the transfer",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Insufficient free quantity. Required: 100, Available: 50",
                                              "path": "/api/v1/equity-transfers/portfolio-to-portfolio"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - The specified account, equity, or stock was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "AccountNotFound",
                                            summary = "Account not found",
                                            description = "The specified sender or receiver account could not be found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Sender account not found with ID: 123",
                                              "path": "/api/v1/equity-transfers/portfolio-to-portfolio"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "EquityNotFound",
                                            summary = "Equity not found",
                                            description = "The specified equity could not be found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Equity not found with ID: 456",
                                              "path": "/api/v1/equity-transfers/portfolio-to-portfolio"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "StockNotFound",
                                            summary = "Stock holding not found",
                                            description = "The sender account does not have holdings for the specified equity",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Stock not found for account 123 and equity 456",
                                              "path": "/api/v1/equity-transfers/portfolio-to-portfolio"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected system error during transfer",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Internal server error",
                                    description = "An unexpected server error occurred during the transfer operation",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:37:24.123+03:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred during portfolio transfer",
                                      "path": "/api/v1/equity-transfers/portfolio-to-portfolio"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<?> transferPortfolioToPortfolio(PortfolioTransferRequest request);

    @Operation(
            summary = "Transfer equity from portfolio to external institution",
            description = "Transfers a specified quantity of a stock from a user's portfolio to an external institution. " +
                    "This operation reduces the free quantity in the sender's portfolio while maintaining the average cost. " +
                    "The transfer is logged for audit purposes and regulatory compliance."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer completed successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    name = "SuccessfulTransfer",
                                    summary = "Successful portfolio-to-external transfer",
                                    description = "The equity transfer to external institution was completed successfully",
                                    value = "Portfolio-to-external transfer completed successfully."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed - Invalid request data or insufficient stock quantity",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ValidationError",
                                            summary = "Request validation failed",
                                            description = "Request contains invalid data such as missing required fields or invalid values",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "path": "/api/v1/equity-transfers/portfolio-to-external",
                                              "errors": {
                                                "fromAccountId": "From account ID is required",
                                                "equityId": "Equity ID is required",
                                                "transferQuantity": "Transfer quantity must be positive",
                                                "otherInstitutionId": "Other institution ID is required"
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "InsufficientQuantity",
                                            summary = "Insufficient free quantity",
                                            description = "The account does not have enough free quantity for the external transfer",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Insufficient free quantity for external transfer. Required: 200, Available: 150",
                                              "path": "/api/v1/equity-transfers/portfolio-to-external"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - The specified account, equity, institution, or stock was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "AccountNotFound",
                                            summary = "Account not found",
                                            description = "The specified sender account could not be found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Sender account not found with ID: 123",
                                              "path": "/api/v1/equity-transfers/portfolio-to-external"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "InstitutionNotFound",
                                            summary = "External institution not found",
                                            description = "The specified external institution could not be found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Other institution not found with ID: 789",
                                              "path": "/api/v1/equity-transfers/portfolio-to-external"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected system error during transfer",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Internal server error",
                                    description = "An unexpected server error occurred during the external transfer operation",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:37:24.123+03:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred during external transfer",
                                      "path": "/api/v1/equity-transfers/portfolio-to-external"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<?> transferPortfolioToExternal(PortfolioExternalTransferRequest request);

    @Operation(
            summary = "Transfer equity from external institution to portfolio",
            description = "Transfers a specified quantity of a stock from an external institution to a user's portfolio. " +
                    "The system automatically calculates and updates the weighted average cost based on the incoming transfer. " +
                    "If avgCost is not provided, the system uses automatic cost determination logic. " +
                    "This operation increases the free quantity and recalculates the average cost in the recipient's portfolio."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transfer completed successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    name = "SuccessfulTransfer",
                                    summary = "Successful external-to-portfolio transfer",
                                    description = "The equity transfer from external institution was completed successfully with cost basis update",
                                    value = "External-to-portfolio transfer completed successfully."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed - Invalid request data",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ValidationError",
                                            summary = "Request validation failed",
                                            description = "Request contains invalid data such as missing required fields or invalid values",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "path": "/api/v1/equity-transfers/external-to-portfolio",
                                              "errors": {
                                                "toAccountId": "To account ID is required",
                                                "equityId": "Equity ID is required",
                                                "transferQuantity": "Transfer quantity must be positive",
                                                "otherInstitutionId": "Other institution ID is required"
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "InvalidCost",
                                            summary = "Invalid average cost",
                                            description = "The provided average cost is invalid or negative",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Average cost must be positive when provided",
                                              "path": "/api/v1/equity-transfers/external-to-portfolio"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Not Found - The specified account, equity, or institution was not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "AccountNotFound",
                                            summary = "Account not found",
                                            description = "The specified recipient account could not be found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Recipient account not found with ID: 123",
                                              "path": "/api/v1/equity-transfers/external-to-portfolio"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "EquityNotFound",
                                            summary = "Equity not found",
                                            description = "The specified equity could not be found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Equity not found with ID: 456",
                                              "path": "/api/v1/equity-transfers/external-to-portfolio"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "InstitutionNotFound",
                                            summary = "External institution not found",
                                            description = "The specified external institution could not be found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:37:24.123+03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Other institution not found with ID: 789",
                                              "path": "/api/v1/equity-transfers/external-to-portfolio"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected system error during transfer",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Internal server error",
                                    description = "An unexpected server error occurred during the external-to-portfolio transfer operation",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:37:24.123+03:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred during external-to-portfolio transfer",
                                      "path": "/api/v1/equity-transfers/external-to-portfolio"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<?> transferFromExternalToPortfolio(ExternalTransferToPortfolioRequest request);
}
