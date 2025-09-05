package com.fintra.stocktrading.model.dto.response;

import com.fintra.stocktrading.model.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CashTransactionResponse {
    private Integer transactionId;
    private Integer accountId;
    private BigDecimal amount;
    private TransactionType transactionType;
    private LocalDateTime transactionTime;
    private BigDecimal newFreeBalance;
}
