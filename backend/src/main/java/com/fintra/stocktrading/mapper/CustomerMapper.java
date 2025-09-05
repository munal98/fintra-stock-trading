package com.fintra.stocktrading.mapper;

import com.fintra.stocktrading.model.dto.request.CustomerCreateRequest;
import com.fintra.stocktrading.model.dto.response.AccountResponse;
import com.fintra.stocktrading.model.dto.response.CashBalanceResponse;
import com.fintra.stocktrading.model.dto.response.CustomerResponse;
import com.fintra.stocktrading.model.dto.response.EquityHoldingResponse;
import com.fintra.stocktrading.model.entity.Customer;
import com.fintra.stocktrading.model.entity.EquityStock;
import com.fintra.stocktrading.model.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CustomerMapper {

    public Customer toCustomer(CustomerCreateRequest request, User user) {
        if (request == null || user == null) {
            return null;
        }

        return Customer.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .identityNumber(request.getIdentityNumber())
                .tradingPermission(request.getTradingPermission())
                .tradingEnabled(true)
                .build();
    }

    public CustomerResponse toCustomerResponse(Customer customer) {
        if (customer == null) {
            return null;
        }

        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .identityNumber(customer.getIdentityNumber())
                .tradingPermission(customer.getTradingPermission())
                .tradingEnabled(customer.getTradingEnabled())
                .accounts(customer.getAccounts() != null ? toAccountResponses(customer.getAccounts()) : new ArrayList<>())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    public CustomerResponse toCustomerResponseWithEquities(Customer customer, Map<Integer, List<EquityHoldingResponse>> equityHoldingsByAccount) {
        if (customer == null) {
            return null;
        }

        return CustomerResponse.builder()
                .customerId(customer.getCustomerId())
                .firstName(customer.getFirstName())
                .lastName(customer.getLastName())
                .email(customer.getEmail())
                .identityNumber(customer.getIdentityNumber())
                .tradingPermission(customer.getTradingPermission())
                .tradingEnabled(customer.getTradingEnabled())
                .accounts(customer.getAccounts() != null ? toAccountResponsesWithEquities(customer.getAccounts(), equityHoldingsByAccount) : new ArrayList<>())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }

    private List<AccountResponse> toAccountResponses(List<com.fintra.stocktrading.model.entity.Account> accounts) {
        if (accounts == null || accounts.isEmpty()) {
            return new ArrayList<>();
        }

        return accounts.stream()
                .map(account -> toAccountResponse(account, new ArrayList<>()))
                .collect(Collectors.toList());
    }

    private List<AccountResponse> toAccountResponsesWithEquities(List<com.fintra.stocktrading.model.entity.Account> accounts, Map<Integer, List<EquityHoldingResponse>> equityHoldingsByAccount) {
        if (accounts == null || accounts.isEmpty()) {
            return new ArrayList<>();
        }

        return accounts.stream()
                .map(account -> toAccountResponse(account, equityHoldingsByAccount.getOrDefault(account.getAccountId(), new ArrayList<>())))
                .collect(Collectors.toList());
    }

    private AccountResponse toAccountResponse(com.fintra.stocktrading.model.entity.Account account, List<EquityHoldingResponse> equityHoldings) {
        if (account == null) {
            return null;
        }

        return AccountResponse.builder()
                .accountId(account.getAccountId())
                .accountType(account.getAccountType())
                .cashBalance(toCashBalanceResponse(account.getCashBalance()))
                .equities(equityHoldings != null ? equityHoldings : new ArrayList<>())
                .build();
    }

    private CashBalanceResponse toCashBalanceResponse(com.fintra.stocktrading.model.entity.CashBalance cashBalance) {
        if (cashBalance == null) {
            return null;
        }

        BigDecimal freeBalance = cashBalance.getFreeBalance() != null ? cashBalance.getFreeBalance() : BigDecimal.ZERO;
        BigDecimal blockedBalance = cashBalance.getBlockedBalance() != null ? cashBalance.getBlockedBalance() : BigDecimal.ZERO;
        BigDecimal totalBalance = freeBalance.add(blockedBalance);

        return CashBalanceResponse.builder()
                .balanceId(cashBalance.getBalanceId())
                .freeBalance(freeBalance)
                .blockedBalance(blockedBalance)
                .totalBalance(totalBalance)
                .build();
    }

    public EquityHoldingResponse toEquityHoldingResponse(EquityStock stock) {
        if (stock == null) {
            return null;
        }

        return EquityHoldingResponse.builder()
                .equityId(stock.getEquity().getEquityId())
                .assetCode(stock.getEquity().getEquityCode())
                .assetName(stock.getEquity().getEquityName())
                .totalQuantity(stock.getFreeQuantity() + stock.getBlockedQuantity())
                .averageCost(stock.getAvgCost() != null ? stock.getAvgCost() : BigDecimal.ZERO)
                .build();
    }

    public void enrichEquityHoldingWithDerivedFields(EquityHoldingResponse holding) {
        if (holding == null) {
            return;
        }

        BigDecimal closePrice = holding.getClosePrice();
        BigDecimal averageCost = holding.getAverageCost();

        if (closePrice != null && averageCost != null && averageCost.compareTo(BigDecimal.ZERO) > 0) {
            try {
                BigDecimal profitLossPercentage = closePrice.subtract(averageCost)
                        .divide(averageCost, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP);
                holding.setProfitLossPercentage(profitLossPercentage);
            } catch (ArithmeticException e) {
                holding.setProfitLossPercentage(BigDecimal.ZERO);
            }
        } else {
            holding.setProfitLossPercentage(BigDecimal.ZERO);
        }
    }

    public EquityHoldingResponse toEquityHoldingResponseWithPrice(EquityStock stock, BigDecimal currentPrice) {
        EquityHoldingResponse holding = toEquityHoldingResponse(stock);
        
        if (holding != null) {
            holding.setClosePrice(currentPrice);
            enrichEquityHoldingWithDerivedFields(holding);
        }
        
        return holding;
    }
}
