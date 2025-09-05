package com.fintra.stocktrading.mapper;

import com.fintra.stocktrading.model.dto.response.EquityOrderLogResponse;
import com.fintra.stocktrading.model.entity.EquityOrderLog;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class EquityOrderLogMapper {

    public static EquityOrderLogResponse toDto(EquityOrderLog entity) {
        EquityOrderLogResponse dto = new EquityOrderLogResponse();
        dto.setLogId(entity.getLogId());
        dto.setOrderId(entity.getEquityOrder().getOrderId().longValue());
        dto.setAccountId(entity.getEquityOrder().getAccount().getAccountId().longValue());
        dto.setOrderStatus(entity.getOrderStatus().name());
        dto.setTransactionTime(entity.getTransactionTime().toString());
        return dto;
    }
}
