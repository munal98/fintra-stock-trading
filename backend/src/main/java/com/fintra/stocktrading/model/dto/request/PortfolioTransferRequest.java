package com.fintra.stocktrading.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PortfolioTransferRequest {
    @NotNull(message = "From account ID is required")
    private Integer fromAccountId;
    
    @NotNull(message = "To account ID is required")
    private Integer toAccountId;
    
    @NotNull(message = "Equity ID is required")
    private Integer equityId;
    
    @NotNull(message = "Transfer quantity is required")
    @Positive(message = "Transfer quantity must be positive")
    private Integer transferQuantity;
}
