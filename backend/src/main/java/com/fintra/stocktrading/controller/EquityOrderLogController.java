package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.EquityOrderLogControllerDoc;
import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.mapper.EquityOrderLogMapper;
import com.fintra.stocktrading.model.dto.response.EquityOrderLogResponse;
import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.repository.EquityOrderRepository;
import com.fintra.stocktrading.service.EquityOrderLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-logs")
@RequiredArgsConstructor
public class EquityOrderLogController implements EquityOrderLogControllerDoc {

    private final EquityOrderLogService logService;
    private final EquityOrderRepository orderRepository;

    @GetMapping("/{orderId}")
    @Override
    public ResponseEntity<List<EquityOrderLogResponse>> getOrderLogs(
            @PathVariable Long orderId
    ) {
        EquityOrder order = orderRepository.findById(orderId.intValue())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        List<EquityOrderLogResponse> logs = logService.getLogsForOrder(order)
                .stream()
                .map(EquityOrderLogMapper::toDto)
                .toList();

        return ResponseEntity.ok(logs);
    }
}
