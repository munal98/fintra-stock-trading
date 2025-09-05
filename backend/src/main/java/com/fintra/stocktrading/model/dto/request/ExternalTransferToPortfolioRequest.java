package com.fintra.stocktrading.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExternalTransferToPortfolioRequest {
    @NotNull(message = "To account ID is required")
    private Integer toAccountId;
    
    @NotNull(message = "Equity ID is required")
    private Integer equityId;
    
    @NotNull(message = "Other institution ID is required")
    private Integer otherInstitutionId;
    
    @NotNull(message = "Transfer quantity is required")
    @Positive(message = "Transfer quantity must be positive")
    private Integer transferQuantity;
    
    private Long tckn_vergi_no;

    private BigDecimal avgCost;
}
