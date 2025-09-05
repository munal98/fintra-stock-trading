package com.fintra.stocktrading.model.dto.request;

import com.fintra.stocktrading.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to create a new user")
public class UserCreateRequest {

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Schema(description = "User's email address", example = "admin@example.com")
    private String email;

    @NotBlank(message = "Password is required")
    @Schema(description = "User's password", example = "password123")
    private String password;

    @NotBlank(message = "First name is required")
    @Schema(description = "User's first name", example = "Ahmet")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Schema(description = "User's last name", example = "Yılmaz")
    private String lastName;

    @NotNull(message = "Role is required")
    @Schema(description = "User's role in the system", example = "ROLE_ADMIN")
    private Role role;
}
