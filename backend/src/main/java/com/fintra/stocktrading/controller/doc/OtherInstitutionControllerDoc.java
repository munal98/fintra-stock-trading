package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.entity.OtherInstitution;
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

@Tag(name = "Other Institutions", description = "APIs for managing other financial institution information with Redis caching support")
public interface OtherInstitutionControllerDoc {

    @Operation(
            summary = "Get all other institutions",
            description = "Retrieves a list of all other financial institutions from the database. " +
                    "This endpoint uses Redis caching to improve performance with a 5-day TTL. " +
                    "Results include institution details such as name, code, and contact information."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of all other institutions retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = OtherInstitution.class)),
                            examples = @ExampleObject(
                                    name = "SuccessfulRetrieval",
                                    summary = "Successful institutions list retrieval",
                                    description = "Example response showing list of financial institutions",
                                    value = """
                                    [
                                      {
                                        "id": 1,
                                        "name": "Garanti BBVA",
                                        "code": "GARAN",
                                        "address": "Levent, Istanbul",
                                        "phone": "+90 212 318 18 18",
                                        "email": "info@garanti.com.tr"
                                      },
                                      {
                                        "id": 2,
                                        "name": "Akbank T.A.Ş.",
                                        "code": "AKBNK",
                                        "address": "Sabancı Center, Istanbul",
                                        "phone": "+90 444 25 25",
                                        "email": "info@akbank.com"
                                      }
                                    ]
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected system error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Internal server error",
                                    description = "An unexpected server error occurred while retrieving the list of institutions",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:56:06.123+03:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred while retrieving institutions",
                                      "path": "/api/v1/other-institution"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<List<OtherInstitution>> getAllOtherInstitutions();

    @Operation(
            summary = "Get other institution by ID",
            description = "Retrieves the details of a specific financial institution by its unique ID. " +
                    "Returns comprehensive information including name, code, address, and contact details."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Other institution found successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OtherInstitution.class),
                            examples = @ExampleObject(
                                    name = "SuccessfulRetrieval",
                                    summary = "Successful institution retrieval",
                                    description = "Example response showing institution details",
                                    value = """
                                    {
                                      "id": 1,
                                      "name": "Garanti BBVA",
                                      "code": "GARAN",
                                      "address": "Levent, Istanbul",
                                      "phone": "+90 212 318 18 18",
                                      "email": "info@garanti.com.tr",
                                      "website": "https://www.garanti.com.tr",
                                      "swiftCode": "TGBATRIS",
                                      "active": true
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Other institution not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "NotFound",
                                    summary = "Institution not found",
                                    description = "The institution with the specified ID could not be found",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:56:06.123+03:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Other institution not found with ID: 999",
                                      "path": "/api/v1/other-institution/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected system error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Internal server error",
                                    description = "An unexpected server error occurred while retrieving the institution",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:56:06.123+03:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred while retrieving institution",
                                      "path": "/api/v1/other-institution/1"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<OtherInstitution> getOtherInstitutionById(
            @Parameter(description = "ID of the institution to retrieve", required = true, example = "1")
            Integer id
    );

    @Operation(
            summary = "Create a new other institution",
            description = "Creates a new record for a financial institution in the system. " +
                    "All required fields must be provided including name, code, and contact information. " +
                    "The cache is automatically invalidated after successful creation."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Other institution created successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OtherInstitution.class),
                            examples = @ExampleObject(
                                    name = "SuccessfulCreation",
                                    summary = "Successful institution creation",
                                    description = "Example response showing newly created institution",
                                    value = """
                                    {
                                      "id": 3,
                                      "name": "İş Bankası A.Ş.",
                                      "code": "ISBTR",
                                      "address": "İş Kuleleri, Istanbul",
                                      "phone": "+90 444 0 444",
                                      "email": "info@isbank.com.tr",
                                      "website": "https://www.isbank.com.tr",
                                      "swiftCode": "ISBKTRIS",
                                      "active": true
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
                            schema = @Schema(implementation = Object.class),
                            examples = {
                                    @ExampleObject(
                                            name = "ValidationError",
                                            summary = "Validation failed",
                                            description = "Request contains invalid data such as missing required fields",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:56:06.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Validation failed",
                                              "path": "/api/v1/other-institution",
                                              "errors": {
                                                "name": "Institution name is required",
                                                "code": "Institution code is required"
                                              }
                                            }
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "DuplicateCode",
                                            summary = "Duplicate institution code",
                                            description = "An institution with the same code already exists",
                                            value = """
                                            {
                                              "timestamp": "2025-08-12T00:56:06.123+03:00",
                                              "status": 400,
                                              "error": "Bad Request",
                                              "message": "Institution with code 'GARAN' already exists",
                                              "path": "/api/v1/other-institution"
                                            }
                                            """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected system error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Internal server error",
                                    description = "An unexpected server error occurred while creating the institution",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:56:06.123+03:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred while creating institution",
                                      "path": "/api/v1/other-institution"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<OtherInstitution> createOtherInstitution(OtherInstitution otherInstitution);

    @Operation(
            summary = "Update an other institution",
            description = "Updates the information of an existing financial institution by its ID. " +
                    "All fields can be updated including name, code, address, and contact information. " +
                    "The cache is automatically invalidated after successful update."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Other institution updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OtherInstitution.class),
                            examples = @ExampleObject(
                                    name = "SuccessfulUpdate",
                                    summary = "Successful institution update",
                                    description = "Example response showing updated institution details",
                                    value = """
                                    {
                                      "id": 1,
                                      "name": "Garanti BBVA (Updated)",
                                      "code": "GARAN",
                                      "address": "New Address, Istanbul",
                                      "phone": "+90 212 318 18 18",
                                      "email": "updated@garanti.com.tr",
                                      "website": "https://www.garanti.com.tr",
                                      "swiftCode": "TGBATRIS",
                                      "active": true
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
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ValidationError",
                                    summary = "Validation failed",
                                    description = "Request contains invalid data such as missing required fields",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:56:06.123+03:00",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Validation failed",
                                      "path": "/api/v1/other-institution/1",
                                      "errors": {
                                        "name": "Institution name cannot be empty"
                                      }
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Other institution not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "NotFound",
                                    summary = "Institution not found",
                                    description = "The institution with the specified ID could not be found for update",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:56:06.123+03:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Other institution not found with ID: 999",
                                      "path": "/api/v1/other-institution/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected system error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Internal server error",
                                    description = "An unexpected server error occurred while updating the institution",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:56:06.123+03:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred while updating institution",
                                      "path": "/api/v1/other-institution/1"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<OtherInstitution> updateOtherInstitution(
            @Parameter(description = "ID of the institution to update", required = true, example = "1")
            Integer id,
            OtherInstitution updatedInstitution
    );

    @Operation(
            summary = "Delete an other institution",
            description = "Deletes a financial institution from the database by its ID. " +
                    "This operation is irreversible and will also invalidate the cache. " +
                    "Ensure the institution is not referenced by any equity transfers before deletion."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Other institution deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Other institution not found",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "NotFound",
                                    summary = "Institution not found",
                                    description = "The institution with the specified ID could not be found for deletion",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:56:06.123+03:00",
                                      "status": 404,
                                      "error": "Not Found",
                                      "message": "Other institution not found with ID: 999",
                                      "path": "/api/v1/other-institution/999"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Conflict - Institution is referenced by other entities",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ReferenceConflict",
                                    summary = "Institution is referenced",
                                    description = "The institution cannot be deleted because it is referenced by equity transfers",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:56:06.123+03:00",
                                      "status": 409,
                                      "error": "Conflict",
                                      "message": "Cannot delete institution: referenced by 5 equity transfers",
                                      "path": "/api/v1/other-institution/1"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal Server Error - Unexpected system error",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Object.class),
                            examples = @ExampleObject(
                                    name = "ServerError",
                                    summary = "Internal server error",
                                    description = "An unexpected server error occurred while deleting the institution",
                                    value = """
                                    {
                                      "timestamp": "2025-08-12T00:56:06.123+03:00",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "An unexpected error occurred while deleting institution",
                                      "path": "/api/v1/other-institution/1"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<Void> deleteOtherInstitution(
            @Parameter(description = "ID of the institution to delete", required = true, example = "1")
            Integer id
    );
}
