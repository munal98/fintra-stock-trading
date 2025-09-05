package com.fintra.stocktrading.model.dto.response;

import com.fintra.stocktrading.model.enums.TradingPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Customer information response")
public class CustomerResponse {

    @Schema(description = "Customer ID", example = "1")
    private Integer customerId;

    @Schema(description = "Customer's first name", example = "Ahmet")
    private String firstName;

    @Schema(description = "Customer's last name", example = "Yılmaz")
    private String lastName;

    @Schema(description = "Customer's email address", example = "ahmet.yilmaz@example.com")
    private String email;

    @Schema(description = "Customer's identity number", example = "12345678901")
    private String identityNumber;

    @Schema(description = "Customer's trading permission level", example = "FULL")
    private TradingPermission tradingPermission;

    @Schema(description = "Whether trading is enabled for the customer", example = "true")
    private Boolean tradingEnabled;

    @Schema(description = "Customer's accounts with cash balance information")
    private List<AccountResponse> accounts;

    @Schema(description = "Customer creation date")
    private LocalDateTime createdAt;

    @Schema(description = "Customer last update date")
    private LocalDateTime updatedAt;
}
