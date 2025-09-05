package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.request.LoginRequest;
import com.fintra.stocktrading.model.dto.response.AuthResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "APIs for user authentication")
public interface AuthControllerDoc {

    @Operation(
            summary = "User login",
            description = "Authenticate user with email and password to get JWT token. " +
                    "Use the provided credentials from DataInitializer or create new users. " +
                    "Available test users: admin@fintra.com.tr (ADMIN), zeynep.demir@fintra.com.tr (TRADER), " +
                    "mehmet.can@fintra.com.tr (ANALYST). Default password: Admin123!"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login successful - Returns JWT token with user information",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = @ExampleObject(
                                    name = "AdminLoginSuccess",
                                    summary = "Successful admin login",
                                    description = "User successfully authenticated with valid admin credentials",
                                    value = """
                                    {
                                      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhZG1pbkBmaW50cmEuY29tLnRyIiwiaWF0IjoxNzIzMzE2NTI1LCJleHAiOjE3MjM0MDI5MjV9.xyz123...",
                                      "email": "admin@fintra.com.tr",
                                      "role": "ROLE_ADMIN",
                                      "id": 1,
                                      "firstName": "Ahmet",
                                      "lastName": "Yılmaz"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failed - Invalid request data format",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidEmailFormat",
                                            summary = "Invalid email format",
                                            description = "Email must be in fintra.com.tr domain format",
                                            value = """
                                            {
                                              "error": "Validation Failed",
                                              "status": 400,
                                              "message": "Invalid input data",
                                              "path": "/api/v1/auth/login",
                                              "timestamp": "2025-08-11T14:12:05.019Z",
                                              "validationErrors": {
                                                "email": "Email must be in format: username@fintra.com.tr"
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "WeakPassword",
                                            summary = "Password validation failed",
                                            description = "Password doesn't meet security requirements",
                                            value = """
                                            {
                                              "error": "Validation Failed",
                                              "status": 400,
                                              "message": "Invalid input data",
                                              "path": "/api/v1/auth/login",
                                              "timestamp": "2025-08-11T14:12:05.019Z",
                                              "validationErrors": {
                                                "password": "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "MissingFields",
                                            summary = "Required fields missing",
                                            description = "Both email and password are required",
                                            value = """
                                            {
                                              "error": "Validation Failed",
                                              "status": 400,
                                              "message": "Invalid input data",
                                              "path": "/api/v1/auth/login",
                                              "timestamp": "2025-08-11T14:12:05.019Z",
                                              "validationErrors": {
                                                "email": "Email is required",
                                                "password": "Password is required"
                                              }
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Authentication failed - Invalid email or password",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "InvalidCredentials",
                                            summary = "Invalid credentials",
                                            description = "Authentication failed due to incorrect email or password",
                                            value = """
                                            {
                                              "error": "Unauthorized",
                                              "status": 401,
                                              "message": "Invalid email or password",
                                              "path": "/api/v1/auth/login",
                                              "timestamp": "2025-08-11T14:12:05.020Z"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "UserNotFound",
                                            summary = "User not found",
                                            description = "No user found with the provided email address",
                                            value = """
                                            {
                                              "error": "Unauthorized",
                                              "status": 401,
                                              "message": "User not found with email: nonexistent@fintra.com.tr",
                                              "path": "/api/v1/auth/login",
                                              "timestamp": "2025-08-11T14:12:05.020Z"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "AccountLocked",
                                            summary = "Account locked",
                                            description = "User account is locked or disabled",
                                            value = """
                                            {
                                              "error": "Unauthorized",
                                              "status": 401,
                                              "message": "User account is locked",
                                              "path": "/api/v1/auth/login",
                                              "timestamp": "2025-08-11T14:12:05.020Z"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error - Unexpected system error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Internal server error",
                                    description = "Unexpected server error occurred during authentication",
                                    value = """
                                    {
                                      "error": "Internal Server Error",
                                      "status": 500,
                                      "message": "An unexpected error occurred during authentication",
                                      "path": "/api/v1/auth/login",
                                      "timestamp": "2025-08-11T14:12:05.025Z"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest);
}
