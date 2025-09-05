package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.EquityStockCostControllerDoc;
import com.fintra.stocktrading.model.entity.EquityStock;
import com.fintra.stocktrading.service.EquityStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/equity-stock-costs")
@RequiredArgsConstructor
@Slf4j
public class EquityStockCostController implements EquityStockCostControllerDoc {

    private final EquityStockService equityStockService;

    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    @GetMapping("/calculate-weighted-average")
    public ResponseEntity<BigDecimal> calculateWeightedAverageCost(
            @RequestParam Integer currentQuantity,
            @RequestParam BigDecimal currentTotalCost,
            @RequestParam Integer newQuantity,
            @RequestParam BigDecimal newUnitCost) {
        
        log.info("Calculating weighted average cost - Current Qty: {}, Current Total Cost: {}, New Qty: {}, New Unit Cost: {}", 
                currentQuantity, currentTotalCost, newQuantity, newUnitCost);
        
        BigDecimal weightedAvgCost = equityStockService.calculateWeightedAverageCost(
                currentQuantity, currentTotalCost, newQuantity, newUnitCost);
        
        return ResponseEntity.ok(weightedAvgCost);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    @PostMapping("/test-scenario")
    public ResponseEntity<String> testGaranScenario(
            @RequestParam Integer accountId,
            @RequestParam Integer equityId) {
        
        log.info("Testing GARAN.E scenario for Account: {}, Equity: {}", accountId, equityId);
        
        try {

            EquityStock step1 = equityStockService.updateCostAfterBuyTrade(
                    accountId, equityId, 100, new BigDecimal("10.00"), BigDecimal.ZERO);
            
            Integer totalQty1 = step1.getFreeQuantity() + step1.getBlockedQuantity();
            BigDecimal totalCost1 = step1.getAvgCost() != null ? step1.getAvgCost().multiply(BigDecimal.valueOf(totalQty1)) : BigDecimal.ZERO;

            EquityStock step2 = equityStockService.updateCostAfterIncomingTransfer(
                    accountId, equityId, 200, new BigDecimal("12.00"));
            
            Integer totalQty2 = step2.getFreeQuantity() + step2.getBlockedQuantity();
            BigDecimal totalCost2 = step2.getAvgCost() != null ? step2.getAvgCost().multiply(BigDecimal.valueOf(totalQty2)) : BigDecimal.ZERO;
            
            log.info("Step 2 - After transfer: {} shares @ {} TL, Total: {} TL", 
                    totalQty2, step2.getAvgCost(), totalCost2);
            
            String result = String.format(
                    "GARAN.E Test Scenario Completed:\n" +
                    "Initial: 100 shares @ 10.00 TL = 1,000.00 TL\n" +
                    "Transfer: 200 shares @ 12.00 TL = 2,400.00 TL\n" +
                    "Final: %d shares @ %s TL = %s TL\n" +
                    "Expected: 300 shares @ 11.33 TL = 3,400.00 TL",
                    totalQty2, 
                    step2.getAvgCost().toString(), 
                    totalCost2.toString()
            );
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Error in test scenario: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
