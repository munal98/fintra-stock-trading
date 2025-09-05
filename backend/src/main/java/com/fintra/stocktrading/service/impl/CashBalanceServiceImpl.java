package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.model.dto.response.CashBalanceResponse;
import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.CashBalance;
import com.fintra.stocktrading.repository.AccountRepository;
import com.fintra.stocktrading.repository.CashBalanceRepository;
import com.fintra.stocktrading.service.CashBalanceService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CashBalanceServiceImpl implements CashBalanceService {

    private final CashBalanceRepository cashBalanceRepository;
    private final AccountRepository accountRepository;
    private static final Logger log = LoggerFactory.getLogger(CashBalanceServiceImpl.class);

    private static final String ERR_ACCOUNT_NOT_FOUND = "Account not found: ";
    private static final String ERR_CASHBALANCE_NOT_FOUND_FOR_ACCOUNT = "CashBalance not found for account: ";

    private Account getAccountOrThrow(Integer accountId) {
        return accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException(ERR_ACCOUNT_NOT_FOUND + accountId));
    }

    private CashBalance getBalanceOrThrow(Account account) {
        Integer id = account.getAccountId();
        return cashBalanceRepository.findByAccount(account)
                .orElseThrow(() -> new NotFoundException(ERR_CASHBALANCE_NOT_FOUND_FOR_ACCOUNT + id));
    }
    @Override
    public CashBalanceResponse getBalanceByAccountId(Integer accountId) {
        Account account = getAccountOrThrow(accountId);
        CashBalance balance = getBalanceOrThrow(account);

        return CashBalanceResponse.builder()
                .balanceId(balance.getBalanceId())
                .freeBalance(balance.getFreeBalance())
                .blockedBalance(balance.getBlockedBalance())
                .totalBalance(balance.getFreeBalance().add(balance.getBlockedBalance()))
                .build();
    }

    @Override
    @Transactional
    public void transfer(Integer fromAccountId, Integer toAccountId, BigDecimal amount) {
        Account fromAcc = accountRepository.findById(fromAccountId)
                .orElseThrow(() -> new NotFoundException("From account not found: " + fromAccountId));
        Account toAcc = accountRepository.findById(toAccountId)
                .orElseThrow(() -> new NotFoundException("To account not found: " + toAccountId));

        CashBalance fromBal = cashBalanceRepository.findByAccount(fromAcc)
                .orElseThrow(() -> new NotFoundException("CashBalance not found for account: " + fromAccountId));
        CashBalance toBal = cashBalanceRepository.findByAccount(toAcc)
                .orElseThrow(() -> new NotFoundException("CashBalance not found for account: " + toAccountId));

        if (fromBal.getFreeBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient cash balance: " + fromAccountId);
        }
        fromBal.setFreeBalance(fromBal.getFreeBalance().subtract(amount));
        toBal.setFreeBalance(toBal.getFreeBalance().add(amount));

        cashBalanceRepository.save(fromBal);
        cashBalanceRepository.save(toBal);
    }

    @Override
    @Transactional
    public void reduceBlockedBalance(Integer accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        CashBalance balance = cashBalanceRepository.findByAccount(account)
                .orElseThrow(() -> new NotFoundException("CashBalance not found for account: " + accountId));

        if (balance.getBlockedBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Insufficient blocked balance: " + accountId);
        }

        balance.setBlockedBalance(balance.getBlockedBalance().subtract(amount));
        cashBalanceRepository.save(balance);
    }

    @Override
    @Transactional
    public void addFreeBalance(Integer accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        CashBalance balance = cashBalanceRepository.findByAccount(account)
                .orElseThrow(() -> new NotFoundException("CashBalance not found for account: " + accountId));

        balance.setFreeBalance(balance.getFreeBalance().add(amount));
        cashBalanceRepository.save(balance);
    }

    @Override
    @Transactional
    public void moveBlockedToFree(Integer accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        CashBalance balance = cashBalanceRepository.findByAccount(account)
                .orElseThrow(() -> new NotFoundException("CashBalance not found for account: " + accountId));

        log.debug("moveBlockedToFree: accountId={}, requestedAmount={}, currentBlocked={}, currentFree={}", 
                 accountId, amount, balance.getBlockedBalance(), balance.getFreeBalance());

        if (balance.getBlockedBalance().compareTo(amount) < 0) {
            log.error("Insufficient blocked balance: accountId={}, requestedAmount={}, availableBlocked={}", 
                     accountId, amount, balance.getBlockedBalance());
            throw new IllegalArgumentException("Insufficient blocked balance: requested=" + amount + 
                                             ", available=" + balance.getBlockedBalance() + 
                                             ", accountId=" + accountId);
        }

        balance.setBlockedBalance(balance.getBlockedBalance().subtract(amount));
        balance.setFreeBalance(balance.getFreeBalance().add(amount));
        cashBalanceRepository.save(balance);
        
        log.debug("moveBlockedToFree completed: accountId={}, newBlocked={}, newFree={}", 
                 accountId, balance.getBlockedBalance(), balance.getFreeBalance());
    }

    @Override
    @Transactional
    public void blockBalance(Integer accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        CashBalance balance = cashBalanceRepository.findByAccount(account)
                .orElseThrow(() -> new NotFoundException("CashBalance not found for account: " + accountId));

        log.debug("blockBalance: accountId={}, requestedAmount={}, currentFree={}, currentBlocked={}", 
                 accountId, amount, balance.getFreeBalance(), balance.getBlockedBalance());

        if (balance.getFreeBalance().compareTo(amount) < 0) {
            log.error("Insufficient free balance to block: accountId={}, requestedAmount={}, availableFree={}", 
                     accountId, amount, balance.getFreeBalance());
            throw new IllegalArgumentException("Insufficient free balance: requested=" + amount + 
                                             ", available=" + balance.getFreeBalance() + 
                                             ", accountId=" + accountId);
        }

        balance.setFreeBalance(balance.getFreeBalance().subtract(amount));
        balance.setBlockedBalance(balance.getBlockedBalance().add(amount));
        cashBalanceRepository.save(balance);
        
        log.debug("blockBalance completed: accountId={}, newFree={}, newBlocked={}", 
                 accountId, balance.getFreeBalance(), balance.getBlockedBalance());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasEnoughBalance(Integer accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        CashBalance balance = cashBalanceRepository.findByAccount(account)
                .orElseThrow(() -> new NotFoundException("CashBalance not found for account: " + accountId));

        return balance.getFreeBalance().compareTo(amount) >= 0;
    }

    @Override
    @Transactional
    public void unblockBalance(Integer accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));

        CashBalance balance = cashBalanceRepository.findByAccount(account)
                .orElseThrow(() -> new NotFoundException("CashBalance not found for account: " + accountId));

        log.debug("unblockBalance: accountId={}, requestedAmount={}, currentBlocked={}, currentFree={}", 
                 accountId, amount, balance.getBlockedBalance(), balance.getFreeBalance());

        if (balance.getBlockedBalance().compareTo(amount) < 0) {
            log.error("Insufficient blocked balance to unblock: accountId={}, requestedAmount={}, availableBlocked={}", 
                     accountId, amount, balance.getBlockedBalance());
            throw new IllegalArgumentException("Insufficient blocked balance: requested=" + amount + 
                                             ", available=" + balance.getBlockedBalance() + 
                                             ", accountId=" + accountId);
        }

        balance.setBlockedBalance(balance.getBlockedBalance().subtract(amount));
        balance.setFreeBalance(balance.getFreeBalance().add(amount));
        cashBalanceRepository.save(balance);
        
        log.debug("unblockBalance completed: accountId={}, newBlocked={}, newFree={}", 
                 accountId, balance.getBlockedBalance(), balance.getFreeBalance());
    }
}
