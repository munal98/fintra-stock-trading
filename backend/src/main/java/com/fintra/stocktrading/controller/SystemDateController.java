package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.SystemDateControllerDoc;
import com.fintra.stocktrading.model.dto.response.SystemDateEodResponse;
import com.fintra.stocktrading.model.dto.response.SystemDateT2AdvancementResponse;
import com.fintra.stocktrading.service.SystemDateService;
import com.fintra.stocktrading.service.impl.SystemDateManagementServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/system-date")
@RequiredArgsConstructor
@Slf4j
public class SystemDateController implements SystemDateControllerDoc {

    private final SystemDateService systemDateService;
    private final SystemDateManagementServiceImpl systemDateManagementService;

    @Override
    @GetMapping
    public ResponseEntity<LocalDate> getTDate() {
        log.debug("Requesting current system date");

        LocalDate currentDate = systemDateService.getTDate();

        log.debug("Current system date: {}", currentDate);
        return ResponseEntity.ok(currentDate);
    }

    @Override
    @PostMapping("/next")
    public ResponseEntity<LocalDate> moveToNextDate(@RequestBody LocalDate nextDate) {
        log.info("Request to move system date to: {}", nextDate);

        LocalDate updatedDate = systemDateService.updateTDate(nextDate);

        log.info("System date updated successfully to: {}", updatedDate);
        return ResponseEntity.ok(updatedDate);
    }

    @Override
    @PostMapping("/run")
    public ResponseEntity<SystemDateEodResponse> runEndOfDayFlow() {
        log.info("EOD flow requested via /run endpoint");
        
        SystemDateEodResponse response = systemDateManagementService.runEndOfDayFlow();
        
        log.info("EOD flow completed successfully via /run endpoint");
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/advance-t2")
    public ResponseEntity<SystemDateT2AdvancementResponse> advanceToT2ForTesting() {
        log.info("T+2 advancement requested for testing");
        
        SystemDateT2AdvancementResponse response = systemDateManagementService.advanceToT2ForTesting();
        
        log.info("T+2 advancement completed successfully");
        return ResponseEntity.ok(response);
    }

    @Override
    @PostMapping("/trigger-eod")
    public ResponseEntity<SystemDateEodResponse> triggerEndOfDay() {
        log.info("Manual EOD trigger requested");
        
        SystemDateEodResponse response = systemDateManagementService.triggerEndOfDay();
        
        log.info("Manual EOD trigger completed successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-to-today")
    public ResponseEntity<LocalDate> resetToToday() {
        log.info("System date reset to today requested");
        
        LocalDate todayDate = systemDateManagementService.resetSystemDateToToday();
        
        log.info("System date reset to today completed: {}", todayDate);
        return ResponseEntity.ok(todayDate);
    }
}
