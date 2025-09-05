package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.response.SystemDateEodResponse;
import com.fintra.stocktrading.model.dto.response.SystemDateT2AdvancementResponse;

public interface SystemDateManagementService {

    /**
     * Runs the complete end-of-day flow for the current system date.
     * This includes processing T+2 settlements, handling expired orders, and advancing the date.
     *
     * @return SystemDateEodResponse containing the original date, date after EOD, and success message
     * @throws RuntimeException if EOD flow fails
     */
    SystemDateEodResponse runEndOfDayFlow();

    /**
     * Advances the system date to T+2 for testing purposes.
     * This method runs two EOD processes: one for the current date and another for the T+2 date.
     * Useful for testing T+2 settlement scenarios without waiting for actual business days.
     *
     * @return SystemDateT2AdvancementResponse containing all intermediate dates and final result
     * @throws RuntimeException if T+2 advancement fails
     */
    SystemDateT2AdvancementResponse advanceToT2ForTesting();

    /**
     * Manually triggers the end-of-day process for the current system date.
     * This is equivalent to the automated EOD process that runs at 17:10 daily.
     * Processes order expirations, T+2 settlements, and advances the system date.
     *
     * @return SystemDateEodResponse containing the original date, date after EOD, and success message
     * @throws RuntimeException if manual EOD trigger fails
     */
    SystemDateEodResponse triggerEndOfDay();
}
