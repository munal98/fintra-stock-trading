package com.fintra.stocktrading.mapper;

import com.fintra.stocktrading.model.dto.response.OrderBookItemResponse;
import com.fintra.stocktrading.model.dto.response.OrderBookResponse;
import com.fintra.stocktrading.model.entity.EquityOrder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class OrderBookMapper {

    public OrderBookItemResponse toOrderBookItemResponse(EquityOrder equityOrder) {
        if (equityOrder == null) {
            return null;
        }

        BigDecimal total = equityOrder.getPrice()
                .multiply(BigDecimal.valueOf(equityOrder.getRemainingQuantity()));

        return OrderBookItemResponse.builder()
                .orderId(equityOrder.getOrderId())
                .price(equityOrder.getPrice())
                .amount(equityOrder.getRemainingQuantity())
                .total(total)
                .build();
    }

    public OrderBookResponse toOrderBookResponse(
            Integer equityId,
            String equityCode,
            List<EquityOrder> buyOrders,
            List<EquityOrder> sellOrders
    ) {
        List<OrderBookItemResponse> bids = buyOrders.stream()
                .map(this::toOrderBookItemResponse)
                .collect(Collectors.toList());

        List<OrderBookItemResponse> asks = sellOrders.stream()
                .map(this::toOrderBookItemResponse)
                .collect(Collectors.toList());

        return OrderBookResponse.builder()
                .equityId(equityId)
                .equityCode(equityCode)
                .bids(bids)
                .asks(asks)
                .build();
    }
}
