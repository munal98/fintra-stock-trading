package com.fintra.stocktrading.model.dto.response;

import lombok.Data;

@Data
public class EquityOrderLogResponse {
    private Integer logId;
    private Long orderId;
    private Long accountId;
    private String orderStatus;
    private String transactionTime;
}
