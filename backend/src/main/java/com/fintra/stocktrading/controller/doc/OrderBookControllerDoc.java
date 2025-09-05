package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.dto.response.OrderBookResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Order Book", description = "APIs for retrieving equity orderbook data for trading interface")
@SecurityRequirement(name = "bearerAuth")
public interface OrderBookControllerDoc {

    @Operation(
            summary = "Get orderbook for equity",
            description = "Retrieves all pending, partially filled, and updated orders for a specific equity, " +
                         "grouped as bids (buy orders sorted by price descending) and asks (sell orders sorted by price ascending). " +
                         "Used by trading interface to display current market depth. Accessible by TRADER role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Orderbook retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrderBookResponse.class),
                    examples = @ExampleObject(
                            name = "OrderBookExample",
                            value = """
                                    {
                                      "equityId": 123,
                                      "equityCode": "GARAN.E",
                                      "bids": [
                                        {
                                          "orderId": 1001,
                                          "price": 105.50,
                                          "amount": 100,
                                          "total": 10550.00
                                        },
                                        {
                                          "orderId": 1002,
                                          "price": 105.00,
                                          "amount": 200,
                                          "total": 21000.00
                                        }
                                      ],
                                      "asks": [
                                        {
                                          "orderId": 1003,
                                          "price": 106.00,
                                          "amount": 150,
                                          "total": 15900.00
                                        },
                                        {
                                          "orderId": 1004,
                                          "price": 106.50,
                                          "amount": 100,
                                          "total": 10650.00
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied - TRADER role required",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "AccessDenied",
                            value = """
                                    {
                                      "error": "Forbidden",
                                      "status": 403,
                                      "message": "Access denied - TRADER role required",
                                      "path": "/api/v1/orderbook/123",
                                      "timestamp": "2025-08-11T01:15:58Z"
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Equity not found",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "EquityNotFound",
                            value = """
                                    {
                                      "error": "Not Found",
                                      "status": 404,
                                      "message": "Equity not found with ID: 999",
                                      "path": "/api/v1/orderbook/999",
                                      "timestamp": "2025-08-11T01:15:58Z"
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<OrderBookResponse> getOrderBook(
            @Parameter(description = "Equity ID to get orderbook for", example = "123")
            Integer equityId
    );

    @Operation(
            summary = "Get orderbook excluding matched orders",
            description = "Retrieves all pending, partially filled, and updated orders for a specific equity, " +
                         "excluding the specified order and ALL orders that have been matched with it. " +
                         "This reflects real trading behavior where matched orders are removed from the orderbook. " +
                         "For partial matches, only the matched portion is excluded - remaining quantities stay visible. " +
                         "Orders are grouped as bids and asks with proper sorting. Accessible by TRADER role."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Orderbook retrieved successfully with order excluded",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = OrderBookResponse.class),
                    examples = @ExampleObject(
                            name = "OrderBookExcludingOrderExample",
                            value = """
                                    {
                                      "equityId": 123,
                                      "equityCode": "GARAN.E",
                                      "bids": [
                                        {
                                          "orderId": 1002,
                                          "price": 105.00,
                                          "amount": 200,
                                          "total": 21000.00
                                        }
                                      ],
                                      "asks": [
                                        {
                                          "orderId": 1003,
                                          "price": 106.00,
                                          "amount": 150,
                                          "total": 15900.00
                                        },
                                        {
                                          "orderId": 1004,
                                          "price": 106.50,
                                          "amount": 100,
                                          "total": 10650.00
                                        }
                                      ]
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "403",
            description = "Access denied - TRADER role required",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "AccessDenied",
                            value = """
                                    {
                                      "error": "Forbidden",
                                      "status": 403,
                                      "message": "Access denied - TRADER role required",
                                      "path": "/api/v1/orderbook/123/exclude/1001",
                                      "timestamp": "2025-08-11T01:15:58Z"
                                    }
                                    """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Equity not found",
            content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "EquityNotFound",
                            value = """
                                    {
                                      "error": "Not Found",
                                      "status": 404,
                                      "message": "Equity not found with ID: 999",
                                      "path": "/api/v1/orderbook/999/exclude/1001",
                                      "timestamp": "2025-08-11T01:15:58Z"
                                    }
                                    """
                    )
            )
    )
    ResponseEntity<OrderBookResponse> getOrderBookExcludingOrder(
            @Parameter(description = "Equity ID to get orderbook for", example = "123")
            Integer equityId,
            @Parameter(description = "Order ID to exclude from orderbook", example = "1001")
            Integer orderId
    );
}
