package com.fintra.stocktrading.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Orderbook response containing bids and asks for a specific equity")
public class OrderBookResponse {

    @Schema(description = "Equity ID", example = "123")
    private Integer equityId;

    @Schema(description = "Equity code", example = "GARAN.E")
    private String equityCode;

    @Schema(description = "Buy orders (bids) sorted by price descending (highest to lowest)")
    private List<OrderBookItemResponse> bids;

    @Schema(description = "Sell orders (asks) sorted by price ascending (lowest to highest)")
    private List<OrderBookItemResponse> asks;
}
