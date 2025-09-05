package com.fintra.stocktrading.mapper;

import com.fintra.stocktrading.model.dto.response.AccountResponse;
import com.fintra.stocktrading.model.dto.response.CashBalanceResponse;
import com.fintra.stocktrading.model.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {

    public AccountResponse toAccountResponse(Account account) {
        if (account == null) {
            return null;
        }

        CashBalanceResponse cashBalanceResponse = null;
        if (account.getCashBalance() != null) {
            cashBalanceResponse = CashBalanceResponse.builder()
                    .balanceId(account.getCashBalance().getBalanceId())
                    .freeBalance(account.getCashBalance().getFreeBalance())
                    .blockedBalance(account.getCashBalance().getBlockedBalance())
                    .totalBalance(account.getCashBalance().getFreeBalance().add(account.getCashBalance().getBlockedBalance()))
                    .build();
        }

        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .accountType(account.getAccountType())
                .cashBalance(cashBalanceResponse)
                .build();
    }
}
