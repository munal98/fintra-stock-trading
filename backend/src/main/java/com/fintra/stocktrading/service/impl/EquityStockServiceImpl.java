package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityStock;
import com.fintra.stocktrading.repository.AccountRepository;
import com.fintra.stocktrading.repository.EquityRepository;
import com.fintra.stocktrading.repository.EquityStockRepository;
import com.fintra.stocktrading.service.EquityStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class EquityStockServiceImpl implements EquityStockService {

    private final EquityStockRepository equityStockRepository;
    private final AccountRepository accountRepository;
    private final EquityRepository equityRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EquityStock> getAllEquityStocks() {
        return equityStockRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EquityStock> getEquityStockById(Integer stockId) {
        if (stockId == null) {
            throw new IllegalArgumentException("Stock ID cannot be null!");
        }
        return equityStockRepository.findById(stockId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EquityStock> getEquityStockByAccountAndEquity(Account account, Equity equity) {
        if (account == null || equity == null) {
            throw new IllegalArgumentException("Accounts and equity cannot be null!");
        }
        return equityStockRepository.findByAccountAndEquity(account, equity);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EquityStock> getEquityStockByAccountIdAndEquityId(Long accountId, Integer equityId) {
        if (accountId == null || equityId == null) {
            throw new IllegalArgumentException("Account ID and Equity ID cannot be null!");
        }
        return equityStockRepository.findByAccount_AccountIdAndEquity_EquityId(accountId, equityId);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EquityStock> getEquityStockByAccountIdAndEquityId(Integer accountId, Integer equityId) {
        if (accountId == null || equityId == null) {
            throw new IllegalArgumentException("Account ID and Equity ID cannot be null!");
        }
        return getEquityStockByAccountIdAndEquityId(accountId.longValue(), equityId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquityStock> getEquityStocksByAccount(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("The account cannot be null!");
        }
        return equityStockRepository.findByAccount(account);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquityStock> getEquityStocksByEquity(Equity equity) {
        if (equity == null) {
            throw new IllegalArgumentException("Equity cannot be null!");
        }
        return equityStockRepository.findByEquity(equity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquityStock> getEquityHoldingsByAccountId(Integer accountId) {
        if (accountId == null) {
            throw new IllegalArgumentException("Account ID cannot be null!");
        }
        return equityStockRepository.findEquityHoldingsByAccountId(accountId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquityStock> getEquityHoldingsByAccountIds(List<Integer> accountIds) {
        if (accountIds == null || accountIds.isEmpty()) {
            throw new IllegalArgumentException("The account ID list cannot be null or empty!");
        }
        return equityStockRepository.findEquityHoldingsByAccountIds(accountIds);
    }

    @Override
    public EquityStock saveEquityStock(EquityStock equityStock) {
        if (equityStock == null) {
            throw new IllegalArgumentException("EquityStock cannot be null!");
        }
        return equityStockRepository.save(equityStock);
    }

    @Override
    public EquityStock updateEquityStock(EquityStock equityStock) {
        if (equityStock == null || equityStock.getStockId() == null) {
            throw new IllegalArgumentException("EquityStock and Stock ID cannot be null!");
        }

        if (!equityStockRepository.existsById(equityStock.getStockId())) {
            throw new IllegalArgumentException("EquityStock to be updated not found!");
        }

        return equityStockRepository.save(equityStock);
    }

    @Override
    public void deleteEquityStock(Integer stockId) {
        if (stockId == null) {
            throw new IllegalArgumentException("Stock ID cannot be null!");
        }

        if (!equityStockRepository.existsById(stockId)) {
            throw new IllegalArgumentException("No EquityStock to be deleted found!");
        }

        equityStockRepository.deleteById(stockId);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsById(Integer stockId) {
        if (stockId == null) {
            return false;
        }
        return equityStockRepository.existsById(stockId);
    }

    @Override
    @Transactional
    public EquityStock updateCostAfterBuyTrade(Integer accountId, Integer equityId, Integer tradeQuantity,
                                               BigDecimal tradePrice, BigDecimal commission) {

        EquityStock equityStock = findOrCreateEquityStock(accountId, equityId);

        BigDecimal tradeTotalCost = tradePrice.multiply(BigDecimal.valueOf(tradeQuantity));
        if (commission != null && commission.compareTo(BigDecimal.ZERO) > 0) {
            tradeTotalCost = tradeTotalCost.add(commission);
        }
        BigDecimal tradeUnitCost = tradeTotalCost.divide(BigDecimal.valueOf(tradeQuantity), 4, RoundingMode.HALF_UP);

        Integer currentQuantity = (equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0) +
                (equityStock.getBlockedQuantity() != null ? equityStock.getBlockedQuantity() : 0);
        BigDecimal currentTotalCost = (equityStock.getAvgCost() != null && currentQuantity > 0) ?
                equityStock.getAvgCost().multiply(BigDecimal.valueOf(currentQuantity)) : BigDecimal.ZERO;

        BigDecimal newAvgCost = calculateWeightedAverageCost(currentQuantity, currentTotalCost, tradeQuantity, tradeUnitCost);

        equityStock.setFreeQuantity((equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0) + tradeQuantity);
        equityStock.setAvgCost(newAvgCost);

        EquityStock savedStock = equityStockRepository.save(equityStock);

        return savedStock;
    }

    @Override
    @Transactional
    public EquityStock updateCostAfterSellTrade(Integer accountId, Integer equityId, Integer tradeQuantity) {

        EquityStock equityStock = findEquityStock(accountId, equityId);
        if (equityStock == null) {
            throw new IllegalArgumentException("EquityStock not found for account: " + accountId + ", equity: " + equityId);
        }

        Integer currentTotalQuantity = (equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0) +
                (equityStock.getBlockedQuantity() != null ? equityStock.getBlockedQuantity() : 0);

        if (currentTotalQuantity < tradeQuantity) {
            throw new IllegalArgumentException("Insufficient quantity for sell trade");
        }

        equityStock.setFreeQuantity(equityStock.getFreeQuantity() - tradeQuantity);

        Integer remainingQuantity = currentTotalQuantity - tradeQuantity;
        if (remainingQuantity == 0) {
            equityStock.setAvgCost(null);
        }

        EquityStock savedStock = equityStockRepository.save(equityStock);

        return savedStock;
    }

    @Override
    @Transactional
    public EquityStock updateCostAfterIncomingTransfer(Integer accountId, Integer equityId,
                                                       Integer transferQuantity, BigDecimal transferUnitCost) {

        if (transferQuantity == null || transferQuantity <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be greater than 0");
        }

        if (transferUnitCost == null || transferUnitCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer unit cost must be greater than 0");
        }

        EquityStock equityStock = findOrCreateEquityStock(accountId, equityId);

        Integer currentQuantity = (equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0) +
                (equityStock.getBlockedQuantity() != null ? equityStock.getBlockedQuantity() : 0);
        BigDecimal currentTotalCost = (equityStock.getAvgCost() != null && currentQuantity > 0) ?
                equityStock.getAvgCost().multiply(BigDecimal.valueOf(currentQuantity)) : BigDecimal.ZERO;

        BigDecimal newAvgCost = calculateWeightedAverageCost(currentQuantity, currentTotalCost,
                transferQuantity, transferUnitCost);

        equityStock.setFreeQuantity((equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0) + transferQuantity);
        equityStock.setAvgCost(newAvgCost);

        EquityStock savedStock = equityStockRepository.save(equityStock);

        return savedStock;
    }

    @Override
    @Transactional
    public EquityStock updateCostAfterOutgoingTransfer(Integer accountId, Integer equityId, Integer transferQuantity) {

        if (transferQuantity == null || transferQuantity <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be greater than 0");
        }

        EquityStock equityStock = findEquityStock(accountId, equityId);
        if (equityStock == null) {
            throw new IllegalArgumentException("EquityStock not found for account: " + accountId + ", equity: " + equityId);
        }

        Integer currentTotalQuantity = (equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0) +
                (equityStock.getBlockedQuantity() != null ? equityStock.getBlockedQuantity() : 0);

        if (equityStock.getFreeQuantity() == null || equityStock.getFreeQuantity() < transferQuantity) {
            throw new IllegalArgumentException("Insufficient free quantity for outgoing transfer. Available: " +
                    equityStock.getFreeQuantity() + ", Required: " + transferQuantity);
        }

        Integer newFreeQuantity = equityStock.getFreeQuantity() - transferQuantity;
        Integer remainingQuantity = currentTotalQuantity - transferQuantity;

        equityStock.setFreeQuantity(newFreeQuantity);

        if (remainingQuantity == 0) {
            equityStock.setAvgCost(null);
        }


        EquityStock savedStock = equityStockRepository.save(equityStock);

        EquityStock verificationStock = findEquityStock(accountId, equityId);

        return savedStock;
    }

    @Override
    public BigDecimal calculateWeightedAverageCost(Integer currentQuantity, BigDecimal currentTotalCost,
                                                   Integer newQuantity, BigDecimal newUnitCost) {
        if (currentQuantity == null) currentQuantity = 0;
        if (currentTotalCost == null) currentTotalCost = BigDecimal.ZERO;
        if (newQuantity == null || newQuantity <= 0) {
            throw new IllegalArgumentException("New quantity must be greater than 0");
        }
        if (newUnitCost == null || newUnitCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New unit cost must be greater than 0");
        }

        if (currentQuantity == 0) {
            return newUnitCost;
        }

        BigDecimal newTotalCost = newUnitCost.multiply(BigDecimal.valueOf(newQuantity));
        BigDecimal totalCost = currentTotalCost.add(newTotalCost);
        Integer totalQuantity = currentQuantity + newQuantity;

        return totalCost.divide(BigDecimal.valueOf(totalQuantity), 4, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public EquityStock updateQuantityAfterIncomingTransfer(Integer accountId, Integer equityId, Integer transferQuantity) {

        if (transferQuantity == null || transferQuantity <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be greater than 0");
        }

        EquityStock equityStock = findOrCreateEquityStock(accountId, equityId);

        equityStock.setFreeQuantity((equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0) + transferQuantity);

        EquityStock savedStock = equityStockRepository.save(equityStock);

        return savedStock;
    }

    @Override
    @Transactional
    public EquityStock updateQuantityAfterOutgoingTransfer(Integer accountId, Integer equityId, Integer transferQuantity) {

        if (transferQuantity == null || transferQuantity <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be greater than 0");
        }

        EquityStock equityStock = findEquityStock(accountId, equityId);
        if (equityStock == null) {
            throw new IllegalArgumentException("EquityStock not found for account: " + accountId + ", equity: " + equityId);
        }

        if (equityStock.getFreeQuantity() == null || equityStock.getFreeQuantity() < transferQuantity) {
            throw new IllegalArgumentException("Insufficient free quantity for outgoing transfer. Available: " +
                    equityStock.getFreeQuantity() + ", Required: " + transferQuantity);
        }

        equityStock.setFreeQuantity(equityStock.getFreeQuantity() - transferQuantity);

        Integer totalQuantity = (equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0) +
                (equityStock.getBlockedQuantity() != null ? equityStock.getBlockedQuantity() : 0);
        if (totalQuantity == 0) {
            equityStock.setAvgCost(null);
        }

        EquityStock savedStock = equityStockRepository.save(equityStock);

        return savedStock;
    }

    @Override
    @Transactional
    public EquityStock updateStockAfterExternalTransfer(Integer accountId, Integer equityId, Integer transferQuantity) {

        if (transferQuantity == null || transferQuantity <= 0) {
            throw new IllegalArgumentException("Transfer quantity must be greater than 0");
        }

        EquityStock equityStock = findOrCreateEquityStock(accountId, equityId);

        Integer currentQuantity = (equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0) +
                (equityStock.getBlockedQuantity() != null ? equityStock.getBlockedQuantity() : 0);

        BigDecimal transferUnitCost = determineExternalTransferCost(equityId, equityStock.getAvgCost());

        if (currentQuantity > 0 && equityStock.getAvgCost() != null && equityStock.getAvgCost().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal currentTotalCost = equityStock.getAvgCost().multiply(BigDecimal.valueOf(currentQuantity));
            BigDecimal newAvgCost = calculateWeightedAverageCost(currentQuantity, currentTotalCost,
                    transferQuantity, transferUnitCost);
            equityStock.setAvgCost(newAvgCost);
        } else {
            equityStock.setAvgCost(transferUnitCost);
        }

        equityStock.setFreeQuantity((equityStock.getFreeQuantity() != null ? equityStock.getFreeQuantity() : 0) + transferQuantity);

        EquityStock savedStock = equityStockRepository.save(equityStock);

        return savedStock;
    }

    @Transactional
    public void unblock(Long accountId, Integer equityId, int quantity) {
        if (accountId == null || equityId == null) {
            throw new IllegalArgumentException("Account ID and Equity ID cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to unblock must be greater than 0");
        }

        EquityStock stock = equityStockRepository
                .findByAccount_AccountIdAndEquity_EquityId(accountId, equityId)
                .orElseGet(() -> {
                    Account acct = accountRepository.findById(accountId.intValue())
                            .orElseThrow(() -> new IllegalStateException("Account not found: " + accountId));
                    Equity equity = equityRepository.findById(equityId)
                            .orElseThrow(() -> new IllegalStateException("Equity not found: " + equityId));
                    return EquityStock.builder()
                            .account(acct)
                            .equity(equity)
                            .freeQuantity(0)
                            .blockedQuantity(0)
                            .build();
                });

        int newBlocked = stock.getBlockedQuantity() - quantity;
        if (newBlocked < 0) {
            log.warn("Attempt to unblock more quantity than is blocked. Blocked: {}, Unblocking: {}. Setting blocked to 0.",
                    stock.getBlockedQuantity(), quantity);
            newBlocked = 0;
        }

        stock.setBlockedQuantity(newBlocked);
        stock.setFreeQuantity(stock.getFreeQuantity() + quantity);

        equityStockRepository.save(stock);
        log.debug("Unblocked {} equity for account={} and equity={}", quantity, accountId, equityId);
    }

    @Override
    @Transactional
    public void blockStock(Integer accountId, Integer equityId, Integer quantity) {
        if (accountId == null || equityId == null) {
            throw new IllegalArgumentException("Account ID and Equity ID cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to block must be greater than 0");
        }

        EquityStock stock = equityStockRepository
                .findByAccount_AccountIdAndEquity_EquityId(accountId.longValue(), equityId)
                .orElseThrow(() -> new IllegalArgumentException("No equity stock found for account=" + accountId + " and equity=" + equityId));

        log.debug("blockStock: accountId={}, equityId={}, requestedQuantity={}, currentFree={}, currentBlocked={}", 
                 accountId, equityId, quantity, stock.getFreeQuantity(), stock.getBlockedQuantity());

        if (stock.getFreeQuantity() < quantity) {
            log.error("Insufficient free stock to block: accountId={}, equityId={}, requestedQuantity={}, availableFree={}", 
                     accountId, equityId, quantity, stock.getFreeQuantity());
            throw new IllegalArgumentException("Insufficient free stock: requested=" + quantity + 
                                             ", available=" + stock.getFreeQuantity() + 
                                             ", accountId=" + accountId + ", equityId=" + equityId);
        }

        stock.setFreeQuantity(stock.getFreeQuantity() - quantity);
        stock.setBlockedQuantity(stock.getBlockedQuantity() + quantity);
        equityStockRepository.save(stock);
        
        log.debug("blockStock completed: accountId={}, equityId={}, newFree={}, newBlocked={}", 
                 accountId, equityId, stock.getFreeQuantity(), stock.getBlockedQuantity());
    }

    @Override
    @Transactional
    public void unblockStock(Integer accountId, Integer equityId, Integer quantity) {
        if (accountId == null || equityId == null) {
            throw new IllegalArgumentException("Account ID and Equity ID cannot be null");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to unblock must be greater than 0");
        }

        EquityStock stock = equityStockRepository
                .findByAccount_AccountIdAndEquity_EquityId(accountId.longValue(), equityId)
                .orElseThrow(() -> new IllegalArgumentException("No equity stock found for account=" + accountId + " and equity=" + equityId));

        log.debug("unblockStock: accountId={}, equityId={}, requestedQuantity={}, currentBlocked={}, currentFree={}", 
                 accountId, equityId, quantity, stock.getBlockedQuantity(), stock.getFreeQuantity());

        if (stock.getBlockedQuantity() < quantity) {
            log.error("Insufficient blocked stock to unblock: accountId={}, equityId={}, requestedQuantity={}, availableBlocked={}", 
                     accountId, equityId, quantity, stock.getBlockedQuantity());
            throw new IllegalArgumentException("Insufficient blocked stock: requested=" + quantity + 
                                             ", available=" + stock.getBlockedQuantity() + 
                                             ", accountId=" + accountId + ", equityId=" + equityId);
        }

        stock.setBlockedQuantity(stock.getBlockedQuantity() - quantity);
        stock.setFreeQuantity(stock.getFreeQuantity() + quantity);
        equityStockRepository.save(stock);
        
        log.debug("unblockStock completed: accountId={}, equityId={}, newBlocked={}, newFree={}", 
                 accountId, equityId, stock.getBlockedQuantity(), stock.getFreeQuantity());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEnoughStock(Integer accountId, Integer equityId, Integer quantity) {
        if (accountId == null || equityId == null) {
            return false;
        }
        if (quantity <= 0) {
            return false;
        }

        Optional<EquityStock> stockOpt = equityStockRepository
                .findByAccount_AccountIdAndEquity_EquityId(accountId.longValue(), equityId);
        
        if (stockOpt.isEmpty()) {
            return false;
        }

        return stockOpt.get().getFreeQuantity() >= quantity;
    }

    private BigDecimal determineExternalTransferCost(Integer equityId, BigDecimal existingAvgCost) {

        if (existingAvgCost != null && existingAvgCost.compareTo(BigDecimal.ZERO) > 0) {
            return existingAvgCost;
        }

        BigDecimal defaultCost = BigDecimal.valueOf(10.0);
        return defaultCost;
    }

    private EquityStock findEquityStock(Integer accountId, Integer equityId) {
        if (accountId == null || equityId == null) {
            return null;
        }
        return equityStockRepository.findByAccount_AccountIdAndEquity_EquityId(
                accountId.longValue(), equityId).orElse(null);
    }

    private EquityStock findOrCreateEquityStock(Integer accountId, Integer equityId) {
        if (accountId == null || equityId == null) {
            throw new IllegalArgumentException("Account ID and Equity ID cannot be null");
        }

        EquityStock equityStock = findEquityStock(accountId, equityId);

        if (equityStock == null) {

            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalArgumentException("Account not found: " + accountId));
            Equity equity = equityRepository.findById(equityId)
                    .orElseThrow(() -> new IllegalArgumentException("Equity not found: " + equityId));

            equityStock = EquityStock.builder()
                    .account(account)
                    .equity(equity)
                    .freeQuantity(0)
                    .blockedQuantity(0)
                    .avgCost(null)
                    .build();
        }
        return equityStock;
    }
}