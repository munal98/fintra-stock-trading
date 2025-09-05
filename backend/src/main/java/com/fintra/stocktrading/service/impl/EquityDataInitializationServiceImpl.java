package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.dto.external.ExternalEquityInfoDto;
import com.fintra.stocktrading.model.dto.external.ExternalPriceRecordDto;
import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityPriceHistory;
import com.fintra.stocktrading.model.enums.EquityType;
import com.fintra.stocktrading.repository.EquityPriceHistoryRepository;
import com.fintra.stocktrading.repository.EquityRepository;
import com.fintra.stocktrading.service.EquityApiService;
import com.fintra.stocktrading.service.EquityDataInitializationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EquityDataInitializationServiceImpl implements EquityDataInitializationService {

    private final EquityRepository equityRepository;
    private final EquityPriceHistoryRepository priceHistoryRepository;
    private final EquityApiService equityApiService;

    @Override
    @Transactional
    public void initializeEquityData() {
        log.info("Starting complete equity data initialization...");

        try {
            clearExistingData();

            initializeEquityDefinitions();

            initializePriceHistory();

            log.info("Equity data initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize equity data", e);
            throw new RuntimeException("Equity data initialization failed", e);
        }
    }

    @Override
    @Transactional
    public void initializeEquityDefinitions() {
        log.info("Initializing equity definitions from external API...");

        try {
            List<ExternalEquityInfoDto> externalEquities = equityApiService.getEquityInfoFromExternalApi();
            log.info("Fetched {} equity definitions from external API", externalEquities.size());

            if (externalEquities.isEmpty()) {
                log.warn("No equity definitions received from external API");
                return;
            }

            Map<String, Equity> uniqueEquities = new HashMap<>();
            
            for (ExternalEquityInfoDto dto : externalEquities) {
                if (!isValidEquityInfo(dto)) {
                    continue;
                }
                
                Equity equity = convertToEquityEntity(dto);
                String key = equity.getEquityCode();
                
                if (uniqueEquities.containsKey(key)) {
                    log.warn("Duplicate equity_code found: {}. Keeping first occurrence.", key);
                } else {
                    uniqueEquities.put(key, equity);
                }
            }
            
            List<Equity> equities = new ArrayList<>(uniqueEquities.values());

            if (!equities.isEmpty()) {
                log.info("Deduplicated {} equity definitions, saving {} unique entries", 
                         externalEquities.size(), equities.size());
                equityRepository.saveAll(equities);
                log.info("Successfully saved {} equity definitions to database", equities.size());
            } else {
                log.warn("No valid equity definitions to save");
            }

        } catch (Exception e) {
            log.error("Failed to initialize equity definitions", e);
            throw new RuntimeException("Equity definitions initialization failed", e);
        }
    }

    @Override
    @Transactional
    public void initializePriceHistory() {
        log.info("Initializing price history for last 30 days...");

        try {
            // Calculate date range (last 30 days, ending yesterday)
            LocalDate endDate = LocalDate.now().minusDays(1);
            LocalDate startDate = endDate.minusDays(30);

            String startDateStr = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE);
            String endDateStr = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE);

            log.info("Fetching price history from {} to {}", startDateStr, endDateStr);

            // Fetch price history using batch API
            List<ExternalPriceRecordDto> priceRecords = equityApiService.getAllPriceHistoryByDateRange(startDateStr, endDateStr);
            log.info("Fetched {} price records from external API", priceRecords.size());

            if (priceRecords.isEmpty()) {
                log.warn("No price history received from external API");
                return;
            }

            // Pre-load all equities for efficient lookup
            Map<String, Equity> equityLookupMap = buildEquityLookupMap();

            // Group price records by asset code for efficient processing
            Map<String, List<ExternalPriceRecordDto>> pricesByAssetCode = priceRecords.stream()
                    .collect(Collectors.groupingBy(ExternalPriceRecordDto::getAssetCode));

            // Convert to entities and save
            List<EquityPriceHistory> priceHistories = new ArrayList<>();
            int processedEquities = 0;
            int skippedRecords = 0;

            for (Map.Entry<String, List<ExternalPriceRecordDto>> entry : pricesByAssetCode.entrySet()) {
                String assetCode = entry.getKey();
                List<ExternalPriceRecordDto> records = entry.getValue();

                Optional<Equity> equityOpt = findEquityByAssetCodeFromCache(assetCode, equityLookupMap);
                if (equityOpt.isEmpty()) {
                    log.debug("Skipping price data for unknown asset code: {} ({} records)",
                            assetCode, records.size());
                    skippedRecords += records.size();
                    continue;
                }

                Equity equity = equityOpt.get();
                for (ExternalPriceRecordDto record : records) {
                    if (isValidPriceRecord(record)) {
                        EquityPriceHistory priceHistory = convertToPriceHistoryEntity(equity, record);
                        priceHistories.add(priceHistory);
                    } else {
                        skippedRecords++;
                    }
                }
                processedEquities++;
            }

            // Batch save all price histories
            if (!priceHistories.isEmpty()) {
                priceHistoryRepository.saveAll(priceHistories);
                log.info("Successfully saved {} price history records for {} equities",
                        priceHistories.size(), processedEquities);
            }

            if (skippedRecords > 0) {
                log.info("Skipped {} invalid or unmatched price records", skippedRecords);
            }

        } catch (Exception e) {
            log.error("Failed to initialize price history", e);
            throw new RuntimeException("Price history initialization failed", e);
        }
    }

    private void clearExistingData() {
        log.info("Clearing existing equity data...");

        // Delete price history first (due to foreign key constraint)
        long priceHistoryCount = priceHistoryRepository.count();
        if (priceHistoryCount > 0) {
            priceHistoryRepository.deleteAll();
            log.info("Cleared {} existing price history records", priceHistoryCount);
        }

        // Delete equities
        long equityCount = equityRepository.count();
        if (equityCount > 0) {
            equityRepository.deleteAll();
            log.info("Cleared {} existing equity definitions", equityCount);
        }
    }

    private Map<String, Equity> buildEquityLookupMap() {
        List<Equity> allEquities = equityRepository.findAll();
        Map<String, Equity> lookupMap = new HashMap<>();

        for (Equity equity : allEquities) {
            // Add multiple lookup keys for flexible matching
            lookupMap.put(equity.getTicker(), equity);
            lookupMap.put(equity.getEquityCode(), equity);

            // Handle .E suffix variations
            if (equity.getTicker().endsWith(".E")) {
                lookupMap.put(equity.getTicker().substring(0, equity.getTicker().length() - 2), equity);
            } else {
                lookupMap.put(equity.getTicker() + ".E", equity);
            }

            if (equity.getEquityCode().endsWith(".E")) {
                lookupMap.put(equity.getEquityCode().substring(0, equity.getEquityCode().length() - 2), equity);
            } else {
                lookupMap.put(equity.getEquityCode() + ".E", equity);
            }
        }

        return lookupMap;
    }

    private Optional<Equity> findEquityByAssetCodeFromCache(String assetCode, Map<String, Equity> lookupMap) {
        Equity equity = lookupMap.get(assetCode);
        if (equity != null) {
            return Optional.of(equity);
        }

        equity = lookupMap.get(assetCode + ".E");
        if (equity != null) {
            return Optional.of(equity);
        }

        if (assetCode.endsWith(".E")) {
            equity = lookupMap.get(assetCode.substring(0, assetCode.length() - 2));
            if (equity != null) {
                return Optional.of(equity);
            }
        }

        return Optional.empty();
    }

    private boolean isValidEquityInfo(ExternalEquityInfoDto dto) {
        return dto != null
                && dto.getTicker() != null && !dto.getTicker().trim().isEmpty()
                && dto.getEquityName() != null && !dto.getEquityName().trim().isEmpty()
                && dto.getTicker().endsWith(".E");
    }

    private boolean isValidPriceRecord(ExternalPriceRecordDto dto) {
        return dto != null
                && dto.getAssetCode() != null && !dto.getAssetCode().trim().isEmpty()
                && dto.getDataDate() != null
                && dto.getClosePrice() != null && dto.getClosePrice().compareTo(java.math.BigDecimal.ZERO) > 0;
    }

    private Equity convertToEquityEntity(ExternalEquityInfoDto dto) {
        // Determine participation status based on index info
        boolean participation = dto.getIndex() != null
                && Arrays.stream(dto.getIndex().split(","))
                        .anyMatch(idx -> idx.trim().startsWith("XK"));

        return Equity.builder()
                .ticker(truncateString(dto.getTicker(), 30))
                .equityCode(truncateString(dto.getTicker(), 20))
                .equityName(truncateString(dto.getEquityName(), 200))
                .market(truncateString(dto.getMarket(), 1000))
                .country(truncateString(dto.getCountry(), 10))
                .indexInfo(truncateString(dto.getIndex(), 2000))
                .participation(participation)
                .equityType(EquityType.STOCK)
                .build();
    }

    private EquityPriceHistory convertToPriceHistoryEntity(Equity equity, ExternalPriceRecordDto dto) {
        return EquityPriceHistory.builder()
                .equity(equity)
                .dataDate(dto.getDataDate())
                .openPrice(dto.getOpenPrice())
                .closePrice(dto.getClosePrice())
                .highPrice(dto.getHighPrice())
                .lowPrice(dto.getLowPrice())
                .build();
    }

    private String extractAssetCode(String ticker) {
        // Extract asset code from ticker (remove .E suffix if present)
        if (ticker != null && ticker.contains(".")) {
            return ticker.split("\\.")[0];
        }
        return ticker;
    }

    private String truncateString(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        log.warn("Truncating field value from {} to {} characters: '{}'", 
                value.length(), maxLength, value.substring(0, Math.min(50, value.length())));
        return value.substring(0, maxLength);
    }
}
