package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.response.EquityInfoResponse;
import com.fintra.stocktrading.model.dto.response.EquityPriceResponse;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;

public interface EquityService {

    /**
     * Retrieves all equity prices from the database.
     *
     * @return list of all equity prices
     */
    List<EquityPriceResponse> getAllEquities();

    /**
     * Retrieves paginated equity prices with optional filtering.
     * Used by frontend for equity listing with search functionality.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @param filter optional filter for equity code or name
     * @return paginated equity prices
     */
    Page<EquityPriceResponse> getEquitiesForFrontend(int page, int size, String filter);

    /**
     * Retrieves price history for a specific equity within a date range.
     *
     * @param assetCode the equity asset code (e.g., "GARAN.E")
     * @param startDate the start date for price history
     * @param endDate the end date for price history
     * @return list of price records within the date range
     */
    List<EquityPriceResponse> getPriceHistoryByAssetCode(String assetCode, LocalDate startDate, LocalDate endDate);

    /**
     * Retrieves static information for a specific equity.
     *
     * @param assetCode the equity asset code (e.g., "GARAN.E")
     * @return equity information including name, market, participation status
     */
    EquityInfoResponse getEquityInfoByAssetCode(String assetCode);

    /**
     * Retrieves the latest price for a specific equity.
     * Used for real-time pricing and portfolio calculations.
     *
     * @param assetCode the equity asset code (e.g., "GARAN.E")
     * @return latest price data for the equity
     */
    EquityPriceResponse getLatestPriceByAssetCode(String assetCode);

    /**
     * Retrieves the latest prices for all equities in the system.
     * Used for bulk price updates and market overview displays.
     *
     * @return list of latest prices for all equities
     */
    List<EquityPriceResponse> getLatestPricesForAllEquities();
}
