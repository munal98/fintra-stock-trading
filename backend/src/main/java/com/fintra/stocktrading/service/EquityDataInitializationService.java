package com.fintra.stocktrading.service;

public interface EquityDataInitializationService {
    
    /**
     * Initializes all equity data by:
     * 1. Clearing existing equity and price history data
     * 2. Fetching equity definitions from external API
     * 3. Fetching last 30 days of price history for all equities
     * 4. Storing all data in database using batch operations
     */
    void initializeEquityData();
    
    /**
     * Initializes equity definitions from external API.
     * Clears existing equity data and fetches fresh data.
     */
    void initializeEquityDefinitions();
    
    /**
     * Initializes price history for all equities for the last 30 days.
     * Clears existing price history and fetches fresh data.
     */
    void initializePriceHistory();
}
