package com.fintra.stocktrading.controller.doc;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;

@Tag(name = "Equity Stock Cost Management", description = "APIs for testing and calculating equity stock cost calculations with weighted average cost methods")
public interface EquityStockCostControllerDoc {

    @Operation(
            summary = "Calculate weighted average cost",
            description = "Calculates the weighted average cost for equity holdings based on current holdings and new incoming quantities. " +
                    "This is used to determine the new average cost when additional shares are acquired through trades or transfers. " +
                    "Formula: ((Current Quantity × Current Avg Cost) + (New Quantity × New Unit Cost)) ÷ (Current Quantity + New Quantity)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Weighted average cost calculated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BigDecimal.class),
                            examples = @ExampleObject(
                                    name = "SuccessfulCalculation",
                                    summary = "Successful weighted average calculation",
                                    description = "Example calculation: (100×10.00 + 200×12.00) ÷ (100+200) = 11.33",
                                    value = "11.33"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidParameters",
                                            summary = "Invalid calculation parameters",
                                            description = "Request contains invalid parameters such as negative quantities or costs",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:49:10.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Invalid parameters: quantities and costs must be positive",
                                              "path": "/api/v1/equity-stock-costs/calculate-weighted-average"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "MissingParameters",
                                            summary = "Missing required parameters",
                                            description = "Required calculation parameters are missing from the request",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:49:10.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Required parameters missing: currentQuantity, currentTotalCost, newQuantity, newUnitCost",
                                              "path": "/api/v1/equity-stock-costs/calculate-weighted-average"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Insufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Access denied",
                                    description = "User does not have required ADMIN or TRADER role",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:49:10.123+03:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: Only ADMIN and TRADER roles can access cost calculations",
                                      "path": "/api/v1/equity-stock-costs/calculate-weighted-average"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Calculation error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "CalculationError",
                                    summary = "Internal calculation error",
                                    description = "An unexpected error occurred during the weighted average calculation",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:49:10.123+03:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred during cost calculation",
                                      "path": "/api/v1/equity-stock-costs/calculate-weighted-average"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<BigDecimal> calculateWeightedAverageCost(
            @Parameter(description = "Current quantity of shares held", required = true, example = "100")
            Integer currentQuantity,
            @Parameter(description = "Current total cost of existing holdings", required = true, example = "1000.00")
            BigDecimal currentTotalCost,
            @Parameter(description = "New quantity of shares being added", required = true, example = "200")
            Integer newQuantity,
            @Parameter(description = "Unit cost of new shares being added", required = true, example = "12.00")
            BigDecimal newUnitCost
    );

    @Operation(
            summary = "Test the GARAN.E scenario",
            description = "Tests the complete GARAN.E weighted average cost scenario: " +
                    "Step 1: Initial position of 100 shares @ 10.00 TL = 1,000.00 TL total cost. " +
                    "Step 2: External transfer of 200 shares @ 12.00 TL = 2,400.00 TL additional cost. " +
                    "Result: 300 shares @ 11.33 TL = 3,400.00 TL total cost. " +
                    "This endpoint demonstrates the complete workflow of cost basis tracking through trades and transfers."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Test scenario completed successfully",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    name = "SuccessfulScenario",
                                    summary = "Successful GARAN.E test scenario",
                                    description = "Complete test scenario showing weighted average cost calculation",
                                    value = """
                                    GARAN.E Test Scenario Completed:
                                    Initial: 100 shares @ 10.00 TL = 1,000.00 TL
                                    Transfer: 200 shares @ 12.00 TL = 2,400.00 TL
                                    Final: 300 shares @ 11.33 TL = 3,400.00 TL
                                    Expected: 300 shares @ 11.33 TL = 3,400.00 TL
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters or scenario execution failed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidAccountOrEquity",
                                            summary = "Invalid account or equity ID",
                                            description = "The provided account ID or equity ID is invalid or not found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:49:10.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Account not found with ID: 123",
                                              "path": "/api/v1/equity-stock-costs/test-scenario"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "ScenarioExecutionError",
                                            summary = "Scenario execution failed",
                                            description = "An error occurred during the test scenario execution",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:49:10.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Error: Unable to execute test scenario - insufficient data",
                                              "path": "/api/v1/equity-stock-costs/test-scenario"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - Insufficient permissions",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Access denied",
                                    description = "User does not have required ADMIN or TRADER role",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:49:10.123+03:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: Only ADMIN and TRADER roles can execute test scenarios",
                                      "path": "/api/v1/equity-stock-costs/test-scenario"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account or equity not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "AccountNotFound",
                                            summary = "Account not found",
                                            description = "The specified account could not be found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:49:10.123+03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Account not found with ID: 123",
                                              "path": "/api/v1/equity-stock-costs/test-scenario"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "EquityNotFound",
                                            summary = "Equity not found",
                                            description = "The specified equity could not be found",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:49:10.123+03:00",
                                              "status": 404,
                                              "error": "Not Found",
                                              "message": "Equity not found with ID: 456",
                                              "path": "/api/v1/equity-stock-costs/test-scenario"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected system error during scenario execution",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Internal server error",
                                    description = "An unexpected server error occurred during the test scenario execution",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:49:10.123+03:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred during test scenario execution",
                                      "path": "/api/v1/equity-stock-costs/test-scenario"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<String> testGaranScenario(
            @Parameter(description = "ID of the account to test the scenario with", required = true, example = "1")
            Integer accountId,
            @Parameter(description = "ID of the equity (GARAN.E) to test the scenario with", required = true, example = "1")
            Integer equityId
    );
}
