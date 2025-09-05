package com.fintra.stocktrading.model.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Authentication response containing JWT token and user details")
public class AuthResponse {

    @Schema(description = "JWT authentication token", example = "eyJhbGciOiJIUzI1NiJ9...")
    private String token;

    @Schema(description = "User email address", example = "admin@fintra.com.tr")
    private String email;

    @Schema(description = "User role", example = "ROLE_ADMIN")
    private String role;

    @Schema(description = "User ID", example = "1")
    private Integer id;

    @Schema(description = "User first name", example = "Ahmet")
    private String firstName;

    @Schema(description = "User last name", example = "Yılmaz")
    private String lastName;
}
