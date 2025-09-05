package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.CashTransactionControllerDoc;
import com.fintra.stocktrading.model.dto.request.CashDepositRequest;
import com.fintra.stocktrading.model.dto.request.CashWithdrawRequest;
import com.fintra.stocktrading.model.dto.response.CashTransactionResponse;
import com.fintra.stocktrading.service.CashTransactionService;
import com.fintra.stocktrading.model.dto.request.CashTransferRequest;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cash")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cash Transactions", description = "APIs for customer cash deposit and withdraw operations")
public class CashTransactionController implements CashTransactionControllerDoc {

    private final CashTransactionService cashTransactionService;

    @PostMapping("/deposit")
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    @Override
    public ResponseEntity<CashTransactionResponse> deposit(@Valid @RequestBody CashDepositRequest request) {
        log.info("Cash deposit requested for accountId: {}, amount: {}", request.getAccountId(), request.getAmount());
        CashTransactionResponse response = cashTransactionService.deposit(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/withdraw")
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    @Override
    public ResponseEntity<CashTransactionResponse> withdraw(@Valid @RequestBody CashWithdrawRequest request) {
        log.info("Cash withdraw requested for accountId: {}, amount: {}", request.getAccountId(), request.getAmount());
        CashTransactionResponse response = cashTransactionService.withdraw(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    @Override
    public ResponseEntity<CashTransactionResponse> transfer(
            @Valid @RequestBody CashTransferRequest request
    ) {
        log.info("Cash transfer requested: from accountId {} to accountId {}, amount: {}",
                request.getSenderAccountId(), request.getReceiverAccountId(), request.getAmount());
        CashTransactionResponse response = cashTransactionService.transfer(request);
        return ResponseEntity.ok(response);
    }
}
