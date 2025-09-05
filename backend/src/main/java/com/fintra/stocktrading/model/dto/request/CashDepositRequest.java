package com.fintra.stocktrading.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CashDepositRequest {
    private Integer accountId;
    private BigDecimal amount;
}
