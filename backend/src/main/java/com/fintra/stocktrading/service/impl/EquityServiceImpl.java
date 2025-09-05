package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.dto.response.EquityInfoResponse;
import com.fintra.stocktrading.model.dto.response.EquityPriceResponse;
import com.fintra.stocktrading.exception.ResourceNotFoundException;
import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityPriceHistory;
import com.fintra.stocktrading.repository.EquityPriceHistoryRepository;
import com.fintra.stocktrading.repository.EquityRepository;
import com.fintra.stocktrading.service.EquityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class EquityServiceImpl implements EquityService {

    private final EquityRepository equityRepository;
    private final EquityPriceHistoryRepository priceHistoryRepository;

    @Override
    public List<EquityPriceResponse> getAllEquities() {
        log.debug("Fetching all equities with latest prices from database");

        List<Equity> equities = equityRepository.findAll();
        List<EquityPriceResponse> responses = new ArrayList<>();

        for (Equity equity : equities) {
            Optional<EquityPriceHistory> latestPrice = priceHistoryRepository.findLatestByEquity(equity);
            EquityPriceResponse response = buildEquityPriceResponse(equity, latestPrice.orElse(null));
            responses.add(response);
        }

        log.debug("Retrieved {} equities from database", responses.size());
        return responses;
    }

    @Override
    public Page<EquityPriceResponse> getEquitiesForFrontend(int page, int size, String filter) {
        log.debug("Fetching equities for frontend - page: {}, size: {}, filter: '{}'", page, size, filter);

        Pageable pageable = PageRequest.of(page, size);

        Page<Equity> equityPage = equityRepository.findEquitiesWithFilter(filter, pageable);

        List<Equity> equities = equityPage.getContent();
        List<EquityPriceHistory> latestPrices = equities.isEmpty() ?
            List.of() : priceHistoryRepository.findLatestPricesForEquities(equities);

        Map<Integer, EquityPriceHistory> priceMap = latestPrices.stream()
                .collect(Collectors.toMap(
                    price -> price.getEquity().getEquityId(),
                    Function.identity()
                ));

        List<EquityPriceResponse> responses = equities.stream()
                .map(equity -> {
                    EquityPriceHistory priceHistory = priceMap.get(equity.getEquityId());
                    return buildEquityPriceResponse(equity, priceHistory);
                })
                .toList();

        log.debug("Returning {} equities out of {} total (page {} of {})",
                responses.size(), equityPage.getTotalElements(), page + 1, equityPage.getTotalPages());

        return new PageImpl<>(responses, pageable, equityPage.getTotalElements());
    }

    @Override
    public List<EquityPriceResponse> getPriceHistoryByAssetCode(String assetCode, LocalDate startDate, LocalDate endDate) {
        log.debug("Fetching price history for asset code: {} from {} to {}", assetCode, startDate, endDate);

        Equity equity = findEquityByAssetCode(assetCode);

        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusDays(30);
        }

        List<EquityPriceHistory> priceHistories = priceHistoryRepository
                .findByEquityAndDataDateBetween(equity, startDate, endDate);

        List<EquityPriceResponse> responses = priceHistories.stream()
                .map(priceHistory -> buildEquityPriceResponse(equity, priceHistory))
                .toList();

        log.debug("Retrieved {} price history records for {}", responses.size(), assetCode);
        return responses;
    }

    @Override
    public EquityInfoResponse getEquityInfoByAssetCode(String assetCode) {
        log.debug("Fetching equity info for asset code: {}", assetCode);

        Equity equity = findEquityByAssetCode(assetCode);

        EquityInfoResponse response = EquityInfoResponse.builder()
                .equityId(equity.getEquityId())
                .ticker(equity.getTicker())
                .assetCode(equity.getEquityCode())
                .equityName(equity.getEquityName())
                .market(equity.getMarket())
                .country(equity.getCountry())
                .participation(equity.getParticipation())
                .build();

        log.debug("Retrieved equity info for: {}", assetCode);
        return response;
    }

    @Override
    public EquityPriceResponse getLatestPriceByAssetCode(String assetCode) {
        log.debug("Fetching latest price for asset code: {}", assetCode);

        Equity equity = findEquityByAssetCode(assetCode);
        Optional<EquityPriceHistory> latestPrice = priceHistoryRepository.findLatestByEquity(equity);

        if (latestPrice.isEmpty()) {
            log.warn("No price history found for asset code: {}", assetCode);
            return buildEquityPriceResponse(equity, null);
        }

        EquityPriceResponse response = buildEquityPriceResponse(equity, latestPrice.get());
        log.debug("Retrieved latest price for: {}", assetCode);
        return response;
    }

    @Override
    public List<EquityPriceResponse> getLatestPricesForAllEquities() {
        log.debug("Fetching latest prices for all equities");

        List<EquityPriceHistory> latestPrices = priceHistoryRepository.findLatestPricesForAllEquities();

        List<EquityPriceResponse> responses = latestPrices.stream()
                .map(priceHistory -> buildEquityPriceResponse(priceHistory.getEquity(), priceHistory))
                .toList();

        log.debug("Retrieved latest prices for {} equities", responses.size());
        return responses;
    }

    private Equity findEquityByAssetCode(String assetCode) {
        Optional<Equity> equity = equityRepository.findByTickerOrEquityCode(assetCode);

        if (equity.isEmpty() && !assetCode.endsWith(".E")) {
            equity = equityRepository.findByTickerOrEquityCode(assetCode + ".E");
        }

        if (equity.isEmpty() && assetCode.endsWith(".E")) {
            String codeWithoutSuffix = assetCode.substring(0, assetCode.length() - 2);
            equity = equityRepository.findByTickerOrEquityCode(codeWithoutSuffix);
        }

        return equity.orElseThrow(() ->
                new ResourceNotFoundException("Equity not found with asset code: " + assetCode));
    }

    private EquityPriceResponse buildEquityPriceResponse(Equity equity, EquityPriceHistory priceHistory) {
        EquityPriceResponse.EquityPriceResponseBuilder builder = EquityPriceResponse.builder()
                .equityId(equity.getEquityId())
                .assetCode(equity.getEquityCode())
                .equityName(equity.getEquityName())
                .ticker(equity.getTicker())
                .market(equity.getMarket())
                .participation(equity.getParticipation());

        if (priceHistory != null) {
            builder.dataDate(priceHistory.getDataDate())
                   .openPrice(priceHistory.getOpenPrice())
                   .closePrice(priceHistory.getClosePrice())
                   .highPrice(priceHistory.getHighPrice())
                   .lowPrice(priceHistory.getLowPrice());
        }

        return builder.build();
    }
}
