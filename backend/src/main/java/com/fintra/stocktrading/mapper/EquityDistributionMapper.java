package com.fintra.stocktrading.mapper;

import com.fintra.stocktrading.model.dto.response.EquityDistributionDto;
import com.fintra.stocktrading.model.entity.EquityDistribution;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EquityDistributionMapper {

    public EquityDistributionDto toDto(EquityDistribution entity) {
        if (entity == null) {
            return null;
        }

        EquityDistributionDto dto = new EquityDistributionDto();
        dto.setDistId(entity.getDistId());
        dto.setEquityOrderId(entity.getEquityOrder().getOrderId());
        dto.setDistributionQuantity(entity.getDistributionQuantity());
        dto.setPrice(entity.getPrice());
        dto.setTransactionTime(entity.getTransactionTime());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public List<EquityDistributionDto> toDtoList(List<EquityDistribution> entities) {
        return entities.stream()
                .map(this::toDto)
                .toList();
    }
}
