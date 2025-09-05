package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.request.EquityOrderRequest;
import com.fintra.stocktrading.model.dto.request.EquityOrderUpdateRequest;
import com.fintra.stocktrading.model.dto.response.EquityOrderResponse;
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

@Tag(name = "Equity Orders", description = "APIs for placing, cancelling, and querying equity orders")
@SecurityRequirement(name = "bearerAuth")
public interface EquityOrderControllerDoc {

    @Operation(
            summary = "Create a new equity order",
            description = "Places a new buy or sell order for a specific equity. Accessible to authorized users."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EquityOrderResponse.class),
                            examples = @ExampleObject(
                                    name = "OrderCreated",
                                    summary = "Order created",
                                    value = """
                        {
                          "orderId": 101,
                          "accountId": 45,
                          "equityId": 12,
                          "orderSide": "BUY",
                          "orderQuantity": 100,
                          "price": 102.50,
                          "orderType": "LIMIT",
                          "finalStatus": "PENDING",
                          "entryDate": "2025-08-05T11:20:00",
                          "orderDate": "2025-08-05"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "OrderValidationError",
                                    summary = "Validation error",
                                    value = """
                        {
                          "error": "Validation Failed",
                          "status": 400,
                          "message": "Order quantity must be positive",
                          "path": "/api/v1/equity-order",
                          "timestamp": "2025-08-05T11:21:00Z"
                        }
                        """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account or equity not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<EquityOrderResponse> createOrder(
            @Valid @RequestBody(description = "Create order request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EquityOrderRequest.class),
                            examples = @ExampleObject(
                                    name = "CreateOrderRequest",
                                    value = """
                        {
                          "accountId": 45,
                          "equityId": 12,
                          "orderSide": "BUY",
                          "orderQuantity": 100,
                          "price": 102.50,
                          "orderType": "LIMIT"
                        }
                        """
                            )))
            EquityOrderRequest requestDto
    );

    @Operation(
            summary = "Cancel (delete) an equity order",
            description = "Cancels a pending or updated equity order."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order cancelled successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EquityOrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<EquityOrderResponse> deleteOrder(Integer orderId);

    @Operation(
            summary = "Update an equity order",
            description = "Updates the quantity, price, or type of a pending or updated order."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order updated successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EquityOrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Update not allowed or validation failed",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<EquityOrderResponse> updateOrder(
            Integer orderId,
            @Valid @RequestBody(description = "Update order request",
                    required = true,
                    content = @Content(schema = @Schema(implementation = EquityOrderUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "UpdateOrderRequest",
                                    value = """
                        {
                          "orderQuantity": 150,
                          "price": 101.75,
                          "orderType": "LIMIT"
                        }
                        """
                            )))
            EquityOrderUpdateRequest requestDto
    );

    @Operation(
            summary = "Get order by ID",
            description = "Fetches order details by its ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order details found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EquityOrderResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<EquityOrderResponse> getOrderById(Integer orderId);
}
