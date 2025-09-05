package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.entity.*;
import com.fintra.stocktrading.model.enums.TradeStatus;
import com.fintra.stocktrading.repository.EquityOrderMatchRepository;
import com.fintra.stocktrading.repository.TradeRepository;
import com.fintra.stocktrading.service.BusinessDayService;
import com.fintra.stocktrading.service.CashBalanceService;
import com.fintra.stocktrading.service.EquityStockService;
import com.fintra.stocktrading.service.EquityDistributionService;
import com.fintra.stocktrading.service.TradeSettlementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSettlementServiceImpl implements TradeSettlementService {

    private final EquityOrderMatchRepository matchRepo;
    private final TradeRepository tradeRepo;
    private final CashBalanceService cashBalanceService;
    private final EquityStockService equityStockService;
    private final EquityDistributionService equityDistributionService;
    private final BusinessDayService businessDayService;

    @Override
    @Transactional
    public int settleTradesOnDate(LocalDate tradeDate) {
        LocalDateTime startOfDay = tradeDate.atStartOfDay();
        LocalDateTime endOfDay   = tradeDate.atTime(LocalTime.MAX);

        List<EquityOrderMatch> matches = matchRepo.findByMatchTimeBetween(startOfDay, endOfDay);
        if (matches.isEmpty()) {
            log.info("No matches to settle for date: {}", tradeDate);
            return 0;
        }

        log.info("Starting T+2 settlement for {} matches on date: {}", matches.size(), tradeDate);

        int actuallySettled = 0;
        for (EquityOrderMatch match : matches) {
            try {
                boolean wasSettled = settleMatch(match);
                if (wasSettled) {
                    actuallySettled++;
                }
            } catch (Exception ex) {
                log.error("Error settling match {}: {}", match.getMatchRecordId(), ex.getMessage(), ex);
            }
        }

        log.info("T+2 settlement completed: {} out of {} matches actually settled on {}", 
                actuallySettled, matches.size(), tradeDate);
        return actuallySettled;
    }

    private boolean settleMatch(EquityOrderMatch match) {
        Integer matchId = match.getMatchId();
        
        try {
            EquityOrder buyOrder  = match.getBuyOrder();
            EquityOrder sellOrder = match.getSellOrder();

            if (buyOrder == null || sellOrder == null) {
                log.error("Match {} has null buy or sell order", matchId);
                throw new IllegalStateException("Match " + matchId + " has null buy or sell order");
            }

            if (buyOrder.getAccount() == null || sellOrder.getAccount() == null) {
                log.error("Match {} has orders with null accounts", matchId);
                throw new IllegalStateException("Match " + matchId + " has orders with null accounts");
            }

            if (buyOrder.getEquity() == null || sellOrder.getEquity() == null) {
                log.error("Match {} has orders with null equities", matchId);
                throw new IllegalStateException("Match " + matchId + " has orders with null equities");
            }

            Integer buyerId  = buyOrder.getAccount().getAccountId();
            Integer sellerId = sellOrder.getAccount().getAccountId();
            Integer equityId = buyOrder.getEquity().getEquityId();

            Trade buyTrade = tradeRepo.findByMatchIdAndEquityOrder(matchId, buyOrder)
                    .orElseThrow(() -> new IllegalStateException("Buy trade not found for match " + matchId));

            Trade sellTrade = tradeRepo.findByMatchIdAndEquityOrder(matchId, sellOrder)
                    .orElseThrow(() -> new IllegalStateException("Sell trade not found for match " + matchId));

            if (buyTrade.getStatus() == TradeStatus.SETTLED && sellTrade.getStatus() == TradeStatus.SETTLED) {
                log.debug("Match {} already settled, skipping", matchId);
                return false;
            }

            if (buyTrade.getStatus() == TradeStatus.SETTLED || sellTrade.getStatus() == TradeStatus.SETTLED) {
                log.warn("Partial settlement detected for match {}: buyTrade={}, sellTrade={}", 
                        matchId, buyTrade.getStatus(), sellTrade.getStatus());
                return false;
            }

            int quantity = buyTrade.getTradeQuantity();
            BigDecimal price = buyTrade.getPrice();
            
            if (quantity <= 0) {
                log.error("Match {} has invalid trade quantity: {}", matchId, quantity);
                throw new IllegalStateException("Match " + matchId + " has invalid trade quantity: " + quantity);
            }
            
            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Match {} has invalid price: {}", matchId, price);
                throw new IllegalStateException("Match " + matchId + " has invalid price: " + price);
            }
            
            if (quantity != sellTrade.getTradeQuantity() || !price.equals(sellTrade.getPrice())) {
                log.error("Match {} has inconsistent trade data: buy(qty={}, price={}) vs sell(qty={}, price={})", 
                         matchId, quantity, price, sellTrade.getTradeQuantity(), sellTrade.getPrice());
                throw new IllegalStateException("Match " + matchId + " has inconsistent trade data");
            }

            BigDecimal buyCommission = buyTrade.getCommission() != null ? buyTrade.getCommission() : BigDecimal.ZERO;
            BigDecimal sellCommission = sellTrade.getCommission() != null ? sellTrade.getCommission() : BigDecimal.ZERO;
            BigDecimal totalAmount = price.multiply(BigDecimal.valueOf(quantity));

            log.debug("Settling match {}: quantity={}, price={}, buyCommission={}, sellCommission={}", 
                    matchId, quantity, price, buyCommission, sellCommission);

            try {
                cashBalanceService.transfer(buyerId, sellerId, totalAmount);
            } catch (Exception ex) {
                log.error("Failed to transfer cash for match {}: {}", matchId, ex.getMessage());
                throw new RuntimeException("Cash transfer failed for match " + matchId, ex);
            }

            try {
                settleBuyerSide(buyerId, equityId, quantity, price, buyCommission, totalAmount);
            } catch (Exception ex) {
                log.error("Failed to settle buyer side for match {}: {}", matchId, ex.getMessage());
                throw new RuntimeException("Buyer settlement failed for match " + matchId, ex);
            }

            try {
                settleSellerSide(sellerId, equityId, quantity, totalAmount.subtract(sellCommission));
            } catch (Exception ex) {
                log.error("Failed to settle seller side for match {}: {}", matchId, ex.getMessage());
                throw new RuntimeException("Seller settlement failed for match " + matchId, ex);
            }

            try {
                buyTrade.setStatus(TradeStatus.SETTLED);
                sellTrade.setStatus(TradeStatus.SETTLED);
                tradeRepo.saveAll(List.of(buyTrade, sellTrade));
            } catch (Exception ex) {
                log.error("Failed to save settled trades for match {}: {}", matchId, ex.getMessage());
                throw new RuntimeException("Failed to save settled trades for match " + matchId, ex);
            }

            try {
                createEquityDistributions(buyOrder, sellOrder, quantity, price);
            } catch (Exception ex) {
                log.error("Failed to create equity distributions for match {}: {}", matchId, ex.getMessage());
                throw new RuntimeException("Failed to create equity distributions for match " + matchId, ex);
            }

            log.debug("Successfully settled match {}", matchId);
            return true;
            
        } catch (Exception ex) {
            log.error("Critical error settling match {}: {}", matchId, ex.getMessage(), ex);
            throw ex;
        }
    }

    private void settleBuyerSide(Integer buyerId, Integer equityId, int quantity,
                                 BigDecimal price, BigDecimal commission, BigDecimal totalAmount) {

        cashBalanceService.reduceBlockedBalance(buyerId, totalAmount.add(commission));

        EquityStock buyerStock = equityStockService
                .getEquityStockByAccountIdAndEquityId(buyerId.longValue(), equityId)
                .orElse(null);

        if (buyerStock == null) {
            BigDecimal avgCostPerShare = price.add(commission.divide(BigDecimal.valueOf(quantity), 4, RoundingMode.HALF_UP));
            buyerStock = EquityStock.builder()
                    .account(Account.builder().accountId(buyerId).build())
                    .equity(Equity.builder().equityId(equityId).build())
                    .freeQuantity(quantity)
                    .blockedQuantity(0)
                    .avgCost(avgCostPerShare)
                    .build();
        } else {
            updateAvgCostAndQuantity(buyerStock, quantity, price, commission);
        }

        equityStockService.saveEquityStock(buyerStock);
        log.debug("Updated buyer {} equity position: quantity={}, avgCost={}",
                buyerId, buyerStock.getFreeQuantity(), buyerStock.getAvgCost());
    }

    private void settleSellerSide(Integer sellerId, Integer equityId, int quantity, BigDecimal netAmount) {

        EquityStock sellerStock = equityStockService
                .getEquityStockByAccountIdAndEquityId(sellerId.longValue(), equityId)
                .orElseThrow(() -> new IllegalStateException(
                        "Seller stock not found: account=" + sellerId + ", equity=" + equityId));

        sellerStock.setBlockedQuantity(sellerStock.getBlockedQuantity() - quantity);
        equityStockService.updateEquityStock(sellerStock);

        cashBalanceService.addFreeBalance(sellerId, netAmount);

        log.debug("Updated seller {} equity position: blocked quantity reduced by {}, cash increased by {}",
                sellerId, quantity, netAmount);
    }

    private void updateAvgCostAndQuantity(EquityStock stock, int newQuantity,
                                          BigDecimal newPrice, BigDecimal commission) {

        int currentQuantity = stock.getFreeQuantity() + stock.getBlockedQuantity();
        BigDecimal currentAvgCost = stock.getAvgCost();


        BigDecimal currentTotalValue = currentAvgCost.multiply(BigDecimal.valueOf(currentQuantity));
        BigDecimal newPurchaseValue = newPrice.multiply(BigDecimal.valueOf(newQuantity)).add(commission);
        BigDecimal newTotalQuantity = BigDecimal.valueOf(currentQuantity + newQuantity);

        BigDecimal newAvgCost = currentTotalValue.add(newPurchaseValue)
                .divide(newTotalQuantity, 4, RoundingMode.HALF_UP);

        stock.setAvgCost(newAvgCost);
        stock.setFreeQuantity(stock.getFreeQuantity() + newQuantity);

        log.debug("AvgCost calculation: current={}@{}, new={}@{}, result={}@{}",
                currentQuantity, currentAvgCost, newQuantity, newPrice,
                currentQuantity + newQuantity, newAvgCost);
    }

    private void createEquityDistributions(EquityOrder buyOrder, EquityOrder sellOrder,
                                           int quantity, BigDecimal price) {
        try {
            equityDistributionService.createDistribution(buyOrder, quantity, price, "BUY");

            equityDistributionService.createDistribution(sellOrder, quantity, price, "SELL");

            log.debug("Created EquityDistribution records for buy order {} and sell order {}",
                    buyOrder.getOrderId(), sellOrder.getOrderId());
        } catch (Exception ex) {
            log.error("Error creating EquityDistribution records: {}", ex.getMessage(), ex);
        }
    }
}
