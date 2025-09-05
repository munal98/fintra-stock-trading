package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.response.EquityOrderHistoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Order History", description = "APIs for order history records")
@SecurityRequirement(name = "bearerAuth")
public interface EquityOrderHistoryControllerDoc {

    @Operation(
            summary = "List order history records",
            description = """
                    Lists order history records.
                    - Optional filters: `accountId`, `equityId`
                    - Supports pagination & sorting: `page`, `size`, `sort=transactionTime,desc`
                    Examples:
                    - /api/v1/order-histories?page=0&size=20&sort=transactionTime,desc
                    - /api/v1/order-histories?accountId=5
                    - /api/v1/order-histories?equityId=10
                    - /api/v1/order-histories?accountId=5&equityId=10
                    """
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Paginated order history list",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EquityOrderHistoryResponse.class),
                            examples = @ExampleObject(
                                    name = "PagedList",
                                    summary = "Sample paged response",
                                    value = """
                            {
                              "content": [
                                {
                                  "historyId": 4,
                                  "orderId": 43,
                                  "accountId": 5,
                                  "equityId": 10,
                                  "orderStatus": "PARTIALLY_FILLED",
                                  "orderSide": "BUY",
                                  "orderType": "LIMIT",
                                  "oldOrderQuantity": 100,
                                  "oldPrice": 15.50,
                                  "transactionTime": "2025-08-04T13:12:15"
                                }
                              ],
                              "pageable": { },
                              "totalElements": 1,
                              "totalPages": 1,
                              "size": 20,
                              "number": 0
                            }
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<Page<EquityOrderHistoryResponse>> listHistories(
            Long accountId,
            Long equityId,
            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "Get order history by order",
            description = "Returns all history records for the given order ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Order history list",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = EquityOrderHistoryResponse.class),
                            examples = @ExampleObject(
                                    name = "OrderHistoryList",
                                    value = """
                            [
                              {
                                "historyId": 4,
                                "orderId": 43,
                                "accountId": 5,
                                "equityId": 10,
                                "orderStatus": "FILLED",
                                "orderSide": "SELL",
                                "orderType": "LIMIT",
                                "oldOrderQuantity": 50,
                                "oldPrice": 16.00,
                                "transactionTime": "2025-08-04T13:12:15"
                              }
                            ]
                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<List<EquityOrderHistoryResponse>> getByOrder(Long orderId);
}
