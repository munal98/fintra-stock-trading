package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.dto.response.EquityOrderHistoryResponse;
import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.entity.EquityOrderHistory;
import com.fintra.stocktrading.repository.EquityOrderHistoryRepository;
import com.fintra.stocktrading.service.EquityOrderHistoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EquityOrderHistoryServiceImpl implements EquityOrderHistoryService {

    private final EquityOrderHistoryRepository historyRepository;

    @Override
    @Transactional
    public void recordHistory(EquityOrder order, Integer oldQty, BigDecimal oldPr, LocalDateTime txnTime) {
        Integer q = (oldQty != null) ? oldQty : order.getOrderQuantity();
        BigDecimal p = (oldPr != null) ? oldPr : order.getPrice();
        LocalDateTime when = (txnTime != null ? txnTime : LocalDateTime.now()).withNano(0);

        var lastOpt = historyRepository.findTopByEquityOrder_OrderIdOrderByTransactionTimeDesc(order.getOrderId());

        if (lastOpt.isPresent()) {
            var last = lastOpt.get();
            boolean noChange =
                    last.getOrderStatus() == order.getFinalStatus() &&
                            last.getOrderSide()   == order.getOrderSide()   &&
                            last.getOrderType()   == order.getOrderType()   &&
                            q.equals(last.getOldOrderQuantity()) &&
                            p.compareTo(last.getOldPrice()) == 0;
            if (noChange) return;
        }
        historyRepository.save(EquityOrderHistory.builder()
                .equityOrder(order)
                .oldOrderQuantity(q)
                .oldPrice(p)
                .orderStatus(order.getFinalStatus())
                .orderSide(order.getOrderSide())
                .orderType(order.getOrderType())
                .transactionTime(when)
                .build());
    }

    @Override
    public Page<EquityOrderHistoryResponse> getHistoriesView(Long accountId, Long equityId, Pageable pageable) {
        Integer acc = accountId == null ? null : accountId.intValue();
        Integer eq  = equityId  == null ? null : equityId.intValue();
        return historyRepository.findLatestPerOrderView(acc, eq, pageable);
    }
}
