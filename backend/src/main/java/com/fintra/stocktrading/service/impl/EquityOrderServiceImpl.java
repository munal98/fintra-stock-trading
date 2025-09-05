package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.BadRequestException;
import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.mapper.EquityOrderMapper;
import com.fintra.stocktrading.model.dto.request.EquityOrderRequest;
import com.fintra.stocktrading.model.dto.request.EquityOrderUpdateRequest;
import com.fintra.stocktrading.model.dto.response.EquityOrderResponse;
import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.entity.EquityStock;
import com.fintra.stocktrading.model.enums.OrderSide;
import com.fintra.stocktrading.model.enums.OrderStatus;
import com.fintra.stocktrading.model.enums.OrderType;
import com.fintra.stocktrading.repository.AccountRepository;
import com.fintra.stocktrading.repository.EquityOrderRepository;
import com.fintra.stocktrading.repository.EquityRepository;
import com.fintra.stocktrading.service.CashBalanceService;
import com.fintra.stocktrading.service.EquityOrderHistoryService;
import com.fintra.stocktrading.service.EquityOrderService;
import com.fintra.stocktrading.service.EquityStockService;
import com.fintra.stocktrading.service.OrderMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Service
@RequiredArgsConstructor
public class EquityOrderServiceImpl implements EquityOrderService {

    private final EquityOrderRepository equityOrderRepository;
    private final AccountRepository accountRepository;
    private final EquityRepository equityRepository;
    private final EquityOrderMapper equityOrderMapper;
    private final EquityOrderHistoryService orderHistoryService;
    private final OrderMatchingService orderMatchingService;
    private final CashBalanceService cashBalanceService;
    private final EquityStockService equityStockService;

    private static final String ERR_ORDER_NOT_FOUND   = "Order not found!";
    private static final String ERR_ACCOUNT_NOT_FOUND = "Account not found!";
    private static final String ERR_EQUITY_NOT_FOUND  = "Equity not found!";


    private static final ZoneId TR_ZONE = ZoneId.of("Europe/Istanbul");
    private static final LocalTime SESSION_START = LocalTime.of(10, 0);
    private static final LocalTime SESSION_END   = LocalTime.of(17, 0);

    private static final boolean AUTO_DETECT_DAY = true;

    private boolean isWithinDayWindow() {
        ZonedDateTime now = ZonedDateTime.now(TR_ZONE);
        if (now.getDayOfWeek() == DayOfWeek.SATURDAY || now.getDayOfWeek() == DayOfWeek.SUNDAY) return false;
        LocalTime t = now.toLocalTime();
        return !t.isBefore(SESSION_START) && !t.isAfter(SESSION_END);
    }
    private OrderType decideOrderType(OrderType requestedType) {
        boolean inWindow = isWithinDayWindow();

        if (requestedType == OrderType.DAY) {
            if (!inWindow) {
                throw new BadRequestException("DAY orders can only be placed on weekdays between 10:00 a.m. and 5:00 p.m. (Europe/Istanbul).");
            }
            return OrderType.DAY;
        }
        if (AUTO_DETECT_DAY && inWindow) {
            return OrderType.DAY;
        }
        return requestedType;
    }

    @Override
    @Transactional
    public EquityOrderResponse createOrder(EquityOrderRequest requestDto) {
        Account account = accountRepository.findById(requestDto.getAccountId())
                .orElseThrow(() -> new NotFoundException(ERR_ACCOUNT_NOT_FOUND));

        Equity equity = equityRepository.findById(requestDto.getEquityId())
                .orElseThrow(() -> new NotFoundException(ERR_EQUITY_NOT_FOUND));

        if (requestDto.getOrderSide() == OrderSide.BUY) {
            BigDecimal requiredAmount = requestDto.getPrice().multiply(BigDecimal.valueOf(requestDto.getOrderQuantity()));
            if (!cashBalanceService.hasEnoughBalance(account.getAccountId(), requiredAmount)) {
                throw new BadRequestException("Insufficient balance! Order rejected.");
            }
            cashBalanceService.blockBalance(account.getAccountId(), requiredAmount);
        }

        if (requestDto.getOrderSide() == OrderSide.SELL) {
            int availableLot = getUserPortfolioLot(account, equity);
            if (availableLot < requestDto.getOrderQuantity()) {
                throw new BadRequestException("Insufficient number of shares! Order rejected.");
            }
            equityStockService.blockStock(account.getAccountId(), equity.getEquityId(), requestDto.getOrderQuantity());
        }

        OrderType effectiveType = decideOrderType(requestDto.getOrderType());

        EquityOrder order = equityOrderMapper.toEntity(requestDto, account, equity);
        order.setOrderType(effectiveType);

        order.setFinalStatus(OrderStatus.PENDING);
        order.setCombinedStatus(OrderStatus.PENDING);
        order.setEntryDate(LocalDateTime.now());
        order.setOrderDate(LocalDate.now());
        order.setRemainingQuantity(requestDto.getOrderQuantity());

        EquityOrder savedOrder = equityOrderRepository.save(order);

        orderMatchingService.matchOrder(savedOrder);

        orderHistoryService.recordHistory(
                savedOrder,
                null,
                null,
                LocalDateTime.now()
        );
        return equityOrderMapper.toDto(savedOrder);
    }

