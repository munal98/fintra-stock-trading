package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.event.TradeMatchedEvent;
import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.entity.EquityOrderMatch;
import com.fintra.stocktrading.model.entity.Trade;
import com.fintra.stocktrading.model.enums.OrderSide;
import com.fintra.stocktrading.model.enums.OrderStatus;
import com.fintra.stocktrading.model.enums.OrderType;
import com.fintra.stocktrading.model.enums.TradeStatus;
import com.fintra.stocktrading.repository.EquityOrderMatchRepository;
import com.fintra.stocktrading.repository.EquityOrderRepository;
import com.fintra.stocktrading.repository.TradeRepository;
import com.fintra.stocktrading.service.*;
import com.fintra.stocktrading.service.event.TradeEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderMatchingServiceImpl implements OrderMatchingService {

    private final EquityOrderRepository orderRepository;
    private final EquityOrderLogService equityOrderLogService;
    private final TradeRepository tradeRepository;
    private final TradeEventPublisher tradeEventPublisher;
    private final EquityStockService equityStockService;
    private final EquityOrderHistoryService orderHistoryService;
    private final EquityOrderMatchRepository orderMatchRepository;

    private static final ZoneId TR = ZoneId.of("Europe/Istanbul");

    @Override
    @Transactional
    public void matchOrder(EquityOrder newOrder) {
        if (newOrder == null || newOrder.getOrderId() == null) {
            throw new IllegalArgumentException("Order or orderId is null");
        }
        doMatchById(newOrder.getOrderId());
    }

    @Override
    @Transactional
    public void matchOrder(Integer newOrderId) {
        if (newOrderId == null) {
            throw new IllegalArgumentException("orderId is null");
        }
        doMatchById(newOrderId);
    }

    private void doMatchById(Integer newOrderId) {
        log.info("[MATCH] Started matchOrder for newOrderId={}", newOrderId);

        EquityOrder newOrder = loadForUpdate(newOrderId);

        if (isExpiredDayOrder(newOrder)) {
            log.warn("[MATCH] Order expired (day order)");
            updateStatus(newOrder, OrderStatus.EXPIRED);
            equityOrderLogService.logStatusChange(newOrder, OrderStatus.EXPIRED, null, "Day order expired");
            orderRepository.save(newOrder);
            return;
        }

        validateBandAndTick(newOrder);

        List<EquityOrder> oppositeOrders = findOppositeOrders(newOrder);

        int remainingQty = computeRemainingQty(newOrder);
        Integer matchId = (int) (System.currentTimeMillis() % Integer.MAX_VALUE);

        int filledTotal = 0;
        BigDecimal lastExecPrice = null;

        for (EquityOrder opp : oppositeOrders) {
            if (remainingQty <= 0) break;

            int availableOpp = computeRemainingQty(opp);
            if (availableOpp > 0) {
                int matchQty = Math.min(remainingQty, availableOpp);

                BigDecimal price = determineTradePrice(newOrder, opp);

                Trade[] trades = createTrades(matchId, newOrder, opp, matchQty, price);
                Trade tNew = trades[0];
                Trade tOpp = trades[1];

                recordMatch(matchId, newOrder, opp);

                Trade buyTrade  = (newOrder.getOrderSide() == OrderSide.BUY) ? tNew : tOpp;
                Trade sellTrade = (newOrder.getOrderSide() == OrderSide.BUY) ? tOpp : tNew;

                TradeMatchedEvent event = buildEvent(newOrder, opp, matchQty, price);
                event.setMatchId(matchId);
                event.setBuyTradeId(buyTrade.getTradeId());
                event.setSellTradeId(sellTrade.getTradeId());

                publishAfterCommit(event);

                int newOppRemaining = availableOpp - matchQty;
                updateOppAfterMatch(opp, newOppRemaining);
                remainingQty -= matchQty;

                filledTotal += matchQty;
                lastExecPrice = price;
            }
        }

        finalizeNewOrderAfterLoop(newOrder, remainingQty, filledTotal, lastExecPrice);
    }

    @Override
    @Transactional
    public void matchAllOpenOrders() {
        var openStatuses = List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);
        for (var o : orderRepository.findByFinalStatusIn(openStatuses)) {
            var rem = o.getRemainingQuantity();
            if (rem != null && rem > 0) {
                doMatchById(o.getOrderId());
            }
        }
    }

    private EquityOrder loadForUpdate(Integer id) {
        return orderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Order not found: " + id));
    }

    private boolean isExpiredDayOrder(EquityOrder o) {
        if (o.getOrderType() != OrderType.DAY || o.getEntryDate() == null) return false;
        LocalDate orderDay = o.getEntryDate().toLocalDate();
        return !LocalDate.now(TR).equals(orderDay);
    }

    private List<EquityOrder> findOppositeOrders(EquityOrder taker) {
        var open = List.of(OrderStatus.PENDING, OrderStatus.PARTIALLY_FILLED);

        var list = orderRepository.findByFinalStatusIn(open).stream()
                .filter(o -> o.getEquity().getEquityId().equals(taker.getEquity().getEquityId()))
                .filter(o -> o.getOrderSide() != taker.getOrderSide())
                .filter(o -> computeRemainingQty(o) > 0)
                .filter(o -> priceOk(taker, o))
                .collect(Collectors.toList());

        if (taker.getOrderSide() == OrderSide.BUY) {
            list.sort(Comparator.comparing(EquityOrder::getPrice)
                    .thenComparing(EquityOrder::getEntryDate));
        } else {
            list.sort(Comparator.comparing(EquityOrder::getPrice).reversed()
                    .thenComparing(EquityOrder::getEntryDate));
        }
        return list;
    }

    private int computeRemainingQty(EquityOrder o) {
        Integer rem = o.getRemainingQuantity();
        if (rem != null) return rem;
        Integer ord = o.getOrderQuantity();
        return ord != null ? ord : 0;
    }

    private boolean priceOk(EquityOrder taker, EquityOrder maker) {
        if (taker.getOrderType() == OrderType.MARKET) return true;
        if (taker.getPrice() == null || maker.getPrice() == null) return false;
        return (taker.getOrderSide() == OrderSide.BUY)
                ? taker.getPrice().compareTo(maker.getPrice()) >= 0
                : taker.getPrice().compareTo(maker.getPrice()) <= 0;
    }

    private BigDecimal determineTradePrice(EquityOrder taker, EquityOrder maker) {
        if (taker.getOrderType() == OrderType.MARKET) {
            return maker.getPrice();
        }
        if (taker.getOrderSide() == OrderSide.BUY) {
            return taker.getPrice().min(maker.getPrice());
        } else {
            return taker.getPrice().max(maker.getPrice());
        }
    }

    private Trade[] createTrades(Integer matchId, EquityOrder a, EquityOrder b, int qty, BigDecimal price) {
        Trade ta = tradeRepository.save(Trade.builder()
                .matchId(matchId)
                .equityOrder(a)
                .tradeQuantity(qty)
                .price(price)
                .commission(BigDecimal.ZERO)
                .transactionTime(LocalDateTime.now())
                .status(TradeStatus.MATCHED)
                .build());

        Trade tb = tradeRepository.save(Trade.builder()
                .matchId(matchId)
                .equityOrder(b)
                .tradeQuantity(qty)
                .price(price)
                .commission(BigDecimal.ZERO)
                .transactionTime(LocalDateTime.now())
                .status(TradeStatus.MATCHED)
                .build());

        tradeRepository.flush();
        return new Trade[]{ta, tb};
    }

    @SuppressWarnings("unused")
    private void safelyUpdateCostsAfterTrade(EquityOrder newOrder, EquityOrder opp, int qty, BigDecimal price) {
        try {
            if (newOrder.getOrderSide() == OrderSide.BUY) {
                equityStockService.updateCostAfterBuyTrade(
                        newOrder.getAccount().getAccountId(),
                        newOrder.getEquity().getEquityId(),
                        qty, price, BigDecimal.ZERO
                );
                equityStockService.updateCostAfterSellTrade(
                        opp.getAccount().getAccountId(),
                        opp.getEquity().getEquityId(),
                        qty
                );
            } else {
                equityStockService.updateCostAfterSellTrade(
                        newOrder.getAccount().getAccountId(),
                        newOrder.getEquity().getEquityId(),
                        qty
                );
                equityStockService.updateCostAfterBuyTrade(
                        opp.getAccount().getAccountId(),
                        opp.getEquity().getEquityId(),
                        qty, price, BigDecimal.ZERO
                );
            }
            log.info("Updated equity stock costs after trade - qty={}, price={}", qty, price);
        } catch (Exception e) {
            log.error("Error updating equity stock costs after trade: {}", e.getMessage(), e);
        }
    }

    private TradeMatchedEvent buildEvent(EquityOrder newOrder, EquityOrder opp, int qty, BigDecimal price) {
        var event = new TradeMatchedEvent();
        if (newOrder.getOrderSide() == OrderSide.BUY) {
            event.setBuyOrderId(newOrder.getOrderId());
            event.setSellOrderId(opp.getOrderId());
        } else {
            event.setBuyOrderId(opp.getOrderId());
            event.setSellOrderId(newOrder.getOrderId());
        }
        event.setQuantity(qty);
        event.setPrice(price);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    private void validateBandAndTick(EquityOrder order) {
        if (order == null || order.getEquity() == null || order.getPrice() == null) return;
        validateBand(order);
        validateTick(order);
    }

    private void validateBand(EquityOrder order) {
        try {
            var m = order.getEquity().getClass().getMethod("getReferencePrice");
            var ref = (BigDecimal) m.invoke(order.getEquity());
            if (ref != null) {
                var upper = ref.multiply(BigDecimal.valueOf(1.10));
                var lower = ref.multiply(BigDecimal.valueOf(0.90));
                if (order.getPrice().compareTo(lower) < 0 || order.getPrice().compareTo(upper) > 0) {
                    throw new IllegalArgumentException("Price out of ±10% band");
                }
            }
        } catch (NoSuchMethodException e) {
            log.debug("Equity has no referencePrice; band check skipped");
        } catch (ReflectiveOperationException e) {
            log.warn("Cannot read referencePrice via reflection", e);
        }
    }

    private void validateTick(EquityOrder order) {
        try {
            var m = order.getEquity().getClass().getMethod("getTickStep");
            var tick = (BigDecimal) m.invoke(order.getEquity());
            if (tick != null && tick.signum() > 0
                    && order.getPrice().remainder(tick).compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalArgumentException("Price not aligned to tick size");
            }
        } catch (NoSuchMethodException e) {
            log.debug("Equity has no tickStep; tick check skipped");
        } catch (ReflectiveOperationException e) {
            log.warn("Cannot read tickStep via reflection", e);
        }
    }

    private void updateStatus(EquityOrder o, OrderStatus status) {
        o.setFinalStatus(status);
        o.setCombinedStatus(status);
        o.setUpdatedAt(LocalDateTime.now());
    }

    private void publishAfterCommit(TradeMatchedEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    log.info("[MATCH] Transaction committed, sending Kafka event");
                    tradeEventPublisher.publishTradeMatchedEvent(event);
                }
            });
        } else {
            log.warn("[MATCH] No active transaction, publishing immediately");
            tradeEventPublisher.publishTradeMatchedEvent(event);
        }
    }

    private void updateOppAfterMatch(EquityOrder opp, int newOppRemaining) {
        newOppRemaining = Math.max(0, newOppRemaining);
        opp.setRemainingQuantity(newOppRemaining);

        OrderStatus status = (newOppRemaining == 0) ? OrderStatus.FILLED : OrderStatus.PARTIALLY_FILLED;
        updateStatus(opp, status);

        equityOrderLogService.logStatusChange(
                opp, status, null,
                (newOppRemaining == 0) ? "Order fully matched/filled" : "Order partially matched"
        );
        orderRepository.saveAndFlush(opp);
        orderHistoryService.recordHistory(
                opp,
                null,
                null,
                LocalDateTime.now()
        );

        log.debug("[MATCH] Opp {} -> rem={}, status={}", opp.getOrderId(), newOppRemaining, status);
    }

    private void finalizeNewOrderAfterLoop(EquityOrder newOrder, int remainingQty,
                                           int filledTotal, BigDecimal lastExecPrice) {
        newOrder.setRemainingQuantity(remainingQty);

        if (remainingQty == 0) {
            updateStatus(newOrder, OrderStatus.FILLED);
            equityOrderLogService.logStatusChange(newOrder, OrderStatus.FILLED, null, "Order fully matched/filled");
        } else if (newOrder.getOrderType() == OrderType.MARKET) {
            if (filledTotal > 0 && lastExecPrice != null) {
                newOrder.setOrderType(OrderType.DAY);
                newOrder.setPrice(lastExecPrice);
                updateStatus(newOrder, OrderStatus.PARTIALLY_FILLED);
                equityOrderLogService.logStatusChange(newOrder, OrderStatus.PARTIALLY_FILLED, null,
                        "MTL: remainder resting at last executed price");
            } else {
                updateStatus(newOrder, OrderStatus.PENDING);
                equityOrderLogService.logStatusChange(newOrder, OrderStatus.PENDING, null,
                        "Market order pending: awaiting opposite liquidity");
            }
        } else {
            if (filledTotal > 0) {
            updateStatus(newOrder, OrderStatus.PARTIALLY_FILLED);
            equityOrderLogService.logStatusChange(newOrder, OrderStatus.PARTIALLY_FILLED, null, "Order partially matched");
            } else {
            updateStatus(newOrder, OrderStatus.PENDING);
            equityOrderLogService.logStatusChange(newOrder, OrderStatus.PENDING, null, "No match; resting on book");
        }
        }

        orderRepository.save(newOrder);
        orderHistoryService.recordHistory(newOrder, null, null, LocalDateTime.now());
    }

    private void recordMatch(Integer matchId, EquityOrder taker, EquityOrder maker) {
        EquityOrder buy  = (taker.getOrderSide() == OrderSide.BUY) ? taker : maker;
        EquityOrder sell = (taker.getOrderSide() == OrderSide.BUY) ? maker : taker;

        orderMatchRepository.save(
                EquityOrderMatch.builder()
                        .matchId(matchId)
                        .buyOrder(buy)
                        .sellOrder(sell)
                        .build()
        );
    }
}
