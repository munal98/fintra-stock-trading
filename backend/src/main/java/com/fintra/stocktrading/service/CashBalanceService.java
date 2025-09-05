package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.response.CashBalanceResponse;
import java.math.BigDecimal;

public interface CashBalanceService {
    /**
     * Retrieves the current cash balance for a given account by its ID.
     *
     * @param accountId the ID of the account whose cash balance is requested
     * @return a response object containing the account's current cash balance and related details
     * @throws com.fintra.stocktrading.exception.NotFoundException if the account or balance is not found
     * @throws IllegalArgumentException if accountId is null or negative
     */
    CashBalanceResponse getBalanceByAccountId(Integer accountId);

    /**
     * Transfers money from one account to another.
     *
     * @param fromAccountId source account ID
     * @param toAccountId destination account ID  
     * @param amount amount to transfer
     */
    void transfer(Integer fromAccountId, Integer toAccountId, BigDecimal amount);

    /**
     * Blocks amount from free balance to blocked balance (used for BUY orders).
     *
     * @param accountId account ID
     * @param amount amount to block from free balance
     */
    void blockBalance(Integer accountId, BigDecimal amount);

    /**
     * Checks if account has enough free balance for the specified amount.
     *
     * @param accountId account ID
     * @param amount amount to check
     * @return true if account has enough free balance, false otherwise
     */
    boolean hasEnoughBalance(Integer accountId, BigDecimal amount);

    /**
     * Unblocks amount from blocked balance to free balance (used for cancelled orders).
     *
     * @param accountId account ID
     * @param amount amount to unblock from blocked to free
     */
    void unblockBalance(Integer accountId, BigDecimal amount);

    /**
     * Reduces blocked balance for an account (used in T+2 settlement for buy orders).
     *
     * @param accountId account ID
     * @param amount amount to reduce from blocked balance
     */
    void reduceBlockedBalance(Integer accountId, BigDecimal amount);

    /**
     * Adds amount to free balance for an account (used in T+2 settlement for sell orders).
     *
     * @param accountId account ID
     * @param amount amount to add to free balance
     */
    void addFreeBalance(Integer accountId, BigDecimal amount);

    /**
     * Moves amount from blocked balance to free balance (used for expired orders).
     *
     * @param accountId account ID
     * @param amount amount to move from blocked to free
     */
    void moveBlockedToFree(Integer accountId, BigDecimal amount);
}
