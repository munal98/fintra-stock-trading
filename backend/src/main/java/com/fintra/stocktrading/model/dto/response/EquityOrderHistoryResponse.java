package com.fintra.stocktrading.model.dto.response;

import com.fintra.stocktrading.model.enums.OrderSide;
import com.fintra.stocktrading.model.enums.OrderStatus;
import com.fintra.stocktrading.model.enums.OrderType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EquityOrderHistoryResponse {
    private Long historyId;
    private Long orderId;
    private Long accountId;
    private Long equityId;
    private String firstName;
    private String lastName;
    private OrderStatus orderStatus;
    private OrderSide orderSide;
    private OrderType orderType;
    private Integer oldOrderQuantity;
    private BigDecimal oldPrice;
    private String transactionTime;

    public EquityOrderHistoryResponse(
            Long historyId,
            Integer orderId,
            Integer accountId,
            Integer equityId,
            String firstName,
            String lastName,
            OrderStatus orderStatus,
            OrderSide orderSide,
            OrderType orderType,
            Integer oldOrderQuantity,
            BigDecimal oldPrice,
            LocalDateTime transactionTime
    ) {
        this.historyId = historyId;
        this.orderId = orderId != null ? orderId.longValue() : null;
        this.accountId = accountId != null ? accountId.longValue() : null;
        this.equityId = equityId != null ? equityId.longValue() : null;
        this.firstName = firstName;
        this.lastName = lastName;
        this.orderStatus = orderStatus;
        this.orderSide = orderSide;
        this.orderType = orderType;
        this.oldOrderQuantity = oldOrderQuantity;
        this.oldPrice = oldPrice;
        this.transactionTime = (transactionTime != null) ? transactionTime.toString() : null;
    }

    public EquityOrderHistoryResponse(
            Integer historyId, Integer orderId, Integer accountId, Integer equityId,
            String firstName, String lastName,
            OrderStatus orderStatus, OrderSide orderSide, OrderType orderType,
            Integer oldOrderQuantity, BigDecimal oldPrice, LocalDateTime transactionTime
    ) {
        this.historyId = historyId != null ? historyId.longValue() : null;
        this.orderId = orderId != null ? orderId.longValue() : null;
        this.accountId = accountId != null ? accountId.longValue() : null;
        this.equityId = equityId != null ? equityId.longValue() : null;
        this.firstName = firstName;
        this.lastName = lastName;
        this.orderStatus = orderStatus;
        this.orderSide = orderSide;
        this.orderType = orderType;
        this.oldOrderQuantity = oldOrderQuantity;
        this.oldPrice = oldPrice;
        this.transactionTime = (transactionTime != null) ? transactionTime.toString() : null;
    }
}
