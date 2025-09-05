package com.fintra.stocktrading.model.dto.response;

import com.fintra.stocktrading.model.enums.OrderSide;
import com.fintra.stocktrading.model.enums.OrderStatus;
import com.fintra.stocktrading.model.enums.OrderType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class EquityOrderResponse {
    private Integer orderId;
    private Integer accountId;
    private Integer equityId;
    private OrderSide orderSide;
    private Integer orderQuantity;
    private BigDecimal price;
    private OrderStatus finalStatus;
    private LocalDateTime entryDate;
    private LocalDate orderDate;
    private OrderType orderType;
}
