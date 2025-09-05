package com.fintra.stocktrading.model.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to update customer assignments for a trader user")
public class CustomerAssignmentUpdateRequest {

    @Schema(description = "List of customer IDs to add to the trader", example = "[4, 5, 6]")
    private List<Integer> addCustomers;

    @Schema(description = "List of customer IDs to remove from the trader", example = "[1, 2]")
    private List<Integer> removeCustomers;
}
