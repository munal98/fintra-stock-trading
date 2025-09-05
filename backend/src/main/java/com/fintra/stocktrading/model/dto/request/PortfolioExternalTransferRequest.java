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
public class PortfolioExternalTransferRequest {
    @NotNull(message = "From account ID is required")
    private Integer fromAccountId;
    
    @NotNull(message = "Equity ID is required")
    private Integer equityId;
    
    @NotNull(message = "Transfer quantity is required")
    @Positive(message = "Transfer quantity must be positive")
    private Integer transferQuantity;
    
    @NotNull(message = "Other institution ID is required")
    private Integer otherInstitutionId;
    
    private Long tckn_vergi_no;
}
