package com.fintra.stocktrading.model.dto.request;

import com.fintra.stocktrading.model.enums.OrderType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EquityOrderUpdateRequest {
    private Integer orderQuantity;
    private BigDecimal price;
    private OrderType orderType;
}
