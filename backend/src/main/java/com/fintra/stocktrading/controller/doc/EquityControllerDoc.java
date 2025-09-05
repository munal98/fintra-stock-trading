package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.response.EquityInfoResponse;
import com.fintra.stocktrading.model.dto.response.EquityPriceResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Equities", description = "APIs for retrieving equity information and price history from database")
@SecurityRequirement(name = "bearerAuth")
public interface EquityControllerDoc {

    @Operation(
            summary = "Get current equity prices from database",
            description = "Returns a paginated list of the latest equity prices stored in database for frontend display. " +
                         "Supports optional filtering by equity code or name. Results are sorted by equity code in ascending order. " +
                         "Accessible by ADMIN and TRADER roles.",
            tags = {"Equities"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Equity prices retrieved successfully from database",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "Equity Prices Page",
                                    value = """
                                            {
                                              "content": [
                                                {
                                                  "equityId": 1,
                                                  "ticker": "GARAN.E",
                                                  "assetCode": "GARAN.E",
                                                  "equityName": "GARANTI BANKASI A.S.",
                                                  "market": "PAY - ANA PAZAR",
                                                  "openPrice": 85.50,
                                                  "closePrice": 86.25,
                                                  "highPrice": 87.00,
                                                  "lowPrice": 85.10,
                                                  "dataDate": "2025-08-07",
                                                  "participation": false
                                                },
                                                {
                                                  "equityId": 2,
                                                  "ticker": "AKBNK.E",
                                                  "assetCode": "AKBNK.E",
                                                  "equityName": "AKBANK T.A.S.",
                                                  "market": "PAY - ANA PAZAR",
                                                  "openPrice": 45.20,
                                                  "closePrice": 45.80,
                                                  "highPrice": 46.15,
                                                  "lowPrice": 44.95,
                                                  "dataDate": "2025-08-07",
                                                  "participation": false
                                                }
                                              ],
                                              "pageable": {
                                                "pageNumber": 0,
                                                "pageSize": 20,
                                                "sort": {
                                                  "sorted": true,
                                                  "unsorted": false
                                                }
                                              },
                                              "totalElements": 1183,
                                              "totalPages": 60,
                                              "first": true,
                                              "last": false,
                                              "numberOfElements": 2
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Invalid Parameters",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "status": 400,
                                              "message": "Page number must be non-negative",
                                              "path": "/api/v1/equities",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - insufficient privileges",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Access Denied",
                                    value = """
                                            {
                                              "error": "Forbidden",
                                              "status": 403,
                                              "message": "Access denied - ROLE_ADMIN or ROLE_TRADER required",
                                              "path": "/api/v1/equities",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<Page<EquityPriceResponse>> getEquities(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,

            @Parameter(description = "Page size (minimum 1)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) int size,

            @Parameter(description = "Optional filter for equity code or name", example = "GARAN")
            @RequestParam(required = false) String filter
    );

    @Operation(
            summary = "Get price history for equity from database",
            description = "Returns price history for the specified asset code from database. " +
                         "Date range defaults to last 30 days if not specified. " +
                         "Results are ordered by data date in descending order (most recent first). " +
                         "Accessible by ADMIN and TRADER roles.",
            tags = {"Equities"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Price history retrieved successfully from database",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = List.class),
                            examples = @ExampleObject(
                                    name = "Price History",
                                    value = """
                                            [
                                              {
                                                "equityId": 1,
                                                "ticker": "GARAN.E",
                                                "assetCode": "GARAN.E",
                                                "equityName": "GARANTI BANKASI A.S.",
                                                "market": "PAY - ANA PAZAR",
                                                "openPrice": 86.00,
                                                "closePrice": 86.25,
                                                "highPrice": 87.00,
                                                "lowPrice": 85.80,
                                                "dataDate": "2025-08-07",
                                                "participation": false
                                              },
                                              {
                                                "equityId": 1,
                                                "ticker": "GARAN.E",
                                                "assetCode": "GARAN.E",
                                                "equityName": "GARANTI BANKASI A.S.",
                                                "market": "PAY - ANA PAZAR",
                                                "openPrice": 85.25,
                                                "closePrice": 86.00,
                                                "highPrice": 86.50,
                                                "lowPrice": 85.00,
                                                "dataDate": "2025-08-06",
                                                "participation": false
                                              },
                                              {
                                                "equityId": 1,
                                                "ticker": "GARAN.E",
                                                "assetCode": "GARAN.E",
                                                "equityName": "GARANTI BANKASI A.S.",
                                                "market": "PAY - ANA PAZAR",
                                                "openPrice": 84.75,
                                                "closePrice": 85.25,
                                                "highPrice": 85.60,
                                                "lowPrice": 84.50,
                                                "dataDate": "2025-08-05",
                                                "participation": false
                                              }
                                            ]
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid asset code format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Invalid Asset Code",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "status": 400,
                                              "message": "Asset code must contain only uppercase letters, numbers, and dots",
                                              "path": "/api/v1/equities/invalid-code/prices",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - insufficient privileges",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Access Denied",
                                    value = """
                                            {
                                              "error": "Forbidden",
                                              "status": 403,
                                              "message": "Access denied - ROLE_ADMIN or ROLE_TRADER required",
                                              "path": "/api/v1/equities/GARAN.E/prices",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Equity not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Equity Not Found",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "status": 404,
                                              "message": "Equity not found with asset code: NONEXISTENT",
                                              "path": "/api/v1/equities/NONEXISTENT/prices",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<List<EquityPriceResponse>> getPriceHistory(
            @Parameter(description = "Asset code (e.g., GARAN.E, AKBNK.E)", example = "GARAN.E")
            @PathVariable @NotBlank @Pattern(regexp = "^[A-Z0-9.]+$", message = "Asset code must contain only uppercase letters, numbers, and dots")
            String assetCode,

            @Parameter(description = "Start date for price history (YYYY-MM-DD)", example = "2025-07-01")
            @RequestParam(required = false) LocalDate startDate,

            @Parameter(description = "End date for price history (YYYY-MM-DD)", example = "2025-08-07")
            @RequestParam(required = false) LocalDate endDate
    );

    @Operation(
            summary = "Get equity info from database",
            description = "Returns static information (name, market, country, participation flag) for the given asset code from database. " +
                         "This endpoint provides basic equity information without price data. " +
                         "Accessible by ADMIN and TRADER roles.",
            tags = {"Equities"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Equity info retrieved successfully from database",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EquityInfoResponse.class),
                            examples = @ExampleObject(
                                    name = "Equity Info",
                                    value = """
                                            {
                                              "equityId": 1,
                                              "assetCode": "GARAN",
                                              "ticker": "GARAN.E",
                                              "equityName": "GARANTI BANKASI A.S.",
                                              "market": "PAY - ANA PAZAR",
                                              "country": "TR",
                                              "participation": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid asset code format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Invalid Asset Code",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "status": 400,
                                              "message": "Asset code must contain only uppercase letters, numbers, and dots",
                                              "path": "/api/v1/equities/invalid-code/info",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - insufficient privileges",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Access Denied",
                                    value = """
                                            {
                                              "error": "Forbidden",
                                              "status": 403,
                                              "message": "Access denied - ROLE_ADMIN or ROLE_TRADER required",
                                              "path": "/api/v1/equities/GARAN.E/info",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Equity not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Equity Not Found",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "status": 404,
                                              "message": "Equity not found with asset code: NONEXISTENT",
                                              "path": "/api/v1/equities/NONEXISTENT/info",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<EquityInfoResponse> getEquityInfo(
            @Parameter(description = "Asset code (e.g., GARAN.E, AKBNK.E)", example = "GARAN.E")
            @PathVariable @NotBlank @Pattern(regexp = "^[A-Z0-9.]+$", message = "Asset code must contain only uppercase letters, numbers, and dots")
            String assetCode
    );

    @Operation(
            summary = "Get latest price for equity from database",
            description = "Returns the most recent price data for the specified asset code from database. " +
                         "This endpoint provides the latest available price information for a single equity. " +
                         "Accessible by ADMIN and TRADER roles.",
            tags = {"Equities"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Latest price retrieved successfully from database",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EquityPriceResponse.class),
                            examples = @ExampleObject(
                                    name = "Latest Price",
                                    value = """
                                            {
                                              "equityId": 1,
                                              "ticker": "GARAN.E",
                                              "assetCode": "GARAN.E",
                                              "equityName": "GARANTI BANKASI A.S.",
                                              "market": "PAY - ANA PAZAR",
                                              "openPrice": 86.00,
                                              "closePrice": 86.25,
                                              "highPrice": 87.00,
                                              "lowPrice": 85.80,
                                              "dataDate": "2025-08-07",
                                              "participation": false
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid asset code format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Invalid Asset Code",
                                    value = """
                                            {
                                              "error": "Bad Request",
                                              "status": 400,
                                              "message": "Asset code must contain only uppercase letters, numbers, and dots",
                                              "path": "/api/v1/equities/invalid-code/latest",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - insufficient privileges",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Access Denied",
                                    value = """
                                            {
                                              "error": "Forbidden",
                                              "status": 403,
                                              "message": "Access denied - ROLE_ADMIN or ROLE_TRADER required",
                                              "path": "/api/v1/equities/GARAN.E/latest",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Equity not found or no price data available",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "Equity Not Found",
                                    value = """
                                            {
                                              "error": "Not Found",
                                              "status": 404,
                                              "message": "Equity not found with asset code: NONEXISTENT",
                                              "path": "/api/v1/equities/NONEXISTENT/latest",
                                              "timestamp": "2025-08-08T13:43:08.123Z"
                                            }
                                            """
                            )
                    )
            )
    })
    ResponseEntity<EquityPriceResponse> getLatestPrice(
            @Parameter(description = "Asset code (e.g., GARAN.E, AKBNK.E)", example = "GARAN.E")
            @PathVariable @NotBlank @Pattern(regexp = "^[A-Z0-9.]+$", message = "Asset code must contain only uppercase letters, numbers, and dots")
            String assetCode
    );
}
