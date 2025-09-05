package com.fintra.stocktrading.service;

import java.time.LocalDate;

public interface BusinessDayService {
    
    /**
     * Calculates a business day that is a specified number of days before the given date.
     * Skips weekends (Saturday/Sunday) when counting backward.
     *
     * @param fromDate the starting date
     * @param backDays number of business days to go back
     * @return the calculated business day
     */
    LocalDate getBusinessDayBefore(LocalDate fromDate, int backDays);
    
    /**
     * Calculates a business day that is a specified number of days after the given date.
     * Skips weekends (Saturday/Sunday) when counting forward.
     *
     * @param fromDate the starting date
     * @param forwardDays number of business days to go forward
     * @return the calculated business day
     */
    LocalDate getBusinessDayAfter(LocalDate fromDate, int forwardDays);
}
