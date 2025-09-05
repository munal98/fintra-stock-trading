package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EodServiceImpl implements EodService {

    private final BusinessDayService businessDayService;
    private final TradeSettlementService tradeSettlementService;
    private final EquityOrderExpireService orderExpireService;
    private final SystemDateService systemDateService;

    @Override
    @Transactional
    public void runEndOfDay() {
        LocalDate today = systemDateService.getTDate();
        log.info("Starting EOD process for date: {}", today);

        try {
            log.info("Step 1: Expiring old orders for date {}", today);
            int expiredOrdersCount = orderExpireService.expireOldOrders(today);
            log.info("Step 1 COMPLETED: Expired {} orders", expiredOrdersCount);

            LocalDate tradeDateToSettle = businessDayService.getBusinessDayBefore(today, 2);
            log.info("Step 2: Running T+2 settlement for trades dated {}", tradeDateToSettle);
            int settledTradesCount = tradeSettlementService.settleTradesOnDate(tradeDateToSettle);
            log.info("Step 2 COMPLETED: Settled {} trades for date {}", settledTradesCount, tradeDateToSettle);

            log.info("Step 3: Advancing system date");
            LocalDate nextBusinessDay = businessDayService.getBusinessDayAfter(today, 1);
            LocalDate updated = systemDateService.updateTDate(nextBusinessDay);
            log.info("Step 3 COMPLETED: System date advanced from {} to {}", today, updated);

            log.info("EOD process completed successfully for date: {} (Expired: {} orders, Settled: {} trades)",
                    today, expiredOrdersCount, settledTradesCount);

        } catch (Exception ex) {
            log.error("CRITICAL ERROR during EOD process for date {}: {}", today, ex.getMessage(), ex);
            log.error("Transaction will be rolled back due to this error");

            throw new RuntimeException("EOD process failed for date " + today + ": " + ex.getMessage(), ex);
        }
    }
}
