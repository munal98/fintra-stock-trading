package com.fintra.stocktrading.model.dto.request;

import com.fintra.stocktrading.model.enums.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request to partially update user information")
public class UserPatchRequest {

    @Email(message = "Email should be valid")
    @Schema(description = "User's email address", example = "admin@example.com")
    private String email;

    @Schema(description = "User's password", example = "password123")
    private String password;

    @Schema(description = "User's first name", example = "Ahmet")
    private String firstName;

    @Schema(description = "User's last name", example = "Yılmaz")
    private String lastName;

    @Schema(description = "User's role in the system", example = "ROLE_ADMIN")
    private Role role;

    @Schema(description = "Whether the user account is enabled", example = "true")
    private Boolean enabled;
}
