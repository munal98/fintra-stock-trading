package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.response.PortfolioReportResponse;

public interface PortfolioReportService {
    
    /**
     * Generates a comprehensive portfolio report for a specific account.
     * Includes cash balances, equity holdings, current market values, and profit/loss calculations.
     *
     * @param accountId the account ID to generate portfolio report for
     * @return portfolio report containing all account positions and performance data
     */
    PortfolioReportResponse getPortfolio(Integer accountId);
}
