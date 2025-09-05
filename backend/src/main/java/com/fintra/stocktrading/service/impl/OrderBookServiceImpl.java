package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.mapper.OrderBookMapper;
import com.fintra.stocktrading.model.dto.response.OrderBookResponse;
import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.model.enums.OrderSide;
import com.fintra.stocktrading.model.enums.OrderStatus;
import com.fintra.stocktrading.repository.EquityOrderMatchRepository;
import com.fintra.stocktrading.repository.EquityOrderRepository;
import com.fintra.stocktrading.repository.EquityRepository;
import com.fintra.stocktrading.service.OrderBookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderBookServiceImpl implements OrderBookService {

    private final EquityOrderRepository equityOrderRepository;
    private final EquityRepository equityRepository;
    private final OrderBookMapper orderBookMapper;
    private final EquityOrderMatchRepository equityOrderMatchRepository;

    private static final List<OrderStatus> ORDERBOOK_STATUSES = Arrays.asList(
            OrderStatus.PENDING,
            OrderStatus.PARTIALLY_FILLED,
            OrderStatus.UPDATED
    );

    @Override
    @Transactional(readOnly = true)
    public OrderBookResponse getOrderBookByEquityId(Integer equityId) {
        log.info("Getting orderbook for equity ID: {}", equityId);

        Equity equity = equityRepository.findById(equityId)
                .orElseThrow(() -> new NotFoundException("Equity not found with ID: " + equityId));

        List<EquityOrder> buyOrders = equityOrderRepository.findOrderBookOrdersByEquityIdAndSide(
                equityId, ORDERBOOK_STATUSES, OrderSide.BUY
        );

        List<EquityOrder> sellOrders = equityOrderRepository.findOrderBookOrdersByEquityIdAndSide(
                equityId, ORDERBOOK_STATUSES, OrderSide.SELL
        );

        log.info("Found {} buy orders and {} sell orders for equity ID: {}",
                buyOrders.size(), sellOrders.size(), equityId);

        return orderBookMapper.toOrderBookResponse(
                equityId,
                equity.getEquityCode(),
                buyOrders,
                sellOrders
        );
    }

    @Override
    @Transactional(readOnly = true)
    public OrderBookResponse getOrderBookByEquityIdExcludingOrder(Integer equityId, Integer excludeOrderId) {
        log.info("Getting orderbook for equity ID: {} excluding order ID: {}", equityId, excludeOrderId);

        Equity equity = equityRepository.findById(equityId)
                .orElseThrow(() -> new NotFoundException("Equity not found with ID: " + equityId));

        List<Integer> matchedOrderIds = equityOrderMatchRepository.findMatchedOrderIds(excludeOrderId);

        List<Integer> excludeOrderIds = new ArrayList<>(matchedOrderIds);
        excludeOrderIds.add(excludeOrderId);
        
        log.info("Excluding order ID: {} and its {} matched orders: {}", 
                excludeOrderId, matchedOrderIds.size(), matchedOrderIds);

        List<EquityOrder> buyOrders;
        List<EquityOrder> sellOrders;

        if (excludeOrderIds.size() == 1) {
            buyOrders = equityOrderRepository.findOrderBookOrdersByEquityIdAndSideExcludingOrder(
                    equityId, ORDERBOOK_STATUSES, OrderSide.BUY, excludeOrderId
            );
            sellOrders = equityOrderRepository.findOrderBookOrdersByEquityIdAndSideExcludingOrder(
                    equityId, ORDERBOOK_STATUSES, OrderSide.SELL, excludeOrderId
            );
        } else {
            buyOrders = equityOrderRepository.findOrderBookOrdersByEquityIdAndSideExcludingMultipleOrders(
                    equityId, ORDERBOOK_STATUSES, OrderSide.BUY, excludeOrderIds
            );
            sellOrders = equityOrderRepository.findOrderBookOrdersByEquityIdAndSideExcludingMultipleOrders(
                    equityId, ORDERBOOK_STATUSES, OrderSide.SELL, excludeOrderIds
            );
        }

        log.info("Found {} buy orders and {} sell orders for equity ID: {} (excluding {} orders total)",
                buyOrders.size(), sellOrders.size(), equityId, excludeOrderIds.size());

        return orderBookMapper.toOrderBookResponse(
                equityId,
                equity.getEquityCode(),
                buyOrders,
                sellOrders
        );
    }
}
