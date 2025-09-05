package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.model.dto.response.PortfolioHoldingResponse;
import com.fintra.stocktrading.model.dto.response.PortfolioReportResponse;
import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.CashBalance;
import com.fintra.stocktrading.model.entity.EquityStock;
import com.fintra.stocktrading.model.entity.Trade;
import com.fintra.stocktrading.repository.AccountRepository;
import com.fintra.stocktrading.repository.CashBalanceRepository;
import com.fintra.stocktrading.repository.EquityStockRepository;
import com.fintra.stocktrading.repository.TradeRepository;
import com.fintra.stocktrading.service.PortfolioReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioReportServiceImpl implements PortfolioReportService {

    private final AccountRepository accountRepo;
    private final CashBalanceRepository cashBalanceRepo;
    private final EquityStockRepository equityStockRepo;
    private final TradeRepository tradeRepo;

    private static final RoundingMode RM = RoundingMode.HALF_UP;

    @Override
    @Transactional(readOnly = true)
    public PortfolioReportResponse getPortfolio(Integer accountId) {

        Account account = accountRepo.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        String accountTypeStr = (account.getAccountType() != null)
                ? account.getAccountType().name()
                : "N/A";
        String identityNumber = "N/A";
        String fullName = "N/A";

        if (account.getCustomer() != null) {
            if (account.getCustomer().getIdentityNumber() != null) {
                identityNumber = account.getCustomer().getIdentityNumber();
            }
            String fn = account.getCustomer().getFirstName();
            String ln = account.getCustomer().getLastName();
            if (fn == null) fn = "";
            if (ln == null) ln = "";
            String joined = (fn + " " + ln).trim();
            if (!joined.isEmpty()) {
                fullName = joined;
            }
        }

        CashBalance cb = cashBalanceRepo.findByAccount_AccountId(accountId)
                .orElseThrow(() -> new NotFoundException("CashBalance not found for account: " + accountId));

        BigDecimal freeCash = cb.getFreeBalance() == null ? BigDecimal.ZERO : cb.getFreeBalance();
        BigDecimal blockedCash = cb.getBlockedBalance() == null ? BigDecimal.ZERO : cb.getBlockedBalance();
        BigDecimal totalCash = freeCash.add(blockedCash);

        List<EquityStock> stocks = equityStockRepo.findEquityHoldingsByAccountId(accountId);

        List<PortfolioHoldingResponse> rows = new ArrayList<>();
        BigDecimal holdingsValue = BigDecimal.ZERO;
        BigDecimal totalUnreal = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;

        for (EquityStock es : stocks) {
            Integer fqInt = es.getFreeQuantity() == null ? 0 : es.getFreeQuantity();
            Integer bqInt = es.getBlockedQuantity() == null ? 0 : es.getBlockedQuantity();
            BigDecimal freeQty = BigDecimal.valueOf(fqInt);
            BigDecimal blockedQty = BigDecimal.valueOf(bqInt);
            BigDecimal totalQty = freeQty.add(blockedQty);
            if (totalQty.compareTo(BigDecimal.ZERO) <= 0) {
                continue;
            }

            BigDecimal avgCost = es.getAvgCost() == null ? BigDecimal.ZERO : es.getAvgCost();
            BigDecimal costBasis = avgCost.multiply(totalQty).setScale(2, RM);

            List<Trade> recent = tradeRepo
                    .findLatestNonZeroTrades(
                            es.getEquity().getEquityId()
                    );

            BigDecimal lastPrice = BigDecimal.ZERO;
            if (!recent.isEmpty() && recent.get(0).getPrice() != null) {
                lastPrice = recent.get(0).getPrice();
            }

            if (lastPrice.compareTo(BigDecimal.ZERO) <= 0) {
                lastPrice = avgCost;
            }

            if (lastPrice.compareTo(BigDecimal.ZERO) <= 0) {
                lastPrice = avgCost;
            }

            BigDecimal marketValue = lastPrice.multiply(totalQty).setScale(2, RM);
            BigDecimal unreal = marketValue.subtract(costBasis).setScale(2, RM);

            BigDecimal unrealPct;
            if (costBasis.compareTo(BigDecimal.ZERO) == 0) {
                unrealPct = BigDecimal.ZERO;
            } else {
                unrealPct = unreal.multiply(BigDecimal.valueOf(100)).divide(costBasis, 2, RM);
            }

            String symbol;
            if (es.getEquity().getEquityCode() != null) {
                symbol = es.getEquity().getEquityCode();
            } else if (es.getEquity().getEquityName() != null) {
                symbol = es.getEquity().getEquityName();
            } else {
                symbol = "?";
            }

            PortfolioHoldingResponse line = new PortfolioHoldingResponse();
            line.setEquityId(es.getEquity().getEquityId());
            line.setSymbol(symbol);
            line.setFreeQty(freeQty);
            line.setBlockedQty(blockedQty);
            line.setTotalQty(totalQty);
            line.setAvgCost(avgCost.setScale(4, RM));
            line.setCostBasis(costBasis);
            line.setLastClosePrice(lastPrice.setScale(4, RM));
            line.setMarketValue(marketValue);
            line.setUnrealizedPnl(unreal);
            line.setUnrealizedPnlPct(unrealPct);

            rows.add(line);

            holdingsValue = holdingsValue.add(marketValue);
            totalUnreal = totalUnreal.add(unreal);
            totalCost = totalCost.add(costBasis);
        }

        BigDecimal portfolioValue = holdingsValue.add(totalCash);

        BigDecimal totalUnrealPct;
        if (totalCost.compareTo(BigDecimal.ZERO) == 0) {
            totalUnrealPct = BigDecimal.ZERO;
        } else {
            totalUnrealPct = totalUnreal.multiply(BigDecimal.valueOf(100)).divide(totalCost, 2, RM);
        }

        PortfolioReportResponse resp = new PortfolioReportResponse();
        resp.setAccountId(account.getAccountId());
        resp.setIdentityNumber(identityNumber);
        resp.setUserFullName(fullName);
        resp.setAccountType(accountTypeStr);
        resp.setFreeCash(freeCash.setScale(2, RM));
        resp.setBlockedCash(blockedCash.setScale(2, RM));
        resp.setTotalCash(totalCash.setScale(2, RM));
        resp.setHoldings(rows);
        resp.setHoldingsValue(holdingsValue.setScale(2, RM));
        resp.setPortfolioValue(portfolioValue.setScale(2, RM));
        resp.setTotalUnrealizedPnl(totalUnreal.setScale(2, RM));
        resp.setTotalUnrealizedPnlPct(totalUnrealPct);

        return resp;
    }
}
