package com.fintra.stocktrading.job;

import com.fintra.stocktrading.service.EodService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EodJob {

    private final EodService eodService;

    @Scheduled(cron = "0 10 17 * * MON-FRI")
    public void triggerEod() {
        eodService.runEndOfDay();
    }
}
