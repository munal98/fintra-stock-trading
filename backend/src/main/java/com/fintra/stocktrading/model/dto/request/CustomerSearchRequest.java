package com.fintra.stocktrading.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Customer search and pagination request")
public class CustomerSearchRequest {

    @Schema(description = "Search term to find in firstName, lastName, email, tradingPermission, or tradingEnabled (partial match)", example = "Ahmet")
    private String search;

    @Schema(description = "Page number (0-based)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "Page number must be non-negative")
    @Builder.Default
    private Integer page = 0;

    @Schema(description = "Page size", example = "10", defaultValue = "10")
    @Min(value = 1, message = "Page size must be positive")
    @Builder.Default
    private Integer size = 10;
}
