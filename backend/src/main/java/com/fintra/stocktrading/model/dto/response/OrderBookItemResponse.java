package com.fintra.stocktrading.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Individual orderbook item representing a single order")
public class OrderBookItemResponse {

    @Schema(description = "Order ID", example = "12345")
    private Integer orderId;

    @Schema(description = "Order price", example = "105.50")
    private BigDecimal price;

    @Schema(description = "Order quantity (lot count)", example = "100")
    private Integer amount;

    @Schema(description = "Total value (price * amount)", example = "10550.00")
    private BigDecimal total;
}
