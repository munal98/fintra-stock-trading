package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.mapper.EquityDistributionMapper;
import com.fintra.stocktrading.model.dto.response.EquityDistributionDto;
import com.fintra.stocktrading.model.entity.EquityDistribution;
import com.fintra.stocktrading.model.entity.EquityOrder;
import com.fintra.stocktrading.repository.EquityDistributionRepository;
import com.fintra.stocktrading.service.EquityDistributionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EquityDistributionServiceImpl implements EquityDistributionService {
    private final EquityDistributionRepository equityDistributionRepository;
    private final EquityDistributionMapper equityDistributionMapper;

    @Override
    public void createDistribution(EquityOrder order, Integer quantity, BigDecimal price, String side) {
        EquityDistribution distribution = new EquityDistribution();
        distribution.setEquityOrder(order); 
        distribution.setEquity(order.getEquity());
        distribution.setAccount(order.getAccount());
        distribution.setDistributionQuantity(quantity);
        distribution.setPrice(price);
        distribution.setSide(side);
        distribution.setCreatedAt(LocalDateTime.now());

        equityDistributionRepository.save(distribution);
    }

    @Override
    public List<EquityDistributionDto> getAllDistributions() {
        return equityDistributionMapper.toDtoList(equityDistributionRepository.findAll());
    }

    @Override
    public List<EquityDistributionDto> getDistributionsByOrderId(Integer orderId) {
        return equityDistributionMapper.toDtoList(
                equityDistributionRepository.findByEquityOrder_OrderId(orderId)
        );
    }
}
