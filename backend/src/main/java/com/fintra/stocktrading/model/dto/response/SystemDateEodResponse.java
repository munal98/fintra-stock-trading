package com.fintra.stocktrading.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Response for End of Day (EOD) operations")
public class SystemDateEodResponse {

    @Schema(description = "System date before EOD process", example = "2025-08-09")
    private LocalDate systemDate;

    @Schema(description = "System date after EOD process", example = "2025-08-12")
    private LocalDate dateAfterEod;

    @Schema(description = "Descriptive message about the EOD process result",
            example = "EOD processes completed successfully - T+2 settlements processed, expired orders handled")
    private String message;
}
