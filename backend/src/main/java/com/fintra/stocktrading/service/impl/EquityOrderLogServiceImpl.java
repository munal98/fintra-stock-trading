package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.entity.EquityOrderLog;
import com.fintra.stocktrading.model.enums.OrderStatus;
import com.fintra.stocktrading.repository.EquityOrderLogRepository;
import com.fintra.stocktrading.service.EquityOrderLogService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquityOrderLogServiceImpl implements EquityOrderLogService {

    private final EquityOrderLogRepository logRepository;

    @Override
    @Transactional
    public void logStatusChange(EquityOrder order, OrderStatus status, LocalDateTime txnTime, String desc) {
        EquityOrderLog log = EquityOrderLog.builder()
                .equityOrder(order)
                .orderStatus(status)
                .transactionTime(txnTime != null ? txnTime : LocalDateTime.now())
                .build();
        logRepository.save(log);
    }

    @Override
    public List<EquityOrderLog> getLogsForOrder(EquityOrder order) {
        return logRepository.findByEquityOrder(order);
    }
}
