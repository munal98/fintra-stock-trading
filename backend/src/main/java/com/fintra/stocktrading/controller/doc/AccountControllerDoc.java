package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.dto.request.AccountCreateRequest;
import com.fintra.stocktrading.model.dto.request.AccountPatchRequest;
import com.fintra.stocktrading.model.dto.request.AccountUpdateRequest;
import com.fintra.stocktrading.model.dto.response.AccountResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.util.List;

@Tag(name = "Account Management", description = "APIs for account management with role-based access control")
public interface AccountControllerDoc {

    @Operation(
            summary = "Create a new account for an existing customer (ADMIN only)",
            description = "Creates a new account for an existing customer with the specified account type. " +
                    "A cash balance with zero balance is automatically created for the new account. " +
                    "Only accessible by ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Account created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class),
                            examples = @ExampleObject(
                                    name = "AccountCreated",
                                    summary = "Account created successfully",
                                    description = "Example response when account is created successfully",
                                    value = """
                                    {
                                      "accountId": 3,
                                      "accountType": "CORPORATE",
                                      "cashBalance": {
                                        "balanceId": 3,
                                        "freeBalance": 0.00,
                                        "blockedBalance": 0.00,
                                        "totalBalance": 0.00
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    summary = "Validation error",
                                    description = "Example response when request validation fails",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Validation failed",
                                      "path": "/api/v1/accounts",
                                      "errors": {
                                        "customerId": "Customer ID is required",
                                        "accountType": "Account type is required"
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Access denied",
                                    description = "Example response when access is denied",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: Only ADMIN role can create accounts",
                                      "path": "/api/v1/accounts"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "CustomerNotFound",
                                    summary = "Customer not found",
                                    description = "Example response when customer is not found",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Customer not found with ID: 999",
                                      "path": "/api/v1/accounts"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<AccountResponse> createAccount(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Account creation request with customer ID and account type",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "CreateAccountRequest",
                                    summary = "Create new account",
                                    description = "Example request to create a new corporate account for an existing customer",
                                    value = """
                                    {
                                      "customerId": 1,
                                      "accountType": "CORPORATE"
                                    }
                                    """
                            )
                    )
            )
            AccountCreateRequest request);

    @Operation(
            summary = "Get all accounts for a customer (ADMIN and TRADER)",
            description = "Retrieves all accounts for a specific customer. " +
                    "ADMIN can access any customer's accounts. " +
                    "TRADER can only access accounts of customers assigned to them."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Accounts retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = AccountResponse.class)),
                            examples = @ExampleObject(
                                    name = "AccountsList",
                                    summary = "List of accounts",
                                    description = "Example response with list of accounts for a customer",
                                    value = """
                                    [
                                      {
                                        "accountId": 1,
                                        "accountType": "INDIVIDUAL",
                                        "cashBalance": {
                                          "balanceId": 1,
                                          "freeBalance": 10000.00,
                                          "blockedBalance": 500.00,
                                          "totalBalance": 10500.00
                                        }
                                      },
                                      {
                                        "accountId": 3,
                                        "accountType": "CORPORATE",
                                        "cashBalance": {
                                          "balanceId": 3,
                                          "freeBalance": 50000.00,
                                          "blockedBalance": 0.00,
                                          "totalBalance": 50000.00
                                        }
                                      }
                                    ]
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Access denied",
                                    description = "Example response when TRADER tries to access accounts of a customer not assigned to them",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: You can only access accounts of customers assigned to you",
                                      "path": "/api/v1/accounts/customer/2"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "CustomerNotFound",
                                    summary = "Customer not found",
                                    description = "Example response when customer is not found",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Customer not found with ID: 999",
                                      "path": "/api/v1/accounts/customer/999"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(
            @Parameter(description = "ID of the customer", required = true, example = "1")
            Integer customerId);

    @Operation(
            summary = "Update an existing account (ADMIN only)",
            description = "Updates an existing account with the specified account type. " +
                    "Only accessible by ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class),
                            examples = @ExampleObject(
                                    name = "AccountUpdated",
                                    summary = "Account updated successfully",
                                    description = "Example response when account is updated successfully",
                                    value = """
                                    {
                                      "accountId": 3,
                                      "accountType": "INDIVIDUAL",
                                      "cashBalance": {
                                        "balanceId": 3,
                                        "freeBalance": 5000.00,
                                        "blockedBalance": 0.00,
                                        "totalBalance": 5000.00
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    summary = "Validation error",
                                    description = "Example response when request validation fails",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Validation failed",
                                      "path": "/api/v1/accounts/3",
                                      "errors": {
                                        "accountType": "Account type is required"
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Access denied",
                                    description = "Example response when access is denied",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: Only ADMIN role can update accounts",
                                      "path": "/api/v1/accounts/3"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "AccountNotFound",
                                    summary = "Account not found",
                                    description = "Example response when account is not found",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Account not found with ID: 999",
                                      "path": "/api/v1/accounts/999"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<AccountResponse> updateAccount(
            @Parameter(description = "ID of the account to update", required = true, example = "3")
            Integer accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Account update request with the new account type",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "UpdateAccountRequest",
                                    summary = "Update account",
                                    description = "Example request to update an account to individual type",
                                    value = """
                                    {
                                      "accountType": "INDIVIDUAL"
                                    }
                                    """
                            )
                    )
            )
            AccountUpdateRequest request);

    @Operation(
            summary = "Partially update an existing account (ADMIN only)",
            description = "Partially updates an existing account with the provided fields. " +
                    "Only non-null fields in the request will be updated, others remain unchanged. " +
                    "Only accessible by ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Account partially updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountResponse.class),
                            examples = @ExampleObject(
                                    name = "AccountPatched",
                                    summary = "Account partially updated successfully",
                                    description = "Example response when account is partially updated successfully",
                                    value = """
                                    {
                                      "accountId": 3,
                                      "accountType": "CORPORATE",
                                      "cashBalance": {
                                        "balanceId": 3,
                                        "freeBalance": 5000.00,
                                        "blockedBalance": 0.00,
                                        "totalBalance": 5000.00
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    summary = "Validation error",
                                    description = "Example response when request validation fails",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Validation failed",
                                      "path": "/api/v1/accounts/3",
                                      "errors": {
                                        "accountType": "Invalid account type"
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Access denied",
                                    description = "Example response when access is denied",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: Only ADMIN role can update accounts",
                                      "path": "/api/v1/accounts/3"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "AccountNotFound",
                                    summary = "Account not found",
                                    description = "Example response when account is not found",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Account not found with ID: 999",
                                      "path": "/api/v1/accounts/999"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<AccountResponse> patchAccount(
            @Parameter(description = "ID of the account to update", required = true, example = "3")
            Integer accountId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Account patch request with optional fields to update",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AccountPatchRequest.class),
                            examples = @ExampleObject(
                                    name = "PatchAccountRequest",
                                    summary = "Patch account type",
                                    description = "Example request to partially update an account's type",
                                    value = """
                                    {
                                      "accountType": "CORPORATE"
                                    }
                                    """
                            )
                    )
            )
            AccountPatchRequest request);

    @Operation(
            summary = "Delete an account (ADMIN only)",
            description = "Deletes an existing account and its associated cash balance. " +
                    "Only accessible by ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Account deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Access denied",
                                    description = "Example response when access is denied",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 403,
                                      "error": "Forbidden",
                                      "message": "Access Denied: Only ADMIN role can delete accounts",
                                      "path": "/api/v1/accounts/3"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Account not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "AccountNotFound",
                                    summary = "Account not found",
                                    description = "Example response when account is not found",
                                    value = """
                                    {
                                      "timestamp": "2023-07-15T10:30:45.123+00:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Account not found with ID: 999",
                                      "path": "/api/v1/accounts/999"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<Void> deleteAccount(
            @Parameter(description = "ID of the account to delete", required = true, example = "3")
            Integer accountId);
}
