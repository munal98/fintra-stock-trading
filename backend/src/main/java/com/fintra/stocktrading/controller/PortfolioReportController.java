package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.PortfolioReportControllerDoc;
import com.fintra.stocktrading.model.dto.response.PortfolioReportResponse;
import com.fintra.stocktrading.service.PortfolioReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/report")
@RequiredArgsConstructor
public class PortfolioReportController implements PortfolioReportControllerDoc {

    private final PortfolioReportService portfolioReportService;

    @GetMapping("/portfolio")
    @PreAuthorize("hasAnyAuthority('ROLE_ADMIN','ROLE_ANALYST')")
    public PortfolioReportResponse getPortfolio(@RequestParam Integer accountId) {
        return portfolioReportService.getPortfolio(accountId);
    }
}
