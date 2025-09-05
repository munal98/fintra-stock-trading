package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.Equity;
import com.fintra.stocktrading.model.entity.EquityStock;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EquityStockService {

    /**
     * Retrieves all equity stock records in the system.
     *
     * @return list of all equity stocks
     */
    List<EquityStock> getAllEquityStocks();

    /**
     * Retrieves an equity stock record by its ID.
     *
     * @param stockId the stock record ID
     * @return optional equity stock record
     */
    Optional<EquityStock> getEquityStockById(Integer stockId);

    /**
     * Retrieves an equity stock record for a specific account and equity.
     *
     * @param account the account entity
     * @param equity the equity entity
     * @return optional equity stock record
     */
    Optional<EquityStock> getEquityStockByAccountAndEquity(Account account, Equity equity);

    /**
     * Retrieves an equity stock record by account ID and equity ID.
     *
     * @param accountId the account ID (Long)
     * @param equityId the equity ID
     * @return optional equity stock record
     */
    Optional<EquityStock> getEquityStockByAccountIdAndEquityId(Long accountId, Integer equityId);

    /**
     * Retrieves an equity stock record by account ID and equity ID.
     *
     * @param accountId the account ID (Integer)
     * @param equityId the equity ID
     * @return optional equity stock record
     */
    Optional<EquityStock> getEquityStockByAccountIdAndEquityId(Integer accountId, Integer equityId);

    /**
     * Retrieves all equity stock records for a specific account.
     *
     * @param account the account entity
     * @return list of equity stocks for the account
     */
    List<EquityStock> getEquityStocksByAccount(Account account);

    /**
     * Retrieves all equity stock records for a specific equity.
     *
     * @param equity the equity entity
     * @return list of equity stocks for the equity
     */
    List<EquityStock> getEquityStocksByEquity(Equity equity);

    /**
     * Retrieves equity holdings for a specific account ID.
     *
     * @param accountId the account ID
     * @return list of equity holdings
     */
    List<EquityStock> getEquityHoldingsByAccountId(Integer accountId);

    /**
     * Retrieves equity holdings for multiple account IDs.
     * Used for bulk portfolio queries.
     *
     * @param accountIds list of account IDs
     * @return list of equity holdings for all accounts
     */
    List<EquityStock> getEquityHoldingsByAccountIds(List<Integer> accountIds);

    /**
     * Saves an equity stock record to the database.
     *
     * @param equityStock the equity stock to save
     * @return saved equity stock record
     */
    EquityStock saveEquityStock(EquityStock equityStock);

    /**
     * Updates an existing equity stock record.
     *
     * @param equityStock the equity stock to update
     * @return updated equity stock record
     */
    EquityStock updateEquityStock(EquityStock equityStock);

    /**
     * Deletes an equity stock record by ID.
     *
     * @param stockId the stock record ID to delete
     */
    void deleteEquityStock(Integer stockId);

    /**
     * Checks if an equity stock record exists by ID.
     *
     * @param stockId the stock record ID
     * @return true if exists, false otherwise
     */
    boolean existsById(Integer stockId);

    /**
     * Updates average cost after a buy trade using weighted average calculation.
     * Includes commission in the cost calculation.
     *
     * @param accountId the account ID
     * @param equityId the equity ID
     * @param tradeQuantity the quantity bought
     * @param tradePrice the price per share
     * @param commission the commission paid
     * @return updated equity stock record
     */
    EquityStock updateCostAfterBuyTrade(Integer accountId, Integer equityId, Integer tradeQuantity, 
                                       BigDecimal tradePrice, BigDecimal commission);

    /**
     * Updates average cost after a sell trade.
     * Average cost remains unchanged for sell trades.
     *
     * @param accountId the account ID
     * @param equityId the equity ID
     * @param tradeQuantity the quantity sold
     * @return updated equity stock record
     */
    EquityStock updateCostAfterSellTrade(Integer accountId, Integer equityId, Integer tradeQuantity);

    /**
     * Updates average cost after receiving an incoming transfer.
     * Uses weighted average calculation with transfer unit cost.
     *
     * @param accountId the account ID
     * @param equityId the equity ID
     * @param transferQuantity the quantity received
     * @param transferUnitCost the unit cost of transferred shares
     * @return updated equity stock record
     */
    EquityStock updateCostAfterIncomingTransfer(Integer accountId, Integer equityId, 
                                               Integer transferQuantity, BigDecimal transferUnitCost);

    /**
     * Updates average cost after an outgoing transfer.
     * Average cost remains unchanged for outgoing transfers.
     *
     * @param accountId the account ID
     * @param equityId the equity ID
     * @param transferQuantity the quantity transferred out
     * @return updated equity stock record
     */
    EquityStock updateCostAfterOutgoingTransfer(Integer accountId, Integer equityId, Integer transferQuantity);

    /**
     * Calculates weighted average cost for combining existing and new positions.
     *
     * @param currentQuantity the current quantity held
     * @param currentTotalCost the current total cost
     * @param newQuantity the new quantity being added
     * @param newUnitCost the unit cost of new quantity
     * @return calculated weighted average cost
     */
    BigDecimal calculateWeightedAverageCost(Integer currentQuantity, BigDecimal currentTotalCost, 
                                          Integer newQuantity, BigDecimal newUnitCost);

    /**
     * Updates stock quantity after receiving an incoming transfer.
     *
     * @param accountId the account ID
     * @param equityId the equity ID
     * @param transferQuantity the quantity received
     * @return updated equity stock record
     */
    EquityStock updateQuantityAfterIncomingTransfer(Integer accountId, Integer equityId, Integer transferQuantity);

    /**
     * Updates stock quantity after an outgoing transfer.
     *
     * @param accountId the account ID
     * @param equityId the equity ID
     * @param transferQuantity the quantity transferred out
     * @return updated equity stock record
     */
    EquityStock updateQuantityAfterOutgoingTransfer(Integer accountId, Integer equityId, Integer transferQuantity);

    /**
     * Updates stock after an external transfer (from/to external institutions).
     *
     * @param accountId the account ID
     * @param equityId the equity ID
     * @param transferQuantity the quantity transferred
     * @return updated equity stock record
     */
    EquityStock updateStockAfterExternalTransfer(Integer accountId, Integer equityId, Integer transferQuantity);

    /**
     * Blocks a specified quantity of stock for pending sell orders.
     * Moves quantity from free to blocked.
     *
     * @param accountId the account ID
     * @param equityId the equity ID
     * @param quantity the quantity to block
     */
    void blockStock(Integer accountId, Integer equityId, Integer quantity);

    /**
     * Unblocks a specified quantity of stock.
     * Moves quantity from blocked back to free.
     *
     * @param accountId the account ID
     * @param equityId the equity ID
     * @param quantity the quantity to unblock
     */
    void unblockStock(Integer accountId, Integer equityId, Integer quantity);

    /**
     * Checks if an account has enough free stock for a transaction.
     *
     * @param accountId the account ID
     * @param equityId the equity ID
     * @param quantity the required quantity
     * @return true if sufficient stock available, false otherwise
     */
    boolean hasEnoughStock(Integer accountId, Integer equityId, Integer quantity);

    /**
     * Unblocks stock quantity (alternative method signature).
     *
     * @param accountId the account ID (Long)
     * @param equityId the equity ID
     * @param quantity the quantity to unblock
     */
    void unblock(Long accountId, Integer equityId, int quantity);
}
