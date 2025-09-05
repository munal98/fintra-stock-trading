package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.request.CashDepositRequest;
import com.fintra.stocktrading.model.dto.request.CashTransferRequest;
import com.fintra.stocktrading.model.dto.request.CashWithdrawRequest;
import com.fintra.stocktrading.model.dto.response.CashTransactionResponse;

public interface CashTransactionService {
    /**
     * Deposits a specified amount into the given account.
     *
     * @param request the deposit request containing account ID and amount
     * @return details of the cash transaction (id, account, amount, type, time, new balance)
     * @throws com.fintra.stocktrading.exception.NotFoundException if account or balance not found
     * @throws IllegalArgumentException if the deposit amount is not positive
     */
    CashTransactionResponse deposit(CashDepositRequest request);

    /**
     * Withdraws a specified amount from the given account.
     *
     * @param request the withdraw request containing account ID and amount
     * @return details of the cash transaction (id, account, amount, type, time, new balance)
     * @throws com.fintra.stocktrading.exception.NotFoundException if account or balance not found
     * @throws IllegalArgumentException if the withdraw amount is not positive
     * @throws com.fintra.stocktrading.exception.BadRequestException if insufficient balance
     */
    CashTransactionResponse withdraw(CashWithdrawRequest request);

    /**
     * Transfers a specified amount from one account to another.
     *
     * @param request the transfer request containing sender and receiver account IDs, and amount
     * @return details of the sender's cash transaction (id, account, amount, type, time, new balance)
     * @throws com.fintra.stocktrading.exception.NotFoundException if sender/receiver account or balance not found
     * @throws IllegalArgumentException if the transfer amount is not positive
     * @throws com.fintra.stocktrading.exception.BadRequestException if insufficient sender balance
     */
    CashTransactionResponse transfer(CashTransferRequest request);
}
