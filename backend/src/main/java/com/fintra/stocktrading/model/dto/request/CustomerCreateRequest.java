package com.fintra.stocktrading.model.dto.request;

import com.fintra.stocktrading.model.enums.AccountType;
import com.fintra.stocktrading.model.enums.TradingPermission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new customer with account")
public class CustomerCreateRequest {

    @NotBlank(message = "First name is required")
    @Schema(description = "Customer's first name", example = "Ahmet")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "Customer's last name", example = "Yılmaz")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "Customer's email address", example = "ahmet.yilmaz@example.com")
    private String email;

    @NotBlank(message = "Identity number is required")
    @Pattern(regexp = "^\\d{10,11}$", message = "Identity number must be 10 digits (Tax Number) for corporate or 11 digits (TC Identity) for individual customers")
    @Schema(description = "Customer's identity number (10 digits for corporate, 11 digits for individual)", example = "12345678901")
    private String identityNumber;

    @NotNull(message = "Trading permission is required")
    @Schema(description = "Customer's trading permission level", example = "FULL")
    private TradingPermission tradingPermission;

    @NotNull(message = "Account type is required")
    @Schema(description = "Type of account to create for the customer", example = "INDIVIDUAL")
    private AccountType accountType;

    @NotNull(message = "User ID is required")
    @Schema(description = "ID of the trader user who will manage this customer", example = "1")
    private Integer userId;
}
