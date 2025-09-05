package com.fintra.stocktrading.model.dto.request;

import com.fintra.stocktrading.model.enums.OrderSide;
import com.fintra.stocktrading.model.enums.OrderType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class EquityOrderRequest {
    private Integer accountId;
    private Integer equityId;
    private OrderSide orderSide;
    private Integer orderQuantity;
    private BigDecimal price;
    private OrderType orderType;
}
