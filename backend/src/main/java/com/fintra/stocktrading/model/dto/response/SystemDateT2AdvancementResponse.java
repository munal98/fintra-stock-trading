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
@Schema(description = "Response for T+2 date advancement operations")
public class SystemDateT2AdvancementResponse {

    @Schema(description = "Original system date before T+2 advancement", example = "2025-08-09")
    private LocalDate originalDate;

    @Schema(description = "System date after first EOD process", example = "2025-08-12")
    private LocalDate afterFirstEod;

    @Schema(description = "Target T+2 date", example = "2025-08-13")
    private LocalDate t2Date;

    @Schema(description = "Final system date after complete T+2 advancement", example = "2025-08-14")
    private LocalDate finalDate;

    @Schema(description = "Descriptive message about the T+2 advancement result",
            example = "System advanced to T+2 and settlement processed")
    private String message;
}
