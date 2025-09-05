package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.repository.EquityOrderRepository;
import com.fintra.stocktrading.service.OrderMatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/matching")
@RequiredArgsConstructor
@Tag(name = "Order Matching", description = "APIs for manually triggering order matching")
public class OrderMatchingController {

    private final OrderMatchingService orderMatchingService;
    private final EquityOrderRepository equityOrderRepository;

    @PostMapping("/run")
    @Operation(
            summary = "Run full matching",
            description = "Manually triggers matching for all open orders in the system."
    )
    public ResponseEntity<String> runFullMatching() {
        orderMatchingService.matchAllOpenOrders();
        return ResponseEntity.ok("Full order matching completed.");
    }

    @PostMapping("/{orderId}/match")
    @Operation(
            summary = "Run matching for a specific order",
            description = "Manually triggers matching for the given order."
    )
    public ResponseEntity<String> matchSpecificOrder(@PathVariable Integer orderId) {
        EquityOrder order = equityOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with ID: " + orderId));

        orderMatchingService.matchOrder(order);
        return ResponseEntity.ok("Matching completed for order " + orderId);
    }
}
