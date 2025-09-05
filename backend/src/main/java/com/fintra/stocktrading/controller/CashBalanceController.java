package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.CashBalanceControllerDoc;
import com.fintra.stocktrading.model.dto.response.CashBalanceResponse;
import com.fintra.stocktrading.service.CashBalanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cash-balance")
@RequiredArgsConstructor
@Slf4j
public class CashBalanceController implements CashBalanceControllerDoc {

    private final CashBalanceService cashBalanceService;

    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    @GetMapping("/{accountId}")
    @Override
    public ResponseEntity<CashBalanceResponse> getBalance(@PathVariable Integer accountId) {
        CashBalanceResponse response = cashBalanceService.getBalanceByAccountId(accountId);
        return ResponseEntity.ok(response);
    }
}
