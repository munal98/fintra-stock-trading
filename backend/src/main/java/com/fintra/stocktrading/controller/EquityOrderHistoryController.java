package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.EquityOrderHistoryControllerDoc;
import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.model.dto.response.EquityOrderHistoryResponse;
import com.fintra.stocktrading.repository.EquityOrderHistoryRepository;
import com.fintra.stocktrading.repository.EquityOrderRepository;
import com.fintra.stocktrading.service.EquityOrderHistoryService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/order-histories")
@RequiredArgsConstructor
public class EquityOrderHistoryController implements EquityOrderHistoryControllerDoc {

    private final EquityOrderHistoryService historyService;
    private final EquityOrderRepository orderRepository;
    private final EquityOrderHistoryRepository historyRepository;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    @Override
    public ResponseEntity<Page<EquityOrderHistoryResponse>> listHistories(
            @RequestParam(required = false) Long accountId,
            @RequestParam(required = false) Long equityId,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(historyService.getHistoriesView(accountId, equityId, pageable));
    }

    @GetMapping("/by-order/{orderId}")
    @Override
    public ResponseEntity<List<EquityOrderHistoryResponse>> getByOrder(@PathVariable Long orderId) {
        orderRepository.findById(orderId.intValue())
                .orElseThrow(() -> new NotFoundException("Order not found"));
        var list = historyRepository.findHistoryViewByOrderId(orderId.intValue());
        return ResponseEntity.ok(list);
    }
}
