package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.response.EquityOrderLogResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Order Logs", description = "APIs for retrieving order log history")
@SecurityRequirement(name = "bearerAuth")
public interface EquityOrderLogControllerDoc {

    @Operation(
            summary = "Retrieve order log history",
            description = "Returns all log records (such as status changes) for a given order."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Order logs successfully retrieved",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = EquityOrderLogResponse.class),
                    examples = @ExampleObject(
                            name = "OrderLogList",
                            summary = "Sample order log records",
                            value = """
                            [
                              {
                                "logId": 12,
                                "orderId": 43,
                                "accountId": 5,
                                "orderStatus": "FILLED",
                                "transactionTime": "2025-08-04T13:12:20"
                              },
                              {
                                "logId": 13,
                                "orderId": 43,
                                "accountId": 5,
                                "orderStatus": "CANCELLED",
                                "transactionTime": "2025-08-04T13:20:00"
                              }
                            ]
                            """
                    )
            )
    )
    @ApiResponse(
            responseCode = "404",
            description = "Order not found",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class))
    )
    ResponseEntity<List<EquityOrderLogResponse>> getOrderLogs(Long orderId);
}
