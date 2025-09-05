package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.response.PortfolioReportResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Portfolio Reports", description = "APIs for generating portfolio reports and analytics")
@SecurityRequirement(name = "bearerAuth")
public interface PortfolioReportControllerDoc {

    @Operation(
            summary = "Get portfolio report for account",
            description = "Generates a comprehensive portfolio report including cash balances, equity holdings, market values, and unrealized P&L for the specified account."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Portfolio report generated successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PortfolioReportResponse.class),
                    examples = @ExampleObject(
                            name = "PortfolioReportExample",
                            summary = "Complete portfolio report",
                            value = """
                            {
                              "accountId": 45,
                              "userFullName": "John Doe",
                              "freeCash": 8500.00,
                              "blockedCash": 1500.00,
                              "totalCash": 10000.00,
                              "identityNumber": "12345678901",
                              "holdings": [
                                {
                                  "equityId": 1,
                                  "symbol": "AAPL",
                                  "freeQty": 100.00,
                                  "blockedQty": 0.00,
                                  "totalQty": 100.00,
                                  "avgCost": 150.00,
                                  "costBasis": 15000.00,
                                  "lastClosePrice": 155.50,
                                  "marketValue": 15550.00,
                                  "unrealizedPnl": 550.00,
                                  "unrealizedPnlPct": 3.67
                                },
                                {
                                  "equityId": 2,
                                  "symbol": "GOOGL",
                                  "freeQty": 25.00,
                                  "blockedQty": 5.00,
                                  "totalQty": 30.00,
                                  "avgCost": 2800.00,
                                  "costBasis": 84000.00,
                                  "lastClosePrice": 2750.00,
                                  "marketValue": 82500.00,
                                  "unrealizedPnl": -1500.00,
                                  "unrealizedPnlPct": -1.79
                                }
                              ],
                              "holdingsValue": 98050.00,
                              "portfolioValue": 108050.00,
                              "totalUnrealizedPnl": -950.00,
                              "totalUnrealizedPnlPct": -0.97
                            }
                            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "400",
            description = "Invalid account ID parameter",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "InvalidAccountId",
                            summary = "Invalid account ID",
                            value = """
                            {
                              "timestamp": "2025-08-10T20:06:00Z",
                              "status": 400,
                              "error": "Bad Request",
                              "message": "Account ID must be a positive integer",
                              "path": "/api/v1/report/portfolio"
                            }
                            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Account not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "AccountNotFound",
                            summary = "Account not found",
                            value = """
                            {
                              "timestamp": "2025-08-10T20:06:00Z",
                              "status": 404,
                              "error": "Not Found",
                              "message": "Account with ID 999 not found",
                              "path": "/api/v1/report/portfolio"
                            }
                            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied - insufficient permissions",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                            name = "AccessDenied",
                            summary = "Access denied",
                            value = """
                            {
                              "timestamp": "2025-08-10T20:06:00Z",
                              "status": 403,
                              "error": "Forbidden",
                              "message": "Access denied to account 45",
                              "path": "/api/v1/report/portfolio"
                            }
                            """
                    )
            )
    )
    PortfolioReportResponse getPortfolio(
            @Parameter(
                    name = "accountId",
                    description = "The unique identifier of the account to generate portfolio report for",
                    required = true,
                    example = "45"
            )
            Integer accountId
    );
}
