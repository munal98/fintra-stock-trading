package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.external.ExternalEquityInfoDto;
import com.fintra.stocktrading.model.dto.external.ExternalPriceRecordDto;
import com.fintra.stocktrading.model.dto.response.EquityPriceResponse;

import java.util.List;

public interface EquityApiService {

    /**
     * Retrieves equity information from external API
     *
     * @return list of external equity info DTOs
     */
    List<ExternalEquityInfoDto> getEquityInfoFromExternalApi();

    /**
     * Retrieves price history for a specific asset code
     *
     * @param assetCode the asset code to get price history for
     * @return list of external price record DTOs
     */
    List<ExternalPriceRecordDto> getPriceHistoryByAssetCode(String assetCode);

    /**
     * Retrieves all price data from external API
     *
     * @return list of external price record DTOs
     */
    List<ExternalPriceRecordDto> getAllPricesFromApi();

    /**
     * Combines equity info and price data to create complete equity responses
     *
     * @return list of equity price responses with combined data
     */
    List<EquityPriceResponse> getAllEquities();

    /**
     * @param page zero-based page index
     * @param size maximum number of items on a page
     * @param filter (optional) assetCode **OR** prefix filter on equityName
     */
    List<EquityPriceResponse> getEquitiesForFrontend(
            int page,
            int size,
            String filter
    );

    /**
     * Retrieves equity information for a specific asset code
     *
     * @param assetCode the asset code to search for
     * @return external equity info DTO or null if not found
     */
    ExternalEquityInfoDto getEquityInfoByAssetCode(String assetCode);

    /**
     * Retrieves paginated price history for a specific asset code
     *
     * @param assetCode the asset code to get history for
     * @param page      the page number (0-based)
     * @param size      the number of items per page
     * @return paginated list of equity price responses with historical data
     */
    List<EquityPriceResponse> getEquityHistoryByAssetCode(
            String assetCode,
            int page,
            int size
    );

    /**
     * Retrieves all price history data for all equities within a date range using batch API call
     * This method is optimized for bulk data fetching during initialization
     *
     * @param startDate the start date in format "YYYY-MM-DD"
     * @param endDate   the end date in format "YYYY-MM-DD"
     * @return list of external price record DTOs for all equities in the date range
     */
    List<ExternalPriceRecordDto> getAllPriceHistoryByDateRange(String startDate, String endDate);
}
