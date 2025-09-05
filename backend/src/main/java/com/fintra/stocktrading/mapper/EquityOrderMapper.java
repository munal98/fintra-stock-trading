package com.fintra.stocktrading.mapper;

import com.fintra.stocktrading.model.dto.request.EquityOrderRequest;
import com.fintra.stocktrading.model.dto.response.EquityOrderResponse;
import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityOrder;
import org.springframework.stereotype.Component;

@Component
public class EquityOrderMapper {
    public EquityOrder toEntity(EquityOrderRequest dto, Account account, Equity equity) {
        return EquityOrder.builder()
                .account(account)
                .equity(equity)
                .orderSide(dto.getOrderSide())
                .orderQuantity(dto.getOrderQuantity())
                .orderType(dto.getOrderType())
                .price(dto.getPrice())
                .build();
    }

    public EquityOrderResponse toDto(EquityOrder entity) {
        EquityOrderResponse dto = new EquityOrderResponse();
        dto.setOrderId(entity.getOrderId());
        dto.setAccountId(entity.getAccount().getAccountId());
        dto.setEquityId(entity.getEquity().getEquityId());
        dto.setOrderSide(entity.getOrderSide());
        dto.setOrderQuantity(entity.getOrderQuantity());
        dto.setPrice(entity.getPrice());
        dto.setOrderType(entity.getOrderType());
        dto.setFinalStatus(entity.getFinalStatus());
        dto.setEntryDate(entity.getEntryDate());
        dto.setOrderDate(entity.getOrderDate());
        return dto;
    }
}
