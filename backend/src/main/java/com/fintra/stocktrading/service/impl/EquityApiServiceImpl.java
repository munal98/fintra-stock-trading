package com.fintra.stocktrading.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fintra.stocktrading.exception.ServiceUnavailableException;
import com.fintra.stocktrading.mapper.EquityMapper;
import com.fintra.stocktrading.model.dto.external.ExternalEquityInfoDto;
import com.fintra.stocktrading.model.dto.external.ExternalPriceRecordDto;
import com.fintra.stocktrading.model.dto.response.EquityPriceResponse;
import com.fintra.stocktrading.service.EquityApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquityApiServiceImpl implements EquityApiService {

    @Value("${INFINA_API_INFO_URL}")
    private String infoUrl;

    @Value("${INFINA_API_PRICE_URL}")
    private String priceUrl;

    @Value("${INFINA_API_INFO_KEY}")
    private String apiKey;

    private final WebClient webClient;
    private final EquityMapper equityMapper;

    @Override
    public List<ExternalEquityInfoDto> getEquityInfoFromExternalApi() {
        String url = infoUrl + "?security_type=Stock&api_key=" + apiKey;

        try {
            log.debug("Fetching equity info from external API: {}", infoUrl + "?security_type=Stock&api_key=***");

            String json = webClient.get()
                    .uri(url)
                    .header("User-Agent", "curl/7.68.0")
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule());
            mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

            JsonNode root = mapper.readTree(json);
            JsonNode array = root.path("result").path("data").path("HisseTanim");

            if (!array.isArray()) {
                log.warn("Expected array but got different structure from equity info API");
                return Collections.emptyList();
            }

            ExternalEquityInfoDto[] dtos = mapper.treeToValue(array, ExternalEquityInfoDto[].class);
            log.info("Successfully fetched {} equity info records from external API", dtos.length);
            return List.of(dtos);

        } catch (Exception e) {
            log.error("Error fetching equity info from URL: {}", infoUrl + "?security_type=Stock&api_key=***", e);
            throw new ServiceUnavailableException("Failed to fetch equity info from external API: " + infoUrl + "?security_type=Stock&api_key=***", e);
        }
    }

    @Override
    public List<ExternalPriceRecordDto> getPriceHistoryByAssetCode(String assetCode) {
        ExternalEquityInfoDto info = getEquityInfoByAssetCode(assetCode);
        if (info == null) {
            log.warn("No equity info found for asset code: {}", assetCode);
            return Collections.emptyList();
        }

        String ticker = info.getTicker();

        String url = UriComponentsBuilder
                .fromUriString(priceUrl)
                .queryParam("asset_code", ticker)
                .queryParam("api_key", apiKey)
                .build()
                .toUriString();

        List<ExternalPriceRecordDto> prices = fetchPriceDataFromUrl(url);
        log.info("Fetched {} price records for ticker: {}", prices.size(), ticker);
        return prices;
    }

    @Override
    public List<ExternalPriceRecordDto> getAllPricesFromApi() {
        String url = UriComponentsBuilder
                .fromUriString(priceUrl)
                .queryParam("api_key", apiKey)
                .build()
                .toUriString();

        List<ExternalPriceRecordDto> prices = fetchPriceDataFromUrl(url);
        log.info("Fetched {} total price records from external API", prices.size());
        return prices;
    }

    private List<ExternalPriceRecordDto> fetchPriceDataFromUrl(String url) {
        try {
            log.debug("Fetching price data from URL: {}", url.replace(apiKey, "***"));

            String json = webClient.get()
                    .uri(url)
                    .header("User-Agent", "curl/7.68.0")
                    .header("Accept", "application/json")
                    .header("Content-Type", "application/json")
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("API Response received, length: {} characters", json != null ? json.length() : 0);
            log.debug("API Response content: {}", json);

            ObjectMapper mapper = new ObjectMapper()
                    .registerModule(new JavaTimeModule());

            JsonNode root = mapper.readTree(json);
            JsonNode array = root.path("result").path("data").path("HisseFiyat");

            log.info("JSON parsing - root exists: {}", !root.isMissingNode());
            log.info("JSON parsing - result exists: {}", !root.path("result").isMissingNode());
            log.info("JSON parsing - data exists: {}", !root.path("result").path("data").isMissingNode());
            log.info("JSON parsing - HisseFiyat exists: {}", !array.isMissingNode());
            log.info("JSON parsing - HisseFiyat is array: {}", array.isArray());
            log.info("JSON parsing - HisseFiyat array size: {}", array.isArray() ? array.size() : 0);

            if (!array.isArray()) {
                log.warn("Expected array but got different structure from price data API");
                return Collections.emptyList();
            }

            ExternalPriceRecordDto[] dtos = mapper.treeToValue(array, ExternalPriceRecordDto[].class);
            log.info("Successfully parsed {} price records from API", dtos.length);
            return List.of(dtos);

        } catch (Exception e) {
            log.error("Error fetching price data from URL: {}", url.replace(apiKey, "***"), e);
            throw new ServiceUnavailableException("Failed to fetch price data from external API: " + url.replace(apiKey, "***"), e);
        }
    }

    @Override
    public List<EquityPriceResponse> getAllEquities() {
        List<ExternalEquityInfoDto> infoList = getEquityInfoFromExternalApi();
        List<ExternalPriceRecordDto> priceList = getAllPricesFromApi();
        return equityMapper.toEquityPriceResponses(infoList, priceList);
    }

    @Override
    public List<EquityPriceResponse> getEquitiesForFrontend(int page, int size, String filter) {
        List<EquityPriceResponse> all = getAllEquities();

        List<EquityPriceResponse> filtered = all.stream()
                .filter(e -> {
                    if (filter == null || filter.isBlank()) return true;
                    String term = filter.trim().toLowerCase();
                    return e.getAssetCode().toLowerCase().startsWith(term)
                            || e.getEquityName().toLowerCase().startsWith(term);
                })
                .collect(Collectors.toList());

        int fromIndex = page * size;
        if (fromIndex >= filtered.size()) return Collections.emptyList();
        int toIndex = Math.min(fromIndex + size, filtered.size());
        return filtered.subList(fromIndex, toIndex);
    }

    @Override
    public ExternalEquityInfoDto getEquityInfoByAssetCode(String assetCode) {
        String code = assetCode.trim();
        String requestedPrefix = code.contains(".") ? code.split("\\.")[0] : code;

        return getEquityInfoFromExternalApi().stream()
                .filter(e -> {
                    String ticker = e.getTicker();
                    String baseCode = ticker.contains(".") ? ticker.split("\\.")[0] : ticker;
                    return ticker.equalsIgnoreCase(code) || baseCode.equalsIgnoreCase(requestedPrefix);
                })
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<EquityPriceResponse> getEquityHistoryByAssetCode(String assetCode, int page, int size) {
        List<ExternalPriceRecordDto> priceList = getPriceHistoryByAssetCode(assetCode);
        ExternalEquityInfoDto equityInfo = getEquityInfoByAssetCode(assetCode);

        List<EquityPriceResponse> full = equityMapper.toEquityPriceHistoryResponses(assetCode, equityInfo, priceList);

        int from = page * size;
        if (from >= full.size()) return Collections.emptyList();
        int to = Math.min(from + size, full.size());
        return full.subList(from, to);
    }

    @Override
    public List<ExternalPriceRecordDto> getAllPriceHistoryByDateRange(String startDate, String endDate) {
        String dateRangeParam = "[" + startDate + "," + endDate + "]";

        String url = priceUrl + "?data_date=" + dateRangeParam + "&api_key=" + apiKey;

        try {
            log.info("Fetching batch price history from external API: {}", priceUrl + "?data_date=" + dateRangeParam + "&api_key=***");
            log.info("Date range parameter: {}", dateRangeParam);
            log.info("Manual URL (sanitized): {}", priceUrl + "?data_date=" + dateRangeParam + "&api_key=***");
            List<ExternalPriceRecordDto> result = fetchPriceDataFromUrl(url);
            log.info("Batch API returned {} price records", result.size());
            return result;
        } catch (Exception e) {
            log.error("Error fetching batch price history from external API: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
}
