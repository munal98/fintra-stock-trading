package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.model.dto.request.CashDepositRequest;
import com.fintra.stocktrading.model.dto.request.CashTransferRequest;
import com.fintra.stocktrading.model.dto.request.CashWithdrawRequest;
import com.fintra.stocktrading.model.dto.response.CashTransactionResponse;
import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.exception.BadRequestException;
import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.CashBalance;
import com.fintra.stocktrading.model.entity.CashTransaction;
import com.fintra.stocktrading.model.enums.TransactionType;
import com.fintra.stocktrading.repository.AccountRepository;
import com.fintra.stocktrading.repository.CashBalanceRepository;
import com.fintra.stocktrading.repository.CashTransactionRepository;
import com.fintra.stocktrading.service.CashTransactionService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CashTransactionServiceImpl implements CashTransactionService {

    private final AccountRepository accountRepository;
    private final CashBalanceRepository cashBalanceRepository;
    private final CashTransactionRepository cashTransactionRepository;

    @Override
    @Transactional
    public CashTransactionResponse deposit(CashDepositRequest request) {
        Account account = findAccountById(request.getAccountId());
        CashBalance cashBalance = findCashBalanceByAccount(account);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Deposit amount must be positive!");
        }

        BigDecimal newBalance = cashBalance.getFreeBalance().add(request.getAmount());
        cashBalance.setFreeBalance(newBalance);
        cashBalance.setUpdatedAt(LocalDateTime.now());
        cashBalanceRepository.save(cashBalance);

        CashTransaction cashTransaction = CashTransaction.builder()
                .account(account)
                .amount(request.getAmount())
                .transactionType(TransactionType.DEPOSIT)
                .transactionTime(LocalDateTime.now())
                .build();
        cashTransactionRepository.save(cashTransaction);

        return mapToResponse(cashTransaction, newBalance);
    }

    @Override
    @Transactional
    public CashTransactionResponse withdraw(CashWithdrawRequest request) {
        Account account = findAccountById(request.getAccountId());
        CashBalance cashBalance = findCashBalanceByAccount(account);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Withdraw amount must be positive!");
        }

        if (cashBalance.getFreeBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient free balance for withdraw!");
        }

        BigDecimal newBalance = cashBalance.getFreeBalance().subtract(request.getAmount());
        cashBalance.setFreeBalance(newBalance);
        cashBalance.setUpdatedAt(LocalDateTime.now());
        cashBalanceRepository.save(cashBalance);

        CashTransaction cashTransaction = CashTransaction.builder()
                .account(account)
                .amount(request.getAmount())
                .transactionType(TransactionType.WITHDRAW)
                .transactionTime(LocalDateTime.now())
                .build();
        cashTransactionRepository.save(cashTransaction);

        return mapToResponse(cashTransaction, newBalance);
    }

    @Override
    @Transactional
    public CashTransactionResponse transfer(CashTransferRequest request) {
        Account sender = findAccountById(request.getSenderAccountId());
        Account receiver = findAccountById(request.getReceiverAccountId());
        CashBalance senderBalance = findCashBalanceByAccount(sender);
        CashBalance receiverBalance = findCashBalanceByAccount(receiver);

        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Transfer amount must be positive!");
        }
        if (senderBalance.getFreeBalance().compareTo(request.getAmount()) < 0) {
            throw new BadRequestException("Insufficient free balance for transfer!");
        }
        if (sender.getAccountId().equals(receiver.getAccountId())) {
            throw new BadRequestException("Sender and receiver accounts must be different!");
        }

        senderBalance.setFreeBalance(senderBalance.getFreeBalance().subtract(request.getAmount()));
        receiverBalance.setFreeBalance(receiverBalance.getFreeBalance().add(request.getAmount()));
        senderBalance.setUpdatedAt(LocalDateTime.now());
        receiverBalance.setUpdatedAt(LocalDateTime.now());
        cashBalanceRepository.save(senderBalance);
        cashBalanceRepository.save(receiverBalance);

        CashTransaction senderTransaction = CashTransaction.builder()
                .account(sender)
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER_OUT)
                .transactionTime(LocalDateTime.now())
                .build();

        CashTransaction receiverTransaction = CashTransaction.builder()
                .account(receiver)
                .amount(request.getAmount())
                .transactionType(TransactionType.TRANSFER_IN)
                .transactionTime(LocalDateTime.now())
                .build();

        cashTransactionRepository.save(senderTransaction);
        cashTransactionRepository.save(receiverTransaction);

        return mapToResponse(senderTransaction, senderBalance.getFreeBalance());
    }


    private Account findAccountById(Integer accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found with id: " + accountId));
    }

    private CashBalance findCashBalanceByAccount(Account account) {
        return cashBalanceRepository.findByAccount(account)
                .orElseThrow(() -> new NotFoundException("CashBalance not found for account id: " + account.getAccountId()));
    }

    private CashTransactionResponse mapToResponse(CashTransaction transaction, BigDecimal newBalance) {
        CashTransactionResponse response = new CashTransactionResponse();
        response.setTransactionId(transaction.getTransactionId());
        response.setAccountId(transaction.getAccount().getAccountId());
        response.setAmount(transaction.getAmount());
        response.setTransactionType(transaction.getTransactionType());
        response.setTransactionTime(transaction.getTransactionTime());
        response.setNewFreeBalance(newBalance);
        return response;
    }
}
