package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.dto.response.SystemDateEodResponse;
import com.fintra.stocktrading.model.dto.response.SystemDateT2AdvancementResponse;
import com.fintra.stocktrading.service.BusinessDayService;
import com.fintra.stocktrading.service.EodService;
import com.fintra.stocktrading.service.SystemDateManagementService;
import com.fintra.stocktrading.service.SystemDateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SystemDateManagementServiceImpl implements SystemDateManagementService {

    private final SystemDateService systemDateService;
    private final EodService eodService;
    private final BusinessDayService businessDayService;

    public SystemDateEodResponse runEndOfDayFlow() {
        LocalDate currentDate = systemDateService.getTDate();
        log.info("Starting EOD flow for system date: {}", currentDate);

        try {
            eodService.runEndOfDay();

            LocalDate dateAfterEod = systemDateService.getTDate();
            log.info("EOD flow completed successfully. Date after EOD: {}", dateAfterEod);

            return SystemDateEodResponse.builder()
                    .systemDate(currentDate)
                    .dateAfterEod(dateAfterEod)
                    .message("EOD processes completed successfully - T+2 settlements processed, expired orders handled")
                    .build();

        } catch (Exception e) {
            log.error("Error during EOD flow for date {}: {}", currentDate, e.getMessage(), e);
            throw e;
        }
    }

    public SystemDateT2AdvancementResponse advanceToT2ForTesting() {
        LocalDate currentDate = systemDateService.getTDate();
        log.info("Starting T+2 advancement for testing from date: {}", currentDate);

        try {
            log.debug("Running first EOD process for current date: {}", currentDate);
            eodService.runEndOfDay();
            LocalDate afterEod = systemDateService.getTDate();
            log.debug("Date after first EOD: {}", afterEod);

            LocalDate t2Date = businessDayService.getBusinessDayAfter(afterEod, 1);
            log.debug("Calculated T+2 date: {}", t2Date);
            LocalDate finalDate = systemDateService.updateTDate(t2Date);
            log.debug("System date updated to: {}", finalDate);

            log.debug("Running second EOD process for T+2 settlement");
            eodService.runEndOfDay();
            LocalDate finalDateAfterEod = systemDateService.getTDate();

            log.info("T+2 advancement completed successfully. Final date: {}", finalDateAfterEod);

            return SystemDateT2AdvancementResponse.builder()
                    .originalDate(currentDate)
                    .afterFirstEod(afterEod)
                    .t2Date(t2Date)
                    .finalDate(finalDateAfterEod)
                    .message("System advanced to T+2 and settlement processed")
                    .build();

        } catch (Exception e) {
            log.error("Error during T+2 advancement from date {}: {}", currentDate, e.getMessage(), e);
            throw e;
        }
    }

    public SystemDateEodResponse triggerEndOfDay() {
        LocalDate currentDate = systemDateService.getTDate();
        log.info("Triggering EOD process manually for system date: {}", currentDate);

        try {
            eodService.runEndOfDay();

            LocalDate newDate = systemDateService.getTDate();
            log.info("EOD process completed successfully. New system date: {}", newDate);

            return SystemDateEodResponse.builder()
                    .systemDate(currentDate)
                    .dateAfterEod(newDate)
                    .message("EOD process completed - orders expired, T+2 settlement processed, date advanced")
                    .build();

        } catch (Exception e) {
            log.error("Error during manual EOD trigger for date {}: {}", currentDate, e.getMessage(), e);
            throw e;
        }
    }

    public LocalDate resetSystemDateToToday() {
        LocalDate currentSystemDate = systemDateService.getTDate();
        LocalDate todayDate = LocalDate.now();
        
        log.info("Resetting system date from {} to today: {}", currentSystemDate, todayDate);
        
        try {
            LocalDate updatedDate = systemDateService.updateTDate(todayDate);
            log.info("System date successfully reset to today: {}", updatedDate);
            return updatedDate;
            
        } catch (Exception e) {
            log.error("Error resetting system date to today: {}", e.getMessage(), e);
            throw e;
        }
    }
}
