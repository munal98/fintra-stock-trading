package com.fintra.stocktrading.model.dto.request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CashTransferRequest {
    private Integer senderAccountId;
    private Integer receiverAccountId;
    private BigDecimal amount;
}
