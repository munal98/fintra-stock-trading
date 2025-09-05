package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.EquityDistributionControllerDoc;
import com.fintra.stocktrading.model.dto.response.EquityDistributionDto;
import com.fintra.stocktrading.service.EquityDistributionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/distributions")
@RequiredArgsConstructor
@Slf4j
public class EquityDistributionController implements EquityDistributionControllerDoc {

    private final EquityDistributionService equityDistributionService;

    @Override
    @GetMapping
    public ResponseEntity<List<EquityDistributionDto>> getAllDistributions() {
        log.info("Fetching all equity distributions");
        return ResponseEntity.ok(equityDistributionService.getAllDistributions());
    }

    @Override
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<EquityDistributionDto>> getDistributionsByOrderId(@PathVariable Integer orderId) {
        log.info("Fetching equity distributions for orderId={}", orderId);
        return ResponseEntity.ok(equityDistributionService.getDistributionsByOrderId(orderId));
    }
}