    @Override
    @Transactional
    public EquityOrderResponse cancelOrder(Integer orderId) {
        EquityOrder order = equityOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ERR_ORDER_NOT_FOUND));

        if (order.getFinalStatus() == OrderStatus.FILLED
                || order.getFinalStatus() == OrderStatus.CANCELLED
                || order.getFinalStatus() == OrderStatus.EXPIRED){
            throw new BadRequestException("Order is finalized and cannot be cancelled!");
        }

        order.setFinalStatus(OrderStatus.CANCELLED);
        order.setCombinedStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());

        EquityOrder updatedOrder = equityOrderRepository.save(order);

        if (order.getOrderSide() == OrderSide.BUY) {
            BigDecimal blockedAmount = order.getPrice().multiply(BigDecimal.valueOf(order.getOrderQuantity()));
            cashBalanceService.unblockBalance(order.getAccount().getAccountId(), blockedAmount);
        }

        if (order.getOrderSide() == OrderSide.SELL) {
            equityStockService.unblockStock(order.getAccount().getAccountId(), order.getEquity().getEquityId(), order.getOrderQuantity());
        }

        orderHistoryService.recordHistory(
                updatedOrder,
                null,
                null,
                LocalDateTime.now()
        );
        return equityOrderMapper.toDto(updatedOrder);
    }

    @Override
    public EquityOrderResponse getOrderById(Integer orderId) {
        EquityOrder order = equityOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ERR_ORDER_NOT_FOUND));
        return equityOrderMapper.toDto(order);
    }

    private int getUserPortfolioLot(Account account, Equity equity) {
        return account.getEquityStocks().stream()
                .filter(stock -> stock.getEquity().getEquityId().equals(equity.getEquityId()))
                .mapToInt(EquityStock::getFreeQuantity)
                .sum();
    }

    @Override
    @Transactional
    public EquityOrderResponse updateOrder(Integer orderId, EquityOrderUpdateRequest requestDto) {
        EquityOrder order = equityOrderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException(ERR_ORDER_NOT_FOUND));

        if (!(order.getFinalStatus() == OrderStatus.PENDING || order.getFinalStatus() == OrderStatus.UPDATED)) {
            throw new BadRequestException("Only pending or updated orders can be updated!");
        }

        Integer prevQuantity = order.getOrderQuantity();
        BigDecimal prevPrice = order.getPrice();

        Integer newQuantity = requestDto.getOrderQuantity() != null ? requestDto.getOrderQuantity() : prevQuantity;
        BigDecimal newPrice = requestDto.getPrice() != null ? requestDto.getPrice() : prevPrice;

        Integer accountId = order.getAccount().getAccountId();
        Integer equityId = order.getEquity().getEquityId();

        if (order.getOrderSide() == OrderSide.BUY) {
            BigDecimal requiredAmount = newPrice.multiply(BigDecimal.valueOf(newQuantity));
            if (!cashBalanceService.hasEnoughBalance(accountId, requiredAmount)) {
                throw new BadRequestException("Insufficient balance for updated order!");
            }
            BigDecimal blockedAmount = prevPrice.multiply(BigDecimal.valueOf(prevQuantity));
            cashBalanceService.unblockBalance(accountId, blockedAmount);
            cashBalanceService.blockBalance(accountId, requiredAmount);
        }
        if (order.getOrderSide() == OrderSide.SELL) {
            if (!equityStockService.hasEnoughStock(accountId, equityId, newQuantity)) {
                throw new BadRequestException("Insufficient number of shares for updated order!");
            }
            equityStockService.unblockStock(accountId, equityId, prevQuantity);
            equityStockService.blockStock(accountId, equityId, newQuantity);
        }

        if (requestDto.getOrderType() != null) {
            order.setOrderType(decideOrderType(requestDto.getOrderType()));
        }

        if (requestDto.getOrderQuantity() != null) order.setOrderQuantity(requestDto.getOrderQuantity());
        if (requestDto.getPrice() != null)        order.setPrice(requestDto.getPrice());
        order.setFinalStatus(OrderStatus.UPDATED);
        order.setUpdatedAt(LocalDateTime.now());

        EquityOrder updatedOrder = equityOrderRepository.save(order);

        orderHistoryService.recordHistory(
                updatedOrder,
                prevQuantity,
                prevPrice,
                LocalDateTime.now()
        );

        return equityOrderMapper.toDto(updatedOrder);
    }
}
