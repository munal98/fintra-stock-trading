package com.fintra.stocktrading.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CashWithdrawRequest {
    private Integer accountId;
    private BigDecimal amount;
}
