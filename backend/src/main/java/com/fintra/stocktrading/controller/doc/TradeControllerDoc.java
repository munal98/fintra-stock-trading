package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.response.TradeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Trades", description = "APIs for querying trade records")
public interface TradeControllerDoc {

    @Operation(
            summary = "Get trade by trade ID",
            description = "Retrieves the details of a specific trade by its unique trade ID."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Trade details found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TradeDto.class),
                    examples = @ExampleObject(
                            name = "TradeExample",
                            summary = "Trade found",
                            value = """
                            {
                              "tradeId": 202,
                              "matchId": 1801,
                              "equityOrderId": 101,
                              "tradeQuantity": 50,
                              "price": 102.50,
                              "commission": 0.15,
                              "transactionTime": "2025-08-05T11:33:00"
                            }
                            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Trade not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class)
            )
    )
    ResponseEntity<TradeDto> getTradeById(Integer tradeId);

    @Operation(
            summary = "List all trades",
            description = "Retrieves a list of all trade records in the system."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of all trades",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TradeDto.class)
            )
    )
    ResponseEntity<List<TradeDto>> getAllTrades();

    @Operation(
            summary = "List trades for a specific order",
            description = "Retrieves a list of trades associated with a specific equity order."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of trades for the order",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TradeDto.class)
            )
    )
    ResponseEntity<List<TradeDto>> getTradesByOrderId(Integer orderId);

    @Operation(
            summary = "List trades for a specific equity",
            description = "Retrieves a list of trades associated with a specific equity (stock)."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of trades for the equity",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TradeDto.class),
                    examples = @ExampleObject(
                            name = "TradeHistoryExample",
                            summary = "Trades for equity",
                            value = """
                            [
                              {
                                "tradeId": 305,
                                "matchId": 2001,
                                "equityOrderId": 150,
                                "tradeQuantity": 100,
                                "price": 50.25,
                                "commission": 0.10,
                                "transactionTime": "2025-08-08T10:12:00"
                              }
                            ]
                            """
                    )
            )
    )
    ResponseEntity<List<TradeDto>> getTradesByEquityId(Integer equityId);

    @Operation(
            summary = "Get trades for an equity within optional date range",
            description = """
            Retrieves paginated trade records for a specific equityId within the given date range.
            If no date range is provided, defaults to the last 30 days.
            """
    )
    @ApiResponse(
            responseCode = "200",
            description = "Trade list",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TradeDto.class))
    )
    ResponseEntity<Page<TradeDto>> getTradesByEquityWithDateRange(
            @Parameter(description = "Equity ID", example = "10")
            Long equityId,
            @Parameter(description = "Start datetime (ISO-8601)", example = "2025-08-01T00:00:00")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @Parameter(description = "End datetime (ISO-8601)", example = "2025-08-08T23:59:59")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "Get all settled trades",
            description = "Retrieves all trades that have been settled (status = SETTLED). These are trades that have completed the T+2 settlement process."
    )
    @ApiResponse(
            responseCode = "200",
            description = "List of settled trades",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = TradeDto.class),
                    examples = @ExampleObject(
                            name = "SettledTradesExample",
                            summary = "Settled trades found",
                            value = """
                            [
                              {
                                "tradeId": 301,
                                "matchId": 2001,
                                "equityOrderId": 201,
                                "tradeQuantity": 100,
                                "price": 105.75,
                                "commission": 0.25,
                                "status": "SETTLED",
                                "transactionTime": "2025-08-03T14:30:00"
                              },
                              {
                                "tradeId": 302,
                                "matchId": 2002,
                                "equityOrderId": 202,
                                "tradeQuantity": 50,
                                "price": 98.50,
                                "commission": 0.15,
                                "status": "SETTLED",
                                "transactionTime": "2025-08-03T15:45:00"
                              }
                            ]
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
                              "error": "Forbidden",
                              "status": 403,
                              "message": "Access denied - ADMIN or TRADER role required",
                              "path": "/api/v1/trades/settled",
                              "timestamp": "2025-08-10T20:15:00"
                            }
                            """
                    )
            )
    )
    ResponseEntity<List<TradeDto>> getSettledTrades();
}
