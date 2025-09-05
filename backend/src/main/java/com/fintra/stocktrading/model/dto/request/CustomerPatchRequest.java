package com.fintra.stocktrading.model.dto.request;

import com.fintra.stocktrading.model.enums.TradingPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to partially update customer information")
public class CustomerPatchRequest {

    @Schema(description = "Customer's first name", example = "Ahmet")
    private String firstName;

    @Schema(description = "Customer's last name", example = "Yılmaz")
    private String lastName;

    @Email(message = "Email should be valid")
    @Schema(description = "Customer's email address", example = "ahmet.yilmaz@example.com")
    private String email;

    @Pattern(regexp = "^\\d{10,11}$", message = "Identity number must be 10 digits (Tax Number) for corporate or 11 digits (TC Identity) for individual customers")
    @Schema(description = "Customer's identity number", example = "12345678901")
    private String identityNumber;

    @Schema(description = "Customer's trading permission level", example = "FULL")
    private TradingPermission tradingPermission;

    @Schema(description = "Whether trading is enabled for the customer", example = "true")
    private Boolean tradingEnabled;
}
