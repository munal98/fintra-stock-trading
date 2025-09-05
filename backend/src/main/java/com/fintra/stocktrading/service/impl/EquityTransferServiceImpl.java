package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.model.dto.request.ExternalTransferToPortfolioRequest;
import com.fintra.stocktrading.model.dto.request.PortfolioTransferRequest;
import com.fintra.stocktrading.model.dto.request.PortfolioExternalTransferRequest;
import com.fintra.stocktrading.model.entity.*;
import com.fintra.stocktrading.model.enums.TransferType;
import com.fintra.stocktrading.repository.*;
import com.fintra.stocktrading.service.EquityStockService;
import com.fintra.stocktrading.service.EquityTransferService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
@Slf4j
public class EquityTransferServiceImpl implements EquityTransferService {

    private final EquityTransferRepository equityTransferRepository;
    private final EquityStockRepository equityStockRepository;
    private final AccountRepository accountRepository;
    private final EquityRepository equityRepository;
    private final OtherInstitutionRepository otherInstitutionRepository;
    private final @Lazy EquityStockService equityStockService;

    @Override
    @Transactional
    public EquityTransfer performTransferToPortfolio(PortfolioTransferRequest request) {
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new NotFoundException("The sender account does not exist."));
        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new NotFoundException("The recipient account does not exist."));
        Equity equity = equityRepository.findById(request.getEquityId())
                .orElseThrow(() -> new NotFoundException("The equity does not exist."));

        if (request.getTransferQuantity() == null || request.getTransferQuantity() < 1) {
            throw new IllegalArgumentException("The transfer amount must be at least 1.");
        }

        EquityStock fromStock = equityStockRepository.findByAccountAndEquity(fromAccount, equity)
                .orElseThrow(() -> new IllegalArgumentException("The sender account does not have this equity."));

        if (fromStock.getFreeQuantity() == null || fromStock.getFreeQuantity() < request.getTransferQuantity()) {
            throw new IllegalArgumentException("The sender account does not have enough equity. Available: " +
                    fromStock.getFreeQuantity() + ", Required: " + request.getTransferQuantity());
        }

        BigDecimal transferUnitCost = fromStock.getAvgCost();

        equityStockService.updateCostAfterOutgoingTransfer(
                request.getFromAccountId(),
                request.getEquityId(),
                request.getTransferQuantity()
        );

        if (transferUnitCost != null && transferUnitCost.compareTo(BigDecimal.ZERO) > 0) {
            equityStockService.updateCostAfterIncomingTransfer(
                    request.getToAccountId(),
                    request.getEquityId(),
                    request.getTransferQuantity(),
                    transferUnitCost
            );
        } else {
            try {
                equityStockService.updateCostAfterIncomingTransfer(
                        request.getToAccountId(),
                        request.getEquityId(),
                        request.getTransferQuantity(),
                        BigDecimal.ONE
                );
            } catch (Exception e) {

                EquityStock toStock = equityStockRepository.findByAccountAndEquity(toAccount, equity)
                        .orElse(EquityStock.builder()
                                .account(toAccount)
                                .equity(equity)
                                .freeQuantity(0)
                                .blockedQuantity(0)
                                .avgCost(null)
                                .build());

                var from_avg_cost = fromStock.getAvgCost();
                var to_avg_cost = toStock.getAvgCost();

                var free_qty = toStock.getFreeQuantity();
                var total_stock_cost = to_avg_cost.multiply(BigDecimal.valueOf(free_qty));

                var transger_qty = request.getTransferQuantity();
                var transfer_total_cost = from_avg_cost.multiply(BigDecimal.valueOf(transger_qty));

                var total_cost = total_stock_cost.subtract(transfer_total_cost);
                var total_quantity = free_qty + transger_qty;
                var new_avg_cost = total_cost.divide(BigDecimal.valueOf(total_quantity), 4, RoundingMode.HALF_UP);

                toStock.setAvgCost(new_avg_cost);
                toStock.setFreeQuantity(total_quantity);

                toStock.setFreeQuantity((toStock.getFreeQuantity() != null ? toStock.getFreeQuantity() : 0) + request.getTransferQuantity());

                equityStockRepository.save(toStock);
            }
        }

        log.info("Portfolio transfer completed - From Account: {}, To Account: {}, Equity: {}, Quantity: {}, Unit Cost: {}",
                request.getFromAccountId(), request.getToAccountId(), request.getEquityId(),
                request.getTransferQuantity(), transferUnitCost);

        EquityTransfer transfer = new EquityTransfer();
        transfer.setFromAccount(fromAccount);
        transfer.setToAccount(toAccount);
        transfer.setEquity(equity);
        transfer.setTransferQuantity(request.getTransferQuantity());
        transfer.setTransferType(TransferType.PORTFOLIO_TO_PORTFOLIO);
        transfer.setTransactionTime(LocalDateTime.now());

        return equityTransferRepository.save(transfer);
    }

    @Override
    @Transactional
    public EquityTransfer performTransferToExternal(PortfolioExternalTransferRequest request) {
        Account fromAccount = accountRepository.findById(request.getFromAccountId())
                .orElseThrow(() -> new IllegalArgumentException("The sender account does not exist."));
        Equity equity = equityRepository.findById(request.getEquityId())
                .orElseThrow(() -> new IllegalArgumentException("The equity does not exist."));
        OtherInstitution otherInstitution = otherInstitutionRepository.findById(request.getOtherInstitutionId())
                .orElseThrow(() -> new IllegalArgumentException("The other institution does not exist."));

        if (request.getTransferQuantity() == null || request.getTransferQuantity() < 1) {
            throw new IllegalArgumentException("The transfer amount must be at least 1.");
        }

        EquityStock fromStock = equityStockRepository.findByAccountAndEquity(fromAccount, equity)
                .orElseThrow(() -> new IllegalArgumentException("The sender does not own this stock."));

        if (fromStock.getFreeQuantity() == null || fromStock.getFreeQuantity() < request.getTransferQuantity()) {
            throw new IllegalArgumentException("There is not enough free quantity for the transfer. Available: " +
                    fromStock.getFreeQuantity() + ", Required: " + request.getTransferQuantity());
        }

        int transferQuantity = request.getTransferQuantity();

        log.info("Starting portfolio-to-external transfer - From Account: {}, Equity: {}, Quantity: {}, Current Free Quantity: {}, Current Avg Cost: {}",
                request.getFromAccountId(), request.getEquityId(), transferQuantity,
                fromStock.getFreeQuantity(), fromStock.getAvgCost());

        EquityStock updatedStock = equityStockService.updateCostAfterOutgoingTransfer(
                request.getFromAccountId(),
                request.getEquityId(),
                transferQuantity
        );

        log.info("Portfolio-to-external transfer completed - From Account: {}, Equity: {}, Quantity: {}, New Free Quantity: {}, New Avg Cost: {}",
                request.getFromAccountId(), request.getEquityId(), transferQuantity,
                updatedStock.getFreeQuantity(), updatedStock.getAvgCost());

        EquityTransfer transfer = new EquityTransfer();
        transfer.setFromAccount(fromAccount);
        transfer.setEquity(equity);
        transfer.setOtherInstitution(otherInstitution);
        transfer.setTransferQuantity(transferQuantity);
        transfer.setTransferType(TransferType.PORTFOLIO_TO_EXTERNAL);
        transfer.setTransactionTime(LocalDateTime.now());
        transfer.setTcknOrVergiNo(request.getTckn_vergi_no());

        return equityTransferRepository.save(transfer);
    }

    @Override
    @Transactional
    public EquityTransfer performTransferFromExternalToPortfolio(ExternalTransferToPortfolioRequest request) {
        Account toAccount = accountRepository.findById(request.getToAccountId())
                .orElseThrow(() -> new IllegalArgumentException("The recipient account does not exist."));
        Equity equity = equityRepository.findById(request.getEquityId())
                .orElseThrow(() -> new IllegalArgumentException("The equity does not exist."));
        OtherInstitution otherInstitution = otherInstitutionRepository.findById(request.getOtherInstitutionId())
                .orElseThrow(() -> new IllegalArgumentException("The other institution does not exist."));

        if (request.getTransferQuantity() == null || request.getTransferQuantity() < 1) {
            throw new IllegalArgumentException("The transfer amount must be at least 1.");
        }

        var transferQuantity = request.getTransferQuantity();
        var transferAvgCost = request.getAvgCost();
        var totalTransferCost = transferAvgCost.multiply(BigDecimal.valueOf(transferQuantity));

        var toStock = equityStockRepository.findByAccountAndEquity(toAccount, equity);

        EquityStock updatedEquityStock = toStock.map(equityStock -> {
            Integer currentQuantity = equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0;
            BigDecimal currentAvgCost = equityStock.getAvgCost() != null ? equityStock.getAvgCost() : BigDecimal.ZERO;

            BigDecimal existingQuantityBD = BigDecimal.valueOf(currentQuantity);
            BigDecimal transferQuantityBD = BigDecimal.valueOf(transferQuantity);

            BigDecimal newTotalQuantity = existingQuantityBD.add(transferQuantityBD);
            BigDecimal weightedAvgCost = currentAvgCost
                    .multiply(existingQuantityBD)
                    .add(totalTransferCost)
                    .divide(newTotalQuantity, 4, BigDecimal.ROUND_HALF_UP);

            equityStock.setFreeQuantity(currentQuantity + transferQuantity);
            equityStock.setAvgCost(weightedAvgCost);
            return equityStock;
        }).orElseGet(() -> {

            EquityStock newStock = new EquityStock();
            newStock.setAccount(toAccount);
            newStock.setEquity(equity);
            newStock.setFreeQuantity(transferQuantity);
            newStock.setBlockedQuantity(0);
            newStock.setAvgCost(transferAvgCost);
            return newStock;
        });

        equityStockRepository.save(updatedEquityStock);

        EquityTransfer transfer = new EquityTransfer();
        transfer.setToAccount(toAccount);
        transfer.setEquity(equity);
        transfer.setOtherInstitution(otherInstitution);
        transfer.setTransferQuantity(transferQuantity);
        transfer.setTransferType(TransferType.EXTERNAL_TO_PORTFOLIO);
        transfer.setTransactionTime(LocalDateTime.now());
        transfer.setTcknOrVergiNo(request.getTckn_vergi_no());

        return equityTransferRepository.save(transfer);
    }
}