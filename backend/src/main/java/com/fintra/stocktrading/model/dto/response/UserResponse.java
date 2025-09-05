package com.fintra.stocktrading.model.dto.response;

import com.fintra.stocktrading.model.enums.Role;
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
@Schema(description = "User information response")
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Integer id;

    @Schema(description = "User's email address", example = "admin@example.com")
    private String email;

    @Schema(description = "User's first name", example = "Ahmet")
    private String firstName;

    @Schema(description = "User's last name", example = "Yılmaz")
    private String lastName;

    @Schema(description = "User's role in the system", example = "ROLE_ADMIN")
    private Role role;

    @Schema(description = "Whether the user account is enabled", example = "true")
    private Boolean enabled;

    @Schema(description = "List of customers assigned to this user (for TRADER role)")
    private List<CustomerResponse> customers;

    @Schema(description = "User creation date")
    private LocalDateTime createdAt;

    @Schema(description = "User last update date")
    private LocalDateTime updatedAt;
}
