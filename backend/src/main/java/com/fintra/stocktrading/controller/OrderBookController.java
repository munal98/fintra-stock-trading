package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.OrderBookControllerDoc;
import com.fintra.stocktrading.model.dto.response.OrderBookResponse;
import com.fintra.stocktrading.service.OrderBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orderbook")
@RequiredArgsConstructor
@Slf4j
public class OrderBookController implements OrderBookControllerDoc {

    private final OrderBookService orderBookService;

    @GetMapping("/{equityId}")
    @PreAuthorize("hasAuthority('ROLE_TRADER')")
    @Override
    public ResponseEntity<OrderBookResponse> getOrderBook(@PathVariable Integer equityId) {
        log.info("Request to get orderbook for equity ID: {}", equityId);
        
        OrderBookResponse response = orderBookService.getOrderBookByEquityId(equityId);
        
        log.info("Successfully retrieved orderbook for equity ID: {} with {} bids and {} asks", 
                equityId, response.getBids().size(), response.getAsks().size());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{equityId}/exclude/{orderId}")
    @PreAuthorize("hasAuthority('ROLE_TRADER')")
    @Override
    public ResponseEntity<OrderBookResponse> getOrderBookExcludingOrder(
            @PathVariable Integer equityId,
            @PathVariable Integer orderId
    ) {
        log.info("Request to get orderbook for equity ID: {} excluding order ID: {}", equityId, orderId);
        
        OrderBookResponse response = orderBookService.getOrderBookByEquityIdExcludingOrder(equityId, orderId);
        
        log.info("Successfully retrieved orderbook for equity ID: {} excluding order ID: {} with {} bids and {} asks", 
                equityId, orderId, response.getBids().size(), response.getAsks().size());
        
        return ResponseEntity.ok(response);
    }
}
