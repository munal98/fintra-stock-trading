package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.request.ExternalTransferToPortfolioRequest;
import com.fintra.stocktrading.model.dto.request.PortfolioExternalTransferRequest;
import com.fintra.stocktrading.model.dto.request.PortfolioTransferRequest;
import com.fintra.stocktrading.model.entity.EquityTransfer;

public interface EquityTransferService {

    /**
     * Performs equity transfer between two portfolios within the system.
     * Transfers equity shares from source account to destination account.
     *
     * @param request the portfolio transfer request containing source/destination accounts and transfer details
     * @return the created equity transfer record
     */
    EquityTransfer performTransferToPortfolio(PortfolioTransferRequest request);

    /**
     * Performs equity transfer from portfolio to external institution.
     * Transfers equity shares from internal account to external institution.
     *
     * @param request the external transfer request containing account and transfer details
     * @return the created equity transfer record
     */
    EquityTransfer performTransferToExternal(PortfolioExternalTransferRequest request);

    /**
     * Performs equity transfer from external institution to portfolio.
     * Receives equity shares from external institution into internal account.
     *
     * @param request the external-to-portfolio transfer request containing account and transfer details
     * @return the created equity transfer record
     */
    EquityTransfer performTransferFromExternalToPortfolio(ExternalTransferToPortfolioRequest request);
}
