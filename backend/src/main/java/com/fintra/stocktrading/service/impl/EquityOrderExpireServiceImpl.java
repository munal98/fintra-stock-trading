package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.entity.EquityStock;
import com.fintra.stocktrading.model.enums.OrderSide;
import com.fintra.stocktrading.model.enums.OrderStatus;
import com.fintra.stocktrading.repository.EquityOrderRepository;
import com.fintra.stocktrading.service.CashBalanceService;
import com.fintra.stocktrading.service.EquityOrderExpireService;
import com.fintra.stocktrading.service.EquityStockService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquityOrderExpireServiceImpl implements EquityOrderExpireService {

    private final EquityOrderRepository orderRepository;
    private final CashBalanceService cashBalanceService;
    private final EquityStockService equityStockService;

    @Override
    @Transactional
    public int expireOldOrders(LocalDate today) {

        List<EquityOrder> pendingOrders = orderRepository.findByFinalStatus(OrderStatus.PENDING);
        List<EquityOrder> partialOrders = orderRepository.findByFinalStatus(OrderStatus.PARTIALLY_FILLED);

        List<EquityOrder> toExpire = pendingOrders.stream()
                .filter(o -> o.getOrderDate().isEqual(today))
                .filter(o -> o.getFinalStatus() != OrderStatus.EXPIRED)
                .collect(Collectors.toList());

        toExpire.addAll(partialOrders.stream()
                .filter(o -> o.getOrderDate().isEqual(today))
                .filter(o -> o.getFinalStatus() != OrderStatus.EXPIRED)
                .collect(Collectors.toList()));

        log.info("Found {} orders to expire for TODAY ({}): {}", toExpire.size(), today, 
                toExpire.stream().map(o -> o.getOrderId()).collect(Collectors.toList()));

        int actuallyExpired = 0;
        for (EquityOrder order : toExpire) {
            try {
                boolean wasExpired = expireOrder(order);
                if (wasExpired) {
                    actuallyExpired++;
                }
            } catch (Exception ex) {
                log.error("Error expiring order {}: {}", order.getOrderId(), ex.getMessage(), ex);
            }
        }

        log.info("Successfully expired {} out of {} candidate orders for TODAY", actuallyExpired, toExpire.size());
        return actuallyExpired;
    }

    private boolean expireOrder(EquityOrder order) {
        try {
            if (order.getFinalStatus() == OrderStatus.EXPIRED) {
                log.debug("Order {} already expired, skipping", order.getOrderId());
                return false;
            }

            if (order.getFinalStatus() != OrderStatus.PENDING && 
                order.getFinalStatus() != OrderStatus.PARTIALLY_FILLED) {
                log.debug("Order {} cannot be expired, current status: {}", 
                         order.getOrderId(), order.getFinalStatus());
                return false;
            }

            if (order.getAccount() == null || order.getEquity() == null) {
                log.error("Order {} has null account or equity, cannot expire", order.getOrderId());
                throw new IllegalStateException("Order " + order.getOrderId() + " has null account or equity");
            }

            Integer accountId = order.getAccount().getAccountId();
            Integer equityId = order.getEquity().getEquityId();
            Integer remainingQuantity = order.getRemainingQuantity();
            BigDecimal price = order.getPrice();

            if (remainingQuantity == null || remainingQuantity <= 0) {
                log.error("Order {} has invalid remaining quantity: {}", order.getOrderId(), remainingQuantity);
                throw new IllegalStateException("Order " + order.getOrderId() + " has invalid remaining quantity: " + remainingQuantity);
            }

            if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
                log.error("Order {} has invalid price: {}", order.getOrderId(), price);
                throw new IllegalStateException("Order " + order.getOrderId() + " has invalid price: " + price);
            }

            log.debug("Expiring order {}: side={}, remainingQuantity={}, price={}", 
                     order.getOrderId(), order.getOrderSide(), remainingQuantity, price);

            if (order.getOrderSide() == OrderSide.BUY) {
                BigDecimal blockedAmount = price.multiply(BigDecimal.valueOf(remainingQuantity));
                
                log.debug("Restoring {} blocked balance to free balance for account {}", 
                         blockedAmount, accountId);
                
                try {
                    cashBalanceService.moveBlockedToFree(accountId, blockedAmount);
                } catch (Exception ex) {
                    log.error("Failed to restore blocked balance for BUY order {}: {}", order.getOrderId(), ex.getMessage());
                    throw new RuntimeException("Failed to restore blocked balance for order " + order.getOrderId(), ex);
                }
                
            } else {
                try {
                    EquityStock stock = equityStockService
                            .getEquityStockByAccountIdAndEquityId(accountId.longValue(), equityId)
                            .orElseThrow(() -> new IllegalStateException(
                                    "Seller stock not found for expired order: account=" + accountId + ", equity=" + equityId));

                    if (stock.getBlockedQuantity() < remainingQuantity) {
                        log.error("Insufficient blocked quantity for order {}: blocked={}, required={}", 
                                 order.getOrderId(), stock.getBlockedQuantity(), remainingQuantity);
                        throw new IllegalStateException("Insufficient blocked quantity for order " + order.getOrderId());
                    }
                    
                    stock.setBlockedQuantity(stock.getBlockedQuantity() - remainingQuantity);
                    stock.setFreeQuantity(stock.getFreeQuantity() + remainingQuantity);
                    equityStockService.updateEquityStock(stock);
                    
                    log.debug("Restored {} blocked quantity to free quantity for account {} equity {}", 
                             remainingQuantity, accountId, equityId);
                             
                } catch (Exception ex) {
                    log.error("Failed to restore blocked quantity for SELL order {}: {}", order.getOrderId(), ex.getMessage());
                    throw new RuntimeException("Failed to restore blocked quantity for order " + order.getOrderId(), ex);
                }
            }

            order.setFinalStatus(OrderStatus.EXPIRED);
            order.setCombinedStatus(OrderStatus.EXPIRED);
            
            try {
                orderRepository.save(order);
            } catch (Exception ex) {
                log.error("Failed to save expired order {}: {}", order.getOrderId(), ex.getMessage());
                throw new RuntimeException("Failed to save expired order " + order.getOrderId(), ex);
            }

            log.debug("Order {} marked as EXPIRED", order.getOrderId());
            return true;
            
        } catch (Exception ex) {
            log.error("Critical error expiring order {}: {}", order.getOrderId(), ex.getMessage(), ex);
            throw ex;
        }
    }
}
