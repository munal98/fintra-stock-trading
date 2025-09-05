package com.fintra.stocktrading.mapper;

import com.fintra.stocktrading.model.dto.external.ExternalEquityInfoDto;
import com.fintra.stocktrading.model.dto.external.ExternalPriceRecordDto;
import com.fintra.stocktrading.model.dto.response.EquityPriceResponse;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class EquityMapper {

    public EquityPriceResponse toEquityPriceResponse(
            ExternalPriceRecordDto priceRecord,
            ExternalEquityInfoDto equityInfo
    ) {
        if (priceRecord == null || equityInfo == null) return null;

        String ticker = priceRecord.getAssetCode();

        String assetCode = ticker.contains(".")
                ? ticker.split("\\.")[0]
                : ticker;

        boolean participation = false;
        if (equityInfo.getIndex() != null) {
            participation = Arrays.stream(equityInfo.getIndex().split(","))
                    .anyMatch(idx -> idx.startsWith("XK"));
        }

        return EquityPriceResponse.builder()
                .ticker(ticker)
                .assetCode(assetCode)
                .equityName(equityInfo.getEquityName())
                .market(equityInfo.getMarket())
                .openPrice(priceRecord.getOpenPrice())
                .closePrice(priceRecord.getClosePrice())
                .highPrice(priceRecord.getHighPrice())
                .lowPrice(priceRecord.getLowPrice())
                .dataDate(priceRecord.getDataDate())
                .participation(participation)
                .build();
    }

    public EquityPriceResponse toEquityPriceResponse(
            String assetCodeParam,
            ExternalEquityInfoDto equityInfo,
            ExternalPriceRecordDto priceRecord
    ) {
        if (priceRecord == null || equityInfo == null) return null;

        String ticker = assetCodeParam;
        String assetCode = ticker.contains(".")
                ? ticker.split("\\.")[0]
                : ticker;

        boolean participation = false;
        if (equityInfo.getIndex() != null) {
            participation = Arrays.stream(equityInfo.getIndex().split(","))
                    .anyMatch(idx -> idx.startsWith("XK"));
        }

        return EquityPriceResponse.builder()
                .ticker(ticker)
                .assetCode(assetCode)
                .equityName(equityInfo.getEquityName())
                .market(equityInfo.getMarket())
                .openPrice(priceRecord.getOpenPrice())
                .closePrice(priceRecord.getClosePrice())
                .highPrice(priceRecord.getHighPrice())
                .lowPrice(priceRecord.getLowPrice())
                .dataDate(priceRecord.getDataDate())
                .participation(participation)
                .build();
    }

    public List<EquityPriceResponse> toEquityPriceResponses(
            List<ExternalEquityInfoDto> infoList,
            List<ExternalPriceRecordDto> priceList
    ) {
        Map<String, ExternalEquityInfoDto> equityMap = infoList.stream()
                .collect(Collectors.toMap(ExternalEquityInfoDto::getTicker, Function.identity()));

        return priceList.stream()
                .map(price -> {
                    ExternalEquityInfoDto info = equityMap.get(price.getAssetCode());
                    return toEquityPriceResponse(price, info);
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<EquityPriceResponse> toEquityPriceHistoryResponses(
            String assetCode,
            ExternalEquityInfoDto equityInfo,
            List<ExternalPriceRecordDto> priceList
    ) {
        if (equityInfo == null || priceList == null || priceList.isEmpty()) {
            return List.of();
        }

        return priceList.stream()
                .map(price -> toEquityPriceResponse(assetCode, equityInfo, price))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
