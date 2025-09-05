package com.fintra.stocktrading.service;

import java.time.LocalDate;

public interface SystemDateService {
    /**
     * Retrieves the current system trading date (T-Date).
     * This represents the current business date used throughout the trading system
     * for all trading operations, settlements, and business logic calculations.
     *
     * @return the current system trading date
     */
    LocalDate getTDate();

    /**
     * Updates the system trading date to the specified next date.
     * This method is typically used for end-of-day processing, system date advancement,
     * and testing scenarios where manual date progression is required.
     *
     * @param nextDate the new trading date to set as the current system date
     * @return the updated system trading date
     * @throws IllegalArgumentException if the nextDate is null or invalid
     */
    LocalDate updateTDate(LocalDate nextDate);
}
