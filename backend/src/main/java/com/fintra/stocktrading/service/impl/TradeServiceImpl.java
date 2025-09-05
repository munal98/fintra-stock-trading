package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.mapper.TradeMapper;
import com.fintra.stocktrading.model.dto.response.TradeDto;
import com.fintra.stocktrading.model.entity.Trade;
import com.fintra.stocktrading.model.enums.TradeStatus;
import com.fintra.stocktrading.repository.TradeRepository;
import com.fintra.stocktrading.service.TradeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TradeServiceImpl implements TradeService {
    private final TradeRepository tradeRepository;
    private final TradeMapper tradeMapper;

    @Override
    public TradeDto getTradeById(Integer tradeId) {
        Trade trade = tradeRepository.findById(tradeId)
                .orElseThrow(() -> new NotFoundException("Trade not found"));
        return tradeMapper.toDto(trade);
    }

    @Override
    public List<TradeDto> getAllTrades() {
        return tradeRepository.findAll()
                .stream()
                .map(tradeMapper::toDto)
                .toList();
    }

    @Override
    public List<TradeDto> getTradesByOrderId(Integer orderId) {
        return tradeRepository.findByEquityOrder_OrderId(orderId)
                .stream()
                .map(tradeMapper::toDto)
                .toList();
    }

    @Override
    public List<TradeDto> getTradesByEquityId(Integer equityId) {
        return tradeRepository.findByEquityOrder_Equity_EquityId(equityId)
                .stream()
                .map(tradeMapper::toDto)
                .toList();
    }

    @Override
    public Page<TradeDto> getTradesByEquityIdAndDateRange(Long equityId,
                                                          LocalDateTime from,
                                                          LocalDateTime to,
                                                          Pageable pageable) {
        LocalDateTime now = LocalDateTime.now();
        if (from == null) {
            from = now.minusDays(30);
        }
        if (to == null) {
            to = now;
        }

        return tradeRepository.findByEquityIdAndDateRange(equityId.intValue(), from, to, pageable)
                .map(tradeMapper::toDto);
    }

    @Override
    public List<TradeDto> getSettledTrades() {
        return tradeRepository.findByStatus(TradeStatus.SETTLED)
                .stream()
                .map(tradeMapper::toDto)
                .toList();
    }
}
