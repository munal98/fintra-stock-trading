package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.response.EquityDistributionDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@Tag(name = "Equity Distributions", description = "APIs for querying equity distributions")
public interface EquityDistributionControllerDoc {

    @Operation(
            summary = "List all distributions",
            description = "Retrieves all equity distribution records."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of all equity distributions",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EquityDistributionDto.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<List<EquityDistributionDto>> getAllDistributions();

    @Operation(
            summary = "List distributions for a specific order",
            description = "Retrieves all equity distributions for a given order."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "List of equity distributions for the order",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = EquityDistributionDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Order not found or no distributions",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class))
            )
    })
    ResponseEntity<List<EquityDistributionDto>> getDistributionsByOrderId(@PathVariable Integer orderId);
}
