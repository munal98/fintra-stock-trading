package com.fintra.stocktrading.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to assign customers to a trader user")
public class CustomerAssignmentRequest {

    @NotNull(message = "Trader user ID is required")
    @Schema(description = "ID of the trader user to assign customers to", example = "2")
    private Integer traderId;

    @NotEmpty(message = "At least one customer ID is required")
    @Schema(description = "List of customer IDs to assign to the trader", example = "[1, 2, 3]")
    private List<Integer> customerIds;
}
