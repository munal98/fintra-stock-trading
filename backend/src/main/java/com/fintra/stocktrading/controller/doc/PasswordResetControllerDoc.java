package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.dto.request.PasswordResetRequest;
import com.fintra.stocktrading.model.dto.request.PasswordUpdateRequest;
import com.fintra.stocktrading.model.dto.request.PasswordVerifyRequest;
import com.fintra.stocktrading.model.dto.response.MessageResponse;
import com.fintra.stocktrading.model.ApiError;
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

@Tag(name = "Password Reset", description = "APIs for secure password reset functionality with email-based token verification")
public interface PasswordResetControllerDoc {

    @Operation(
        summary = "Request password reset", 
        description = "Initiates password reset process by sending a 6-digit verification token to the user's email address. " +
                     "The token expires in 15 minutes for security purposes. Only valid Fintra email addresses are accepted."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "Password reset email sent successfully",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class),
                    examples = @ExampleObject(
                        name = "Success",
                        summary = "Password reset email sent",
                        description = "Password reset token successfully sent to user's email",
                        value = """
                        {
                          "message": "Password reset email sent successfully"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Validation failed - Invalid request data format",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                        name = "ValidationError",
                        summary = "Validation failed",
                        description = "Request contains invalid data with field-specific errors",
                        value = """
                        {
                          "error": "Validation Failed",
                          "status": 400,
                          "message": "Invalid input data",
                          "path": "/api/v1/password/reset-request",
                          "timestamp": "2025-08-11T14:42:08.019Z",
                          "validationErrors": {
                            "email": "Email must be in format: username@fintra.com.tr"
                          }
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "404", 
                description = "User not found - No user exists with the provided email",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                        name = "UserNotFound",
                        summary = "User not found",
                        description = "No user found with the provided email address",
                        value = """
                        {
                          "error": "Not Found",
                          "status": 404,
                          "message": "User not found with email: user@fintra.com.tr",
                          "path": "/api/v1/password/reset-request",
                          "timestamp": "2025-08-11T14:42:08.021Z"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500", 
                description = "Email service error - Failed to send reset email",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                        name = "EmailServiceError",
                        summary = "Email service error",
                        description = "Internal server error when sending email",
                        value = """
                        {
                          "error": "Internal Server Error",
                          "status": 500,
                          "message": "Email service temporarily unavailable",
                          "path": "/api/v1/password/reset-request",
                          "timestamp": "2025-08-11T14:42:08.024Z"
                        }
                        """
                    )
                )
            )
    })
    ResponseEntity<MessageResponse> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request);

    @Operation(
        summary = "Verify password reset token", 
        description = "Verifies the 6-digit password reset token received via email. The token must be valid and not expired " +
                     "(15 minutes limit). This step is required before completing the password reset process."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "Token verified successfully",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class),
                    examples = @ExampleObject(
                        name = "Success",
                        summary = "Token verified",
                        description = "Password reset token successfully verified",
                        value = """
                        {
                          "message": "Token verified successfully"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Invalid or expired token - Token validation failed",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = {
                        @ExampleObject(
                            name = "InvalidToken",
                            summary = "Invalid token",
                            description = "Password reset token is invalid",
                            value = """
                            {
                              "error": "Bad Request",
                              "status": 400,
                              "message": "Invalid password reset token",
                              "path": "/api/v1/password/verify-token",
                              "timestamp": "2025-08-11T14:42:08.022Z"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "ExpiredToken",
                            summary = "Token expired",
                            description = "Password reset token has expired (15 minutes limit)",
                            value = """
                            {
                              "error": "Bad Request",
                              "status": 400,
                              "message": "Password reset token has expired",
                              "path": "/api/v1/password/verify-token",
                              "timestamp": "2025-08-11T14:42:08.023Z"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "ValidationError",
                            summary = "Validation failed",
                            description = "Request contains invalid data with field-specific errors",
                            value = """
                            {
                              "error": "Validation Failed",
                              "status": 400,
                              "message": "Invalid input data",
                              "path": "/api/v1/password/verify-token",
                              "timestamp": "2025-08-11T14:42:08.019Z",
                              "validationErrors": {
                                "email": "Email must be in format: username@fintra.com.tr",
                                "token": "Token must be exactly 6 digits"
                              }
                            }
                            """
                        )
                    }
                )
            ),
            @ApiResponse(
                responseCode = "404", 
                description = "User not found - No user exists with the provided email",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                        name = "UserNotFound",
                        summary = "User not found",
                        description = "No user found with the provided email address",
                        value = """
                        {
                          "error": "Not Found",
                          "status": 404,
                          "message": "User not found with email: user@fintra.com.tr",
                          "path": "/api/v1/password/verify-token",
                          "timestamp": "2025-08-11T14:42:08.021Z"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500", 
                description = "Internal server error - Unexpected system error",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                        name = "ServerError",
                        summary = "Internal server error",
                        description = "Unexpected server error occurred",
                        value = """
                        {
                          "error": "Internal Server Error",
                          "status": 500,
                          "message": "An unexpected error occurred",
                          "path": "/api/v1/password/verify-token",
                          "timestamp": "2025-08-11T14:42:08.025Z"
                        }
                        """
                    )
                )
            )
    })
    ResponseEntity<MessageResponse> verifyToken(@Valid @RequestBody PasswordVerifyRequest request);

    @Operation(
        summary = "Complete password reset", 
        description = "Completes the password reset process using the verified token and sets a new password. " +
                     "The token must be valid and not expired (15 minutes limit). Password must meet security requirements: " +
                     "at least 8 characters with uppercase, lowercase, number, and special character."
    )
    @ApiResponses(value = {
            @ApiResponse(
                responseCode = "200", 
                description = "Password reset successfully",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = MessageResponse.class),
                    examples = @ExampleObject(
                        name = "Success",
                        summary = "Password reset completed",
                        description = "User password successfully updated",
                        value = """
                        {
                          "message": "Password reset successfully"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Invalid token, expired token, or password validation failed",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = {
                        @ExampleObject(
                            name = "InvalidToken",
                            summary = "Invalid token",
                            description = "Password reset token is invalid",
                            value = """
                            {
                              "error": "Bad Request",
                              "status": 400,
                              "message": "Invalid password reset token",
                              "path": "/api/v1/password/reset-complete",
                              "timestamp": "2025-08-11T14:42:08.022Z"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "ExpiredToken",
                            summary = "Token expired",
                            description = "Password reset token has expired (15 minutes limit)",
                            value = """
                            {
                              "error": "Bad Request",
                              "status": 400,
                              "message": "Password reset token has expired",
                              "path": "/api/v1/password/reset-complete",
                              "timestamp": "2025-08-11T14:42:08.023Z"
                            }
                            """
                        ),
                        @ExampleObject(
                            name = "ValidationError",
                            summary = "Validation failed",
                            description = "Request contains invalid data with field-specific errors",
                            value = """
                            {
                              "error": "Validation Failed",
                              "status": 400,
                              "message": "Invalid input data",
                              "path": "/api/v1/password/reset-complete",
                              "timestamp": "2025-08-11T14:42:08.019Z",
                              "validationErrors": {
                                "email": "Email must be in format: username@fintra.com.tr",
                                "token": "Token must be exactly 6 digits",
                                "newPassword": "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
                              }
                            }
                            """
                        )
                    }
                )
            ),
            @ApiResponse(
                responseCode = "404", 
                description = "User not found - No user exists with the provided email",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                        name = "UserNotFound",
                        summary = "User not found",
                        description = "No user found with the provided email address",
                        value = """
                        {
                          "error": "Not Found",
                          "status": 404,
                          "message": "User not found with email: user@fintra.com.tr",
                          "path": "/api/v1/password/reset-complete",
                          "timestamp": "2025-08-11T14:42:08.021Z"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "500", 
                description = "Internal server error - Unexpected system error",
                content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ApiError.class),
                    examples = @ExampleObject(
                        name = "ServerError",
                        summary = "Internal server error",
                        description = "Unexpected server error occurred",
                        value = """
                        {
                          "error": "Internal Server Error",
                          "status": 500,
                          "message": "An unexpected error occurred",
                          "path": "/api/v1/password/reset-complete",
                          "timestamp": "2025-08-11T14:42:08.025Z"
                        }
                        """
                    )
                )
            )
    })
    ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordUpdateRequest request);
}
