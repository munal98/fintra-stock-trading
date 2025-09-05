package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.ResourceNotFoundException;
import com.fintra.stocktrading.mapper.AccountMapper;
import com.fintra.stocktrading.model.dto.request.AccountCreateRequest;
import com.fintra.stocktrading.model.dto.request.AccountPatchRequest;
import com.fintra.stocktrading.model.dto.request.AccountUpdateRequest;
import com.fintra.stocktrading.model.dto.response.AccountResponse;
import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.CashBalance;
import com.fintra.stocktrading.model.entity.Customer;
import com.fintra.stocktrading.model.entity.User;
import com.fintra.stocktrading.model.enums.Role;
import com.fintra.stocktrading.repository.AccountRepository;
import com.fintra.stocktrading.repository.CashBalanceRepository;
import com.fintra.stocktrading.repository.CustomerRepository;
import com.fintra.stocktrading.service.AccountService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final CashBalanceRepository cashBalanceRepository;
    private final AccountMapper accountMapper;

    @Override
    @Transactional
    public AccountResponse createAccount(AccountCreateRequest request) {
        log.info("Creating new account for customer with ID: {}", request.getCustomerId());

        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + request.getCustomerId()));

        Account account = Account.builder()
                .customer(customer)
                .accountType(request.getAccountType())
                .build();
        Account savedAccount = accountRepository.save(account);

        CashBalance cashBalance = CashBalance.builder()
                .account(savedAccount)
                .freeBalance(BigDecimal.ZERO)
                .blockedBalance(BigDecimal.ZERO)
                .build();
        cashBalanceRepository.save(cashBalance);

        entityManager.refresh(savedAccount);

        log.info("Account created successfully with ID: {}", savedAccount.getAccountId());
        return accountMapper.toAccountResponse(savedAccount);
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerId(Integer customerId) {
        log.info("Fetching all accounts for customer with ID: {}", customerId);

        if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }

        List<Account> accounts = accountRepository.findByCustomer_CustomerId(customerId);

        log.info("Found {} accounts for customer with ID: {}", accounts.size(), customerId);
        return accounts.stream()
                .map(accountMapper::toAccountResponse)
                .collect(Collectors.toList());
    }
    
    @Override
    public AccountResponse getAccountById(Integer accountId) {
        log.info("Fetching account with ID: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));
        
        log.info("Found account with ID: {}", accountId);
        return accountMapper.toAccountResponse(account);
    }
    
    @Override
    @Transactional
    public AccountResponse updateAccount(Integer accountId, AccountUpdateRequest request) {
        log.info("Updating account with ID: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        account.setAccountType(request.getAccountType());
        
        Account updatedAccount = accountRepository.save(account);
        log.info("Account updated successfully with ID: {}", updatedAccount.getAccountId());
        
        return accountMapper.toAccountResponse(updatedAccount);
    }
    
    @Override
    @Transactional
    public void deleteAccount(Integer accountId) {
        log.info("Deleting account with ID: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        cashBalanceRepository.deleteByAccount_AccountId(accountId);

        accountRepository.delete(account);
        
        log.info("Account deleted successfully with ID: {}", accountId);
    }

    @Override
    @Transactional
    public AccountResponse patchAccount(Integer accountId, AccountPatchRequest request) {
        log.info("Partially updating account with ID: {}", accountId);
        
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountId));

        if (request.getAccountType() != null) {
            account.setAccountType(request.getAccountType());
            log.info("Updated account type to: {}", request.getAccountType());
        }
        
        Account updatedAccount = accountRepository.save(account);
        log.info("Account partially updated successfully with ID: {}", updatedAccount.getAccountId());
        
        return accountMapper.toAccountResponse(updatedAccount);
    }

    @Override
    public List<AccountResponse> getAccountsByCustomerIdWithSecurity(Integer customerId, User currentUser) {
        log.info("Fetching all accounts for customer ID: {} with security check", customerId);

        if (currentUser.getRole() == Role.ROLE_TRADER) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

            if (!customer.getUser().getId().equals(currentUser.getId())) {
                log.warn("Access denied: TRADER {} attempted to access accounts of customer {} not assigned to them",
                        currentUser.getId(), customerId);
                throw new AccessDeniedException("You can only access accounts of customers assigned to you");
            }
        } else if (!customerRepository.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
        }

        List<Account> accounts = accountRepository.findByCustomer_CustomerId(customerId);

        log.info("Found {} accounts for customer with ID: {}", accounts.size(), customerId);
        return accounts.stream()
                .map(accountMapper::toAccountResponse)
                .collect(Collectors.toList());
    }
}
