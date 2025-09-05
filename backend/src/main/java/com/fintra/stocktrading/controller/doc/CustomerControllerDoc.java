package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.ApiError;
import com.fintra.stocktrading.model.dto.request.CustomerCreateRequest;
import com.fintra.stocktrading.model.dto.request.CustomerSearchRequest;
import com.fintra.stocktrading.model.dto.request.CustomerUpdateRequest;
import com.fintra.stocktrading.model.dto.request.CustomerPatchRequest;
import com.fintra.stocktrading.model.dto.response.CustomerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Customer Management", description = "APIs for customer management with role-based access control")
@SecurityRequirement(name = "bearerAuth")
public interface CustomerControllerDoc {

    @Operation(
            summary = "Get all customers with pagination and filtering (ADMIN only)",
            description = "Retrieve all customers with pagination and optional filtering. The search parameter will look for matches in firstName, lastName, email, tradingPermission, and tradingEnabled fields. Results are sorted by customerId in ascending order. Only accessible by ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved customers",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    summary = "Successful customer list retrieval",
                                    description = "Paginated list of customers with filtering applied",
                                    value = """
                                    {
                                      "content": [
                                        {
                                          "customerId": 1,
                                          "firstName": "Ahmet",
                                          "lastName": "Yılmaz",
                                          "email": "ahmet.yilmaz@example.com",
                                          "identityNumber": "12345678901",
                                          "tradingPermission": "FULL",
                                          "tradingEnabled": true,
                                          "accounts": [
                                            {
                                              "accountId": 1,
                                              "accountType": "INDIVIDUAL",
                                              "cashBalance": {
                                                "balanceId": 1,
                                                "freeBalance": 15000.00,
                                                "blockedBalance": 2000.00,
                                                "totalBalance": 17000.00
                                              },
                                              "equities": [
                                                {
                                                  "assetCode": "GARAN",
                                                  "assetName": "Garanti Bankası A.Ş.",
                                                  "totalQuantity": 100,
                                                  "averageCost": 50.25,
                                                  "closePrice": 55.00,
                                                  "profitLossPercentage": 9.45
                                                },
                                                {
                                                  "assetCode": "AKBNK",
                                                  "assetName": "Akbank T.A.Ş.",
                                                  "totalQuantity": 50,
                                                  "averageCost": 120.00,
                                                  "closePrice": 115.00,
                                                  "profitLossPercentage": -4.17
                                                }
                                              ]
                                            }
                                          ],
                                          "createdAt": "2025-01-05T10:30:00",
                                          "updatedAt": "2025-01-05T15:45:00"
                                        }
                                      ],
                                      "pageable": {
                                        "pageNumber": 0,
                                        "pageSize": 10,
                                        "sort": {
                                          "sorted": true,
                                          "ascending": true
                                        }
                                      },
                                      "totalElements": 25,
                                      "totalPages": 3,
                                      "first": true,
                                      "last": false,
                                      "numberOfElements": 1
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request parameters",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    summary = "Invalid pagination parameters",
                                    description = "Request contains invalid pagination or filter parameters",
                                    value = """
                                    {
                                      "error": "Validation Failed",
                                      "status": 400,
                                      "message": "Invalid input data",
                                      "path": "/api/v1/customers",
                                      "timestamp": "2025-01-05T15:30:00.123Z",
                                      "validationErrors": {
                                        "page": "Page number must be non-negative",
                                        "size": "Page size must be positive",
                                        "sortDir": "Sort direction must be 'asc' or 'desc'"
                                      }
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Insufficient permissions",
                                    description = "Only ADMIN role can access all customers",
                                    value = """
                                    {
                                      "error": "Forbidden",
                                      "status": 403,
                                      "message": "Access denied - ADMIN role required",
                                      "path": "/api/v1/customers",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            )
    })
    ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @Parameter(description = "Customer search and pagination parameters")
            @ParameterObject
            @Valid @ModelAttribute CustomerSearchRequest request
    );

    @Operation(
            summary = "Get assigned customers with pagination and filtering (TRADER only)",
            description = "Retrieve only customers assigned to the current trader with pagination and optional filtering. The search parameter will look for matches in firstName, lastName, email, and tradingPermission fields. Note: tradingEnabled parameter is ignored - only tradingEnabled=true customers are returned."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved assigned customers",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = Page.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    summary = "Successful assigned customer list",
                                    description = "Paginated list of customers assigned to current trader",
                                    value = """
                                    {
                                      "content": [
                                        {
                                          "customerId": 2,
                                          "firstName": "Fatma",
                                          "lastName": "Kaya",
                                          "email": "fatma.kaya@example.com",
                                          "identityNumber": "98765432101",
                                          "tradingPermission": "PARTICIPATION_ONLY",
                                          "tradingEnabled": true,
                                          "accounts": [
                                            {
                                              "accountId": 2,
                                              "accountType": "INDIVIDUAL",
                                              "cashBalance": {
                                                "balanceId": 2,
                                                "freeBalance": 25000.00,
                                                "blockedBalance": 0.00,
                                                "totalBalance": 25000.00
                                              }
                                            }
                                          ],
                                          "createdAt": "2025-01-04T14:20:00",
                                          "updatedAt": "2025-01-05T09:15:00"
                                        }
                                      ],
                                      "pageable": {
                                        "pageNumber": 0,
                                        "pageSize": 10
                                      },
                                      "totalElements": 3,
                                      "totalPages": 1,
                                      "first": true,
                                      "last": true,
                                      "numberOfElements": 1
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - TRADER role required",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Insufficient permissions",
                                    description = "Only TRADER role can access assigned customers",
                                    value = """
                                    {
                                      "error": "Forbidden",
                                      "status": 403,
                                      "message": "Access denied - TRADER role required",
                                      "path": "/api/v1/customers/assigned",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            )
    })
    ResponseEntity<Page<CustomerResponse>> getAssignedCustomers(
            @Parameter(description = "Customer search and pagination parameters (tradingEnabled ignored)")
            @ParameterObject
            @Valid @ModelAttribute CustomerSearchRequest request
    );

    @Operation(
            summary = "Create a new customer with account (ADMIN only)",
            description = "Create a new customer and automatically create an associated account with CashBalance initialized to zero. Only accessible by ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Customer created successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CustomerResponse.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    summary = "Customer created successfully",
                                    description = "New customer with account and cash balance created",
                                    value = """
                                    {
                                      "customerId": 6,
                                      "firstName": "Ali",
                                      "lastName": "Veli",
                                      "email": "ali.veli@example.com",
                                      "identityNumber": "11223344556",
                                      "tradingPermission": "FULL",
                                      "tradingEnabled": true,
                                      "accounts": [
                                        {
                                          "accountId": 6,
                                          "accountType": "INDIVIDUAL",
                                          "cashBalance": {
                                            "balanceId": 6,
                                            "freeBalance": 0.00,
                                            "blockedBalance": 0.00,
                                            "totalBalance": 0.00
                                          }
                                        }
                                      ],
                                      "createdAt": "2025-01-05T15:30:00",
                                      "updatedAt": "2025-01-05T15:30:00"
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - Invalid input data",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    summary = "Invalid customer data",
                                    description = "Request contains invalid customer information",
                                    value = """
                                    {
                                      "error": "Validation Failed",
                                      "status": 400,
                                      "message": "Invalid input data",
                                      "path": "/api/v1/customers",
                                      "timestamp": "2025-01-05T15:30:00.123Z",
                                      "validationErrors": {
                                        "firstName": "First name is required",
                                        "email": "Email should be valid",
                                        "identityNumber": "Identity number must be 10 digits (Tax Number) for corporate or 11 digits (TC Identity) for individual customers"
                                      }
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Insufficient permissions",
                                    description = "Only ADMIN role can create customers",
                                    value = """
                                    {
                                      "error": "Forbidden",
                                      "status": 403,
                                      "message": "Access denied - ADMIN role required",
                                      "path": "/api/v1/customers",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Email or identity number already exists",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "ConflictError",
                                    summary = "Duplicate data conflict",
                                    description = "Customer with same email or identity number already exists",
                                    value = """
                                    {
                                      "error": "Conflict",
                                      "status": 409,
                                      "message": "Email already exists: ali.veli@example.com",
                                      "path": "/api/v1/customers",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            )
    })
    ResponseEntity<CustomerResponse> createCustomer(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Customer creation request with account details",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerCreateRequest.class),
                            examples = @ExampleObject(
                                    name = "CreateRequest",
                                    summary = "Create new customer",
                                    description = "Example request to create a new customer with individual account",
                                    value = """
                                    {
                                      "firstName": "Ali",
                                      "lastName": "Veli",
                                      "email": "ali.veli@example.com",
                                      "identityNumber": "11223344556",
                                      "tradingPermission": "FULL",
                                      "accountType": "INDIVIDUAL",
                                      "userId": 2
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody CustomerCreateRequest request
    );

    @Operation(
            summary = "Update customer information (ADMIN only)",
            description = "Update customer's information including firstName, lastName, email, tradingPermission, and tradingEnabled status. Only accessible by ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer updated successfully",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = CustomerResponse.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    summary = "Customer updated successfully",
                                    description = "Customer information updated with new values",
                                    value = """
                                    {
                                      "customerId": 1,
                                      "firstName": "Ahmet",
                                      "lastName": "Yılmaz Güncellenmiş",
                                      "email": "ahmet.yilmaz.updated@example.com",
                                      "identityNumber": "12345678902",
                                      "tradingPermission": "PARTICIPATION_ONLY",
                                      "tradingEnabled": false,
                                      "accounts": [
                                        {
                                          "accountId": 1,
                                          "accountType": "INDIVIDUAL",
                                          "cashBalance": {
                                            "balanceId": 1,
                                            "freeBalance": 15000.00,
                                            "blockedBalance": 2000.00,
                                            "totalBalance": 17000.00
                                          },
                                          "equities": [
                                            {
                                              "assetCode": "GARAN",
                                              "assetName": "Garanti Bankası A.Ş.",
                                              "totalQuantity": 100,
                                              "averageCost": 50.25,
                                              "closePrice": 55.00,
                                              "profitLossPercentage": 9.45
                                            },
                                            {
                                              "assetCode": "AKBNK",
                                              "assetName": "Akbank T.A.Ş.",
                                              "totalQuantity": 50,
                                              "averageCost": 120.00,
                                              "closePrice": 115.00,
                                              "profitLossPercentage": -4.17
                                            }
                                          ]
                                        }
                                      ],
                                      "createdAt": "2025-01-05T10:30:00",
                                      "updatedAt": "2025-01-05T16:45:00"
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - Invalid input data",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    summary = "Invalid update data",
                                    description = "Request contains invalid customer update information",
                                    value = """
                                    {
                                      "error": "Validation Failed",
                                      "status": 400,
                                      "message": "Invalid input data",
                                      "path": "/api/v1/customers/1",
                                      "timestamp": "2025-01-05T15:30:00.123Z",
                                      "validationErrors": {
                                        "email": "Email should be valid",
                                        "tradingPermission": "Trading permission is required"
                                      }
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Insufficient permissions",
                                    description = "Only ADMIN role can update customers",
                                    value = """
                                    {
                                      "error": "Forbidden",
                                      "status": 403,
                                      "message": "Access denied - ADMIN role required",
                                      "path": "/api/v1/customers/1",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "NotFound",
                                    summary = "Customer not found",
                                    description = "Customer with specified ID does not exist",
                                    value = """
                                    {
                                      "error": "Not Found",
                                      "status": 404,
                                      "message": "Customer not found with ID: 999",
                                      "path": "/api/v1/customers/999",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Email already exists",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "ConflictError",
                                    summary = "Email conflict",
                                    description = "Another customer already uses this email address",
                                    value = """
                                    {
                                      "error": "Conflict",
                                      "status": 409,
                                      "message": "Email already exists: existing@example.com",
                                      "path": "/api/v1/customers/1",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            )
    })
    ResponseEntity<CustomerResponse> updateCustomer(
            @Parameter(description = "Customer ID to update", example = "1") 
            @PathVariable Integer customerId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Customer update request with new information",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerUpdateRequest.class),
                            examples = @ExampleObject(
                                    name = "UpdateRequest",
                                    summary = "Update customer information",
                                    description = "Example request to update customer information including trading status",
                                    value = """
                                    {
                                      "firstName": "Ahmet",
                                      "lastName": "Yılmaz Güncellenmiş",
                                      "email": "ahmet.yilmaz.updated@example.com",
                                      "identityNumber": "12345678902",
                                      "tradingPermission": "PARTICIPATION_ONLY",
                                      "tradingEnabled": false
                                    }
                                    """
                            )
                    )
            )
            @Valid @RequestBody CustomerUpdateRequest request
    );

    @Operation(
            summary = "Partially update customer information (ADMIN only)",
            description = "Partially update customer's information. Only provided fields will be updated, others remain unchanged. Only accessible by ADMIN role."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer updated successfully",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = CustomerResponse.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    summary = "Customer partially updated successfully",
                                    description = "Only firstName was updated, other fields remained unchanged",
                                    value = """
                                    {
                                      "customerId": 1,
                                      "firstName": "Ahmet Güncellenmiş",
                                      "lastName": "Yılmaz",
                                      "email": "ahmet.yilmaz@example.com",
                                      "identityNumber": "12345678901",
                                      "tradingPermission": "FULL",
                                      "tradingEnabled": true,
                                      "accounts": [
                                        {
                                          "accountId": 1,
                                          "accountType": "INDIVIDUAL",
                                          "cashBalance": {
                                            "balanceId": 1,
                                            "freeBalance": 15000.00,
                                            "blockedBalance": 2000.00,
                                            "totalBalance": 17000.00
                                          },
                                          "equities": [
                                            {
                                              "assetCode": "GARAN",
                                              "assetName": "Garanti Bankası A.Ş.",
                                              "totalQuantity": 100,
                                              "averageCost": 50.25,
                                              "closePrice": 55.00,
                                              "profitLossPercentage": 9.45
                                            },
                                            {
                                              "assetCode": "AKBNK",
                                              "assetName": "Akbank T.A.Ş.",
                                              "totalQuantity": 50,
                                              "averageCost": 120.00,
                                              "closePrice": 115.00,
                                              "profitLossPercentage": -4.17
                                            }
                                          ]
                                        }
                                      ],
                                      "createdAt": "2025-01-05T10:30:00",
                                      "updatedAt": "2025-01-05T16:45:00"
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error - Invalid input data",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    summary = "Invalid email format",
                                    description = "Email field contains invalid format",
                                    value = """
                                    {
                                      "error": "Validation Failed",
                                      "status": 400,
                                      "message": "Invalid input data",
                                      "path": "/api/v1/customers/1",
                                      "timestamp": "2025-01-05T15:30:00.123Z",
                                      "validationErrors": {
                                        "email": "Email should be valid"
                                      }
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN role required",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Insufficient permissions",
                                    description = "Only ADMIN role can update customers",
                                    value = """
                                    {
                                      "error": "Forbidden",
                                      "status": 403,
                                      "message": "Access denied - ADMIN role required",
                                      "path": "/api/v1/customers/1",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "NotFound",
                                    summary = "Customer not found",
                                    description = "Customer with specified ID does not exist",
                                    value = """
                                    {
                                      "error": "Not Found",
                                      "status": 404,
                                      "message": "Customer not found with ID: 999",
                                      "path": "/api/v1/customers/999",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Email or Identity Number already exists",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = {
                                    @ExampleObject(
                                            name = "EmailConflict",
                                            summary = "Email conflict",
                                            description = "Another customer already uses this email address",
                                            value = """
                                            {
                                              "error": "Conflict",
                                              "status": 409,
                                              "message": "Email already exists: existing@example.com",
                                              "path": "/api/v1/customers/1",
                                              "timestamp": "2025-01-05T15:30:00.123Z"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "IdentityNumberConflict",
                                            summary = "Identity number conflict",
                                            description = "Another customer already uses this identity number",
                                            value = """
                                            {
                                              "error": "Conflict",
                                              "status": 409,
                                              "message": "Identity number already exists: 12345678902",
                                              "path": "/api/v1/customers/1",
                                              "timestamp": "2025-01-05T15:30:00.123Z"
                                            }
                                            """
                                    )
                            }
                    )
            )
    })
    ResponseEntity<CustomerResponse> patchCustomer(
            @Parameter(description = "Customer ID to update", example = "1") 
            @PathVariable Integer customerId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Customer patch request with optional fields to update",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CustomerPatchRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "PatchSingleField",
                                            summary = "Partial update - single field",
                                            description = "Example request to update only firstName field",
                                            value = """
                                            {
                                              "firstName": "Ahmet Güncellenmiş"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "PatchContactInfo",
                                            summary = "Partial update - contact information",
                                            description = "Example request to update email and lastName",
                                            value = """
                                            {
                                              "lastName": "Yeni Soyadı",
                                              "email": "yeni.email@example.com"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "PatchIdentityNumber",
                                            summary = "Partial update - identity number",
                                            description = "Example request to update only identityNumber field",
                                            value = """
                                            {
                                              "identityNumber": "12345678902"
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "PatchTradingSettings",
                                            summary = "Partial update - trading settings",
                                            description = "Example request to update trading permission and status",
                                            value = """
                                            {
                                              "tradingPermission": "LIMITED",
                                              "tradingEnabled": false
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "PatchMultipleFields",
                                            summary = "Partial update - multiple fields",
                                            description = "Example request to update multiple fields at once",
                                            value = """
                                            {
                                              "firstName": "Ahmet Güncellenmiş",
                                              "lastName": "Yılmaz Güncellenmiş",
                                              "email": "ahmet.guncellenmis@example.com",
                                              "identityNumber": "12345678902",
                                              "tradingPermission": "FULL",
                                              "tradingEnabled": true
                                            }
                                            """
                                    )
                            }
                    )
            )
            @Valid @RequestBody CustomerPatchRequest request
    );

    @Operation(
            summary = "Get customer by ID",
            description = "Retrieve customer information by ID. ADMIN can access any customer, TRADER can only access assigned customers."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer retrieved successfully",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = CustomerResponse.class),
                            examples = @ExampleObject(
                                    name = "Success",
                                    summary = "Customer details retrieved",
                                    description = "Complete customer information with accounts and balances",
                                    value = """
                                    {
                                      "customerId": 1,
                                      "firstName": "Ahmet",
                                      "lastName": "Yılmaz",
                                      "email": "ahmet.yilmaz@example.com",
                                      "identityNumber": "12345678901",
                                      "tradingPermission": "FULL",
                                      "tradingEnabled": true,
                                      "accounts": [
                                        {
                                          "accountId": 1,
                                          "accountType": "INDIVIDUAL",
                                          "cashBalance": {
                                            "balanceId": 1,
                                            "freeBalance": 15000.00,
                                            "blockedBalance": 2000.00,
                                            "totalBalance": 17000.00
                                          },
                                          "equities": [
                                            {
                                              "assetCode": "GARAN",
                                              "assetName": "Garanti Bankası A.Ş.",
                                              "totalQuantity": 100,
                                              "averageCost": 50.25,
                                              "closePrice": 55.00,
                                              "profitLossPercentage": 9.45
                                            },
                                            {
                                              "assetCode": "AKBNK",
                                              "assetName": "Akbank T.A.Ş.",
                                              "totalQuantity": 50,
                                              "averageCost": 120.00,
                                              "closePrice": 115.00,
                                              "profitLossPercentage": -4.17
                                            }
                                          ]
                                        }
                                      ],
                                      "createdAt": "2025-01-05T10:30:00",
                                      "updatedAt": "2025-01-05T15:45:00"
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied - ADMIN or TRADER role required",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "AccessDenied",
                                    summary = "Insufficient permissions",
                                    description = "Only ADMIN or TRADER roles can access customer details",
                                    value = """
                                    {
                                      "error": "Forbidden",
                                      "status": 403,
                                      "message": "Access denied - ADMIN or TRADER role required",
                                      "path": "/api/v1/customers/1",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found or not assigned to current trader",
                    content = @Content(mediaType = "application/json", 
                            schema = @Schema(implementation = ApiError.class),
                            examples = @ExampleObject(
                                    name = "NotFound",
                                    summary = "Customer not accessible",
                                    description = "Customer not found or TRADER trying to access unassigned customer",
                                    value = """
                                    {
                                      "error": "Not Found",
                                      "status": 404,
                                      "message": "Customer not found with ID: 1",
                                      "path": "/api/v1/customers/1",
                                      "timestamp": "2025-01-05T15:30:00.123Z"
                                    }
                                    """
                            ))
            )
    })
    ResponseEntity<CustomerResponse> getCustomerById(
            @Parameter(description = "Customer ID", example = "1") 
            @PathVariable Integer customerId
    );
}
