package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.EquityTransferControllerDoc;
import com.fintra.stocktrading.model.dto.request.ExternalTransferToPortfolioRequest;
import com.fintra.stocktrading.model.dto.request.PortfolioExternalTransferRequest;
import com.fintra.stocktrading.model.dto.request.PortfolioTransferRequest;
import com.fintra.stocktrading.service.EquityTransferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/equity-transfers")
@RequiredArgsConstructor
public class EquityTransferController implements EquityTransferControllerDoc {

    private final EquityTransferService equityTransferService;

    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    @PostMapping("/portfolio-to-portfolio")
    public ResponseEntity<?> transferPortfolioToPortfolio(@RequestBody @Valid PortfolioTransferRequest request) {
        equityTransferService.performTransferToPortfolio(request);
        return ResponseEntity.ok("Portfolio-to-portfolio transfer completed successfully.");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    @PostMapping("/portfolio-to-external")
    public ResponseEntity<?> transferPortfolioToExternal(@RequestBody @Valid PortfolioExternalTransferRequest request) {
        equityTransferService.performTransferToExternal(request);
        return ResponseEntity.ok("Portfolio-to-external transfer completed successfully.");
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    @PostMapping("/external-to-portfolio")
    public ResponseEntity<?> transferFromExternalToPortfolio(@RequestBody @Valid ExternalTransferToPortfolioRequest request) {
        equityTransferService.performTransferFromExternalToPortfolio(request);
        return ResponseEntity.ok("External-to-portfolio transfer completed successfully.");
    }
}
