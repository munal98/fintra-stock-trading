package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.EquityOrderControllerDoc;
import com.fintra.stocktrading.model.dto.request.EquityOrderRequest;
import com.fintra.stocktrading.model.dto.request.EquityOrderUpdateRequest;
import com.fintra.stocktrading.model.dto.response.EquityOrderResponse;
import com.fintra.stocktrading.service.EquityOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/equity-order")
@RequiredArgsConstructor
@Slf4j
public class EquityOrderController implements EquityOrderControllerDoc {

    private final EquityOrderService equityOrderService;

    @PostMapping
    @Override
    public ResponseEntity<EquityOrderResponse> createOrder(
            @Valid @RequestBody EquityOrderRequest requestDto
    ) {
        log.info("Create order requested: accountId={}, equityId={}, side={}, qty={}, type={}, price={}",
                requestDto.getAccountId(), requestDto.getEquityId(), requestDto.getOrderSide(),
                requestDto.getOrderQuantity(), requestDto.getOrderType(), requestDto.getPrice());
        EquityOrderResponse response = equityOrderService.createOrder(requestDto);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    @Override
    public ResponseEntity<EquityOrderResponse> deleteOrder(@PathVariable Integer orderId) {
        log.info("Cancel order requested: orderId={}", orderId);
        EquityOrderResponse response = equityOrderService.cancelOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}")
    @Override
    public ResponseEntity<EquityOrderResponse> updateOrder(
            @PathVariable Integer orderId,
            @Valid @RequestBody EquityOrderUpdateRequest requestDto
    ) {
        log.info("Update order requested: orderId={}, newQty={}, newPrice={}, newType={}",
                orderId, requestDto.getOrderQuantity(), requestDto.getPrice(), requestDto.getOrderType());
        EquityOrderResponse response = equityOrderService.updateOrder(orderId, requestDto);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}")
    @Override
    public ResponseEntity<EquityOrderResponse> getOrderById(@PathVariable Integer orderId) {
        log.info("Get order by id requested: orderId={}", orderId);
        EquityOrderResponse response = equityOrderService.getOrderById(orderId);
        return ResponseEntity.ok(response);
    }
}
