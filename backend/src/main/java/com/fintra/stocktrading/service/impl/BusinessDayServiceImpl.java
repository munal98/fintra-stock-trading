package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.repository.EquitySessionRepository;
import com.fintra.stocktrading.service.BusinessDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class BusinessDayServiceImpl implements BusinessDayService {

    private final EquitySessionRepository sessionRepo;

    @Override
    public LocalDate getBusinessDayBefore(LocalDate fromDate, int backDays) {
        LocalDate cursor = fromDate;
        int moved = 0;
        while (moved < backDays) {
            cursor = cursor.minusDays(1);
            if (!isWeekend(cursor)) {
                moved++;
            }
        }
        return cursor;
    }

    @Override
    public LocalDate getBusinessDayAfter(LocalDate fromDate, int forwardDays) {
        LocalDate cursor = fromDate;
        int moved = 0;

        while (moved < forwardDays) {
            cursor = cursor.plusDays(1);
            if (!isWeekend(cursor)) {
                moved++;
            }
        }
        return cursor;
    }

    private boolean isWeekend(LocalDate date) {
        return date.getDayOfWeek().getValue() == 6 || date.getDayOfWeek().getValue() == 7;
    }
}
