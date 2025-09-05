package com.fintra.stocktrading.controller.doc;

import com.fintra.stocktrading.model.dto.response.SystemDateEodResponse;
import com.fintra.stocktrading.model.dto.response.SystemDateT2AdvancementResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

@Tag(name = "System Date Management", description = "APIs for system date management and EOD (End of Day) operations for testing T+2 settlement functionality")
public interface SystemDateControllerDoc {

    @Operation(
            summary = "Get current system date",
            description = "Returns the current system trading date (T-Date) used by the application. " +
                    "This date is used for all trading operations and T+2 settlement calculations."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Current system date retrieved successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LocalDate.class),
                            examples = @ExampleObject(
                                    name = "CurrentSystemDate",
                                    summary = "Current system date",
                                    description = "Example response showing current system trading date",
                                    value = "\"2025-08-09\""
                            )
                    )
            )
    })
    ResponseEntity<LocalDate> getTDate();

    @Operation(
            summary = "Set system date to specific date",
            description = "Manually sets the system trading date to a specific date. " +
                    "This endpoint only changes the date without running any EOD processes. " +
                    "Useful for testing scenarios where you need to jump to a specific date."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "System date updated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LocalDate.class),
                            examples = @ExampleObject(
                                    name = "UpdatedSystemDate",
                                    summary = "Updated system date",
                                    description = "Example response showing the new system date",
                                    value = "\"2025-08-15\""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid date format",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "InvalidDateFormat",
                                    summary = "Invalid date format error",
                                    description = "Error when date format is invalid",
                                    value = """
                                    {
                                      "timestamp": "2025-08-09T20:15:30Z",
                                      "status": 400,
                                      "error": "Bad Request",
                                      "message": "Invalid date format",
                                      "path": "/api/v1/system-date/next"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<LocalDate> moveToNextDate(
            @Parameter(
                    description = "Target date to set as system date (YYYY-MM-DD format)",
                    example = "2025-08-15",
                    required = true
            )
            LocalDate nextDate
    );

    @Operation(
            summary = "Run EOD processes without date advancement",
            description = "Executes End of Day (EOD) processes without advancing the system date. " +
                    "This includes: T+2 settlement processing, order expiration handling, " +
                    "and other end-of-day operations. The system date remains unchanged."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "EOD processes completed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SystemDateEodResponse.class),
                            examples = @ExampleObject(
                                    name = "EodCompleted",
                                    summary = "EOD processes completed",
                                    description = "Successful completion of EOD processes with status information",
                                    value = """
                                    {
                                      "systemDate": "2025-08-09",
                                      "dateAfterEod": "2025-08-09",
                                      "message": "EOD processes completed successfully - T+2 settlements processed, expired orders handled"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error during EOD processing",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "EodProcessingError",
                                    summary = "EOD processing error",
                                    description = "Error during EOD process execution",
                                    value = """
                                    {
                                      "timestamp": "2025-08-09T20:15:30Z",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "Error during EOD processing",
                                      "path": "/api/v1/system-date/run"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SystemDateEodResponse> runEndOfDayFlow();

    @Operation(
            summary = "Advance system date by T+2 business days for testing",
            description = "Advances the system date by T+2 business days (skipping weekends) and processes " +
                    "all related settlements. This endpoint simulates the complete T+2 settlement cycle: " +
                    "1) Runs EOD for current date, 2) Advances by 2 business days, 3) Runs EOD again to " +
                    "trigger T+2 settlements. Ideal for testing T+2 settlement functionality."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "System successfully advanced to T+2 and settlements processed",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SystemDateT2AdvancementResponse.class),
                            examples = @ExampleObject(
                                    name = "T2AdvancementSuccess",
                                    summary = "T+2 advancement completed",
                                    description = "Successful T+2 date advancement with settlement processing",
                                    value = """
                                    {
                                      "originalDate": "2025-08-09",
                                      "afterFirstEod": "2025-08-12",
                                      "t2Date": "2025-08-13",
                                      "finalDate": "2025-08-14",
                                      "message": "System advanced to T+2 and settlement processed"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error during T+2 advancement or settlement processing",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "T2AdvancementError",
                                    summary = "T+2 advancement error",
                                    description = "Error during T+2 date advancement or settlement processing",
                                    value = """
                                    {
                                      "timestamp": "2025-08-09T20:15:30Z",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "Error during T+2 settlement processing",
                                      "path": "/api/v1/system-date/advance-t2"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SystemDateT2AdvancementResponse> advanceToT2ForTesting();

    @Operation(
            summary = "Trigger End of Day process (simulates 17:10 cron job)",
            description = "Simulates the daily 17:10 cron job by running the complete EOD process. " +
                    "This includes: 1) Order expiration processing, 2) T+2 settlement processing, " +
                    "3) System date advancement to next business day. This endpoint replicates " +
                    "the exact behavior of the scheduled daily EOD job."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "EOD process completed successfully with date advancement",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SystemDateEodResponse.class),
                            examples = @ExampleObject(
                                    name = "EodTriggerSuccess",
                                    summary = "EOD trigger completed",
                                    description = "Successful EOD process execution with date advancement",
                                    value = """
                                    {
                                      "systemDate": "2025-08-09",
                                      "dateAfterEod": "2025-08-12",
                                      "message": "EOD process completed - orders expired, T+2 settlement processed, date advanced"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error during EOD process execution",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "EodTriggerError",
                                    summary = "EOD trigger error",
                                    description = "Error during EOD process execution",
                                    value = """
                                    {
                                      "timestamp": "2025-08-09T20:15:30Z",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "Error during EOD process execution",
                                      "path": "/api/v1/system-date/trigger-eod"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<SystemDateEodResponse> triggerEndOfDay();

    @Operation(
            summary = "Reset system date to today",
            description = "Resets the system trading date to the current real-world date (today). " +
                    "This endpoint is useful for testing scenarios where you need to reset the system " +
                    "back to the current date after advancing it for testing purposes. " +
                    "No EOD processes are executed, only the date is reset."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "System date successfully reset to today",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LocalDate.class),
                            examples = @ExampleObject(
                                    name = "ResetToTodaySuccess",
                                    summary = "Date reset to today",
                                    description = "System date successfully reset to current real-world date",
                                    value = "\"2025-08-10\""
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Error during date reset operation",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "ResetToTodayError",
                                    summary = "Date reset error",
                                    description = "Error during system date reset operation",
                                    value = """
                                    {
                                      "timestamp": "2025-08-10T19:10:00Z",
                                      "status": 500,
                                      "error": "Internal Server Error",
                                      "message": "Error resetting system date to today",
                                      "path": "/api/v1/system-date/reset-to-today"
                                    }
                                    """
                            )
                    )
            )
    })
    ResponseEntity<LocalDate> resetToToday();
}
