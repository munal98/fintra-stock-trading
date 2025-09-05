package com.fintra.stocktrading.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request containing customer IDs for assignment operations")
public class CustomerIdsRequest {

    @NotEmpty(message = "At least one customer ID is required")
    @Schema(description = "List of customer IDs", example = "[1, 2, 3]")
    private List<Integer> customerIds;
}
