package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.request.*;
import com.fintra.stocktrading.model.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "User Management", description = "User CRUD operations and customer assignment management")
@SecurityRequirement(name = "bearerAuth")
public interface UserControllerDoc {

    @Operation(
            summary = "Get all users with pagination and filtering",
            description = "Retrieve all users with pagination and optional filtering. The search parameter will look for matches in firstName, lastName, email, and role fields. Only users with ROLE_ADMIN can access this endpoint.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Users retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "Users Page",
                                    value = """
                                            {
                                                "content": [
                                                    {
                                                        "id": 1,
                                                        "email": "admin@example.com",
                                                        "firstName": "Admin",
                                                        "lastName": "User",
                                                        "role": "ROLE_ADMIN",
                                                        "enabled": true,
                                                        "customers": [],
                                                        "createdAt": "2024-01-15T10:30:00",
                                                        "updatedAt": "2024-01-15T10:30:00"
                                                    }
                                                ],
                                                "pageable": {
                                                    "pageNumber": 0,
                                                    "pageSize": 10
                                                },
                                                "totalElements": 1,
                                                "totalPages": 1
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ROLE_ADMIN required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<Page<UserResponse>> getAllUsers(@ParameterObject @Valid UserSearchRequest request);

    @Operation(
            summary = "Create a new user",
            description = "Create a new user in the system. Only users with ROLE_ADMIN can create new users. " +
                         "The enabled field defaults to true if not specified.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "User created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Created User",
                                    value = """
                                            {
                                                "id": 2,
                                                "email": "trader@example.com",
                                                "firstName": "Mehmet",
                                                "lastName": "Demir",
                                                "role": "ROLE_TRADER",
                                                "enabled": true,
                                                "customers": [],
                                                "createdAt": "2024-01-15T10:30:00",
                                                "updatedAt": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ROLE_ADMIN required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request);

    @Operation(
            summary = "Get user by ID",
            description = "Retrieve a specific user by their ID. Only users with ROLE_ADMIN can access this endpoint.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ROLE_ADMIN required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<UserResponse> getUserById(@Parameter(description = "User ID", example = "1") @PathVariable Integer userId);

    @Operation(
            summary = "Update user information",
            description = "Update all user information. Only users with ROLE_ADMIN can update users. " +
                         "All fields in the request are required and will replace existing values.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ROLE_ADMIN required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<UserResponse> updateUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Integer userId,
            @Valid @RequestBody UserUpdateRequest request
    );

    @Operation(
            summary = "Partially update user information",
            description = "Partially update user information. Only users with ROLE_ADMIN can update users. " +
                         "Only provided fields will be updated, others remain unchanged.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ROLE_ADMIN required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "User not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email already exists",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<UserResponse> patchUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Integer userId,
            @RequestBody(description = "User patch request with optional fields", required = true) @Valid UserPatchRequest request
    );

    @Operation(
            summary = "Add customers to trader",
            description = "Add customers to a trader user incrementally (adds to existing assignments). " +
                         "Only users with ROLE_ADMIN can manage customer assignments. The target user must have ROLE_TRADER. " +
                         "If a customer is already assigned to another trader, it will be transferred to the new trader.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customers added successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Trader with Added Customers",
                                    value = """
                                            {
                                                "id": 2,
                                                "email": "trader@example.com",
                                                "firstName": "Mehmet",
                                                "lastName": "Demir",
                                                "role": "ROLE_TRADER",
                                                "enabled": true,
                                                "customers": [
                                                    {
                                                        "customerId": 1,
                                                        "firstName": "Ali",
                                                        "lastName": "Veli",
                                                        "email": "ali@example.com",
                                                        "identityNumber": "12345678901",
                                                        "tradingPermission": "FULL",
                                                        "tradingEnabled": true,
                                                        "accounts": [
                                                            {
                                                                "accountId": 1,
                                                                "accountNumber": "ACC-001",
                                                                "accountType": "INDIVIDUAL",
                                                                "cashBalance": {
                                                                    "balance": 10000.00,
                                                                    "currency": "TRY"
                                                                }
                                                            }
                                                        ],
                                                        "createdAt": "2024-01-15T09:00:00",
                                                        "updatedAt": "2024-01-15T09:00:00"
                                                    },
                                                    {
                                                        "customerId": 2,
                                                        "firstName": "Ayşe",
                                                        "lastName": "Kaya",
                                                        "email": "ayse@example.com",
                                                        "identityNumber": "12345678902",
                                                        "tradingPermission": "LIMITED",
                                                        "tradingEnabled": true,
                                                        "accounts": [
                                                            {
                                                                "accountId": 2,
                                                                "accountNumber": "ACC-002",
                                                                "accountType": "INDIVIDUAL",
                                                                "cashBalance": {
                                                                    "balance": 5000.00,
                                                                    "currency": "TRY"
                                                                }
                                                            }
                                                        ],
                                                        "createdAt": "2024-01-15T09:30:00",
                                                        "updatedAt": "2024-01-15T09:30:00"
                                                    }
                                                ],
                                                "createdAt": "2024-01-15T10:30:00",
                                                "updatedAt": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ROLE_ADMIN required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trader or customer not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User is not a trader",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<UserResponse> addCustomersToTrader(
            @Parameter(description = "Trader user ID", example = "2") @PathVariable Integer traderId,
            @Parameter(description = "Customer IDs to add") 
            @Valid @RequestBody CustomerIdsRequest request
    );

    @Operation(
            summary = "Replace all customer assignments for trader",
            description = "Replace all existing customer assignments for a trader user with new ones. " +
                         "Only users with ROLE_ADMIN can manage customer assignments. The target user must have ROLE_TRADER. " +
                         "All existing assignments will be removed and replaced with the provided customers.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer assignments replaced successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Trader with Replaced Customers",
                                    value = """
                                            {
                                                "id": 2,
                                                "email": "trader@example.com",
                                                "firstName": "Mehmet",
                                                "lastName": "Demir",
                                                "role": "ROLE_TRADER",
                                                "enabled": true,
                                                "customers": [
                                                    {
                                                        "customerId": 3,
                                                        "firstName": "Fatma",
                                                        "lastName": "Öz",
                                                        "email": "fatma@example.com",
                                                        "identityNumber": "12345678903",
                                                        "tradingPermission": "FULL",
                                                        "tradingEnabled": true,
                                                        "accounts": [
                                                            {
                                                                "accountId": 3,
                                                                "accountNumber": "ACC-003",
                                                                "accountType": "INDIVIDUAL",
                                                                "cashBalance": {
                                                                    "balance": 10000.00,
                                                                    "currency": "TRY"
                                                                }
                                                            }
                                                        ],
                                                        "createdAt": "2024-01-15T09:00:00",
                                                        "updatedAt": "2024-01-15T09:00:00"
                                                    }
                                                ],
                                                "createdAt": "2024-01-15T10:30:00",
                                                "updatedAt": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ROLE_ADMIN required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trader or customer not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User is not a trader",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<UserResponse> replaceAllCustomers(
            @Parameter(description = "Trader user ID", example = "2") @PathVariable Integer traderId,
            @Parameter(description = "Customer IDs to assign") 
            @Valid @RequestBody CustomerIdsRequest request
    );

    @Operation(
            summary = "Update customer assignments for trader",
            description = "Add and/or remove specific customers from a trader user. Only users with ROLE_ADMIN can manage customer assignments. " +
                         "The target user must have ROLE_TRADER. This provides granular control over customer assignments.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer assignments updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Updated Trader Assignments",
                                    value = """
                                            {
                                                "id": 2,
                                                "email": "trader@example.com",
                                                "firstName": "Mehmet",
                                                "lastName": "Demir",
                                                "role": "ROLE_TRADER",
                                                "enabled": true,
                                                "customers": [
                                                    {
                                                        "customerId": 3,
                                                        "firstName": "Ayşe",
                                                        "lastName": "Kaya",
                                                        "email": "ayse@example.com",
                                                        "identityNumber": "12345678902",
                                                        "tradingPermission": "LIMITED",
                                                        "tradingEnabled": true,
                                                        "accounts": [
                                                            {
                                                                "accountId": 2,
                                                                "accountNumber": "ACC-002",
                                                                "accountType": "INDIVIDUAL",
                                                                "cashBalance": {
                                                                    "balance": 5000.00,
                                                                    "currency": "TRY"
                                                                }
                                                            }
                                                        ],
                                                        "createdAt": "2024-01-15T09:30:00",
                                                        "updatedAt": "2024-01-15T09:30:00"
                                                    },
                                                    {
                                                        "customerId": 4,
                                                        "firstName": "Fatma",
                                                        "lastName": "Öz",
                                                        "email": "fatma@example.com",
                                                        "identityNumber": "12345678903",
                                                        "tradingPermission": "FULL",
                                                        "tradingEnabled": true,
                                                        "accounts": [
                                                            {
                                                                "accountId": 3,
                                                                "accountNumber": "ACC-003",
                                                                "accountType": "INDIVIDUAL",
                                                                "cashBalance": {
                                                                    "balance": 10000.00,
                                                                    "currency": "TRY"
                                                                }
                                                            }
                                                        ],
                                                        "createdAt": "2024-01-15T09:00:00",
                                                        "updatedAt": "2024-01-15T09:00:00"
                                                    }
                                                ],
                                                "createdAt": "2024-01-15T10:30:00",
                                                "updatedAt": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ROLE_ADMIN required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trader or customer not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User is not a trader",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<UserResponse> updateCustomerAssignments(
            @Parameter(description = "Trader user ID", example = "2") @PathVariable Integer traderId,
            @Parameter(description = "Customer assignment update request with customers to add/remove") 
            @Valid @RequestBody CustomerAssignmentUpdateRequest request
    );

    @Operation(
            summary = "Remove specific customers from trader",
            description = "Remove specific customers from a trader user. Only users with ROLE_ADMIN can manage customer assignments. " +
                         "The target user must have ROLE_TRADER.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customers removed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Trader with Removed Customers",
                                    value = """
                                            {
                                                "id": 2,
                                                "email": "trader@example.com",
                                                "firstName": "Mehmet",
                                                "lastName": "Demir",
                                                "role": "ROLE_TRADER",
                                                "enabled": true,
                                                "customers": [],
                                                "createdAt": "2024-01-15T10:30:00",
                                                "updatedAt": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ROLE_ADMIN required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trader or customer not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User is not a trader",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<UserResponse> removeCustomersFromTrader(
            @Parameter(description = "Trader user ID", example = "2") @PathVariable Integer traderId,
            @Parameter(description = "Customer IDs to remove") 
            @Valid @RequestBody CustomerIdsRequest request
    );

    @Operation(
            summary = "Remove all customers from trader",
            description = "Remove all customers from a trader user. Only users with ROLE_ADMIN can manage customer assignments. " +
                         "The target user must have ROLE_TRADER.",
            tags = {"User Management"}
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "All customers removed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(
                                    name = "Trader with No Customers",
                                    value = """
                                            {
                                                "id": 2,
                                                "email": "trader@example.com",
                                                "firstName": "Mehmet",
                                                "lastName": "Demir",
                                                "role": "ROLE_TRADER",
                                                "enabled": true,
                                                "customers": [],
                                                "createdAt": "2024-01-15T10:30:00",
                                                "updatedAt": "2024-01-15T10:30:00"
                                            }
                                            """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ROLE_ADMIN required",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Trader not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User is not a trader",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class)
                    )
            )
    })
    ResponseEntity<UserResponse> removeAllCustomersFromTrader(
            @Parameter(description = "Trader user ID", example = "2") @PathVariable Integer traderId
    );
}
