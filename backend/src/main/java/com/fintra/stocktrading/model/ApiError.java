package com.fintra.stocktrading.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Standard error response")
public class ApiError {
    
    @Schema(description = "Error type", example = "Bad Request")
    private String error;
    
    @Schema(description = "HTTP status code", example = "400")
    private int status;
    
    @Schema(description = "Error message", example = "Invalid request data")
    private String message;
    
    @Schema(description = "Request path", example = "/api/v1/auth/login")
    private String path;
    
    @Schema(description = "Timestamp when error occurred")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    @Schema(description = "Validation errors for specific fields", example = "{\"email\": \"Email is required\", \"password\": \"Password must be at least 8 characters\"}")
    private Map<String, String> validationErrors;
}
