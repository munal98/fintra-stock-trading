package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.ConflictException;
import com.fintra.stocktrading.exception.ResourceNotFoundException;
import com.fintra.stocktrading.mapper.CustomerMapper;
import com.fintra.stocktrading.model.dto.request.CustomerCreateRequest;
import com.fintra.stocktrading.model.dto.request.CustomerPatchRequest;
import com.fintra.stocktrading.model.dto.request.CustomerSearchRequest;
import com.fintra.stocktrading.model.dto.request.CustomerUpdateRequest;
import com.fintra.stocktrading.model.dto.response.CustomerResponse;
import com.fintra.stocktrading.model.dto.response.EquityHoldingResponse;
import com.fintra.stocktrading.model.dto.response.EquityPriceResponse;
import com.fintra.stocktrading.model.entity.Account;
import com.fintra.stocktrading.model.entity.CashBalance;
import com.fintra.stocktrading.model.entity.Customer;
import com.fintra.stocktrading.model.entity.EquityStock;
import com.fintra.stocktrading.model.entity.User;
import com.fintra.stocktrading.repository.AccountRepository;
import com.fintra.stocktrading.repository.CashBalanceRepository;
import com.fintra.stocktrading.repository.CustomerRepository;
import com.fintra.stocktrading.repository.EquityStockRepository;
import com.fintra.stocktrading.repository.UserRepository;
import com.fintra.stocktrading.service.CustomerService;
import com.fintra.stocktrading.service.EquityService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerServiceImpl implements CustomerService {

    @PersistenceContext
    private EntityManager entityManager;

    private final CustomerRepository customerRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final CashBalanceRepository cashBalanceRepository;
    private final EquityStockRepository equityStockRepository;
    private final EquityService equityService;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAllCustomers(CustomerSearchRequest request) {
        log.info("ADMIN requesting all customers - page: {}, size: {}, search: '{}'",
                request.getPage(), request.getSize(), request.getSearch());

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("customerId").ascending());

        Page<Customer> customers = customerRepository.findAllCustomersWithFilters(
                request.getSearch(), pageable);

        log.info("Retrieved {} customers out of {} total", customers.getNumberOfElements(), customers.getTotalElements());

        List<Integer> allAccountIds = customers.getContent().stream()
                .flatMap(customer -> customer.getAccounts().stream())
                .map(account -> account.getAccountId())
                .collect(Collectors.toList());
        
        log.info("Fetching equity holdings for {} accounts across {} customers", allAccountIds.size(), customers.getNumberOfElements());
        Map<Integer, List<EquityHoldingResponse>> equityHoldingsByAccount = getEquityHoldingsForAccounts(allAccountIds);
        
        return customers.map(customer -> customerMapper.toCustomerResponseWithEquities(customer, equityHoldingsByAccount));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAssignedCustomers(CustomerSearchRequest request) {
        log.info("TRADER requesting assigned customers - page: {}, size: {}, search: '{}' (only tradingEnabled=true customers returned)",
                request.getPage(), request.getSize(), request.getSearch());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("customerId").ascending());

        Page<Customer> customers = customerRepository.findAssignedCustomersForTrader(
                currentUser.getId(), request.getSearch(), pageable);

        log.info("Retrieved {} assigned customers out of {} total", customers.getNumberOfElements(), customers.getTotalElements());

        List<Integer> allAccountIds = customers.getContent().stream()
                .flatMap(customer -> customer.getAccounts().stream())
                .map(account -> account.getAccountId())
                .collect(Collectors.toList());
        
        log.info("Fetching equity holdings for {} accounts across {} assigned customers", allAccountIds.size(), customers.getNumberOfElements());
        Map<Integer, List<EquityHoldingResponse>> equityHoldingsByAccount = getEquityHoldingsForAccounts(allAccountIds);
        
        return customers.map(customer -> customerMapper.toCustomerResponseWithEquities(customer, equityHoldingsByAccount));
    }

    @Override
    @Transactional
    public CustomerResponse createCustomer(CustomerCreateRequest request) {
        log.info("Creating customer with email: {}", request.getEmail());

        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        if (customerRepository.existsByIdentityNumber(request.getIdentityNumber())) {
            throw new ConflictException("Identity number already exists: " + request.getIdentityNumber());
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + request.getUserId()));

        Customer customer = customerMapper.toCustomer(request, user);
        Customer savedCustomer = customerRepository.save(customer);

        Account account = Account.builder()
                .customer(savedCustomer)
                .accountType(request.getAccountType())
                .build();
        Account savedAccount = accountRepository.save(account);

        CashBalance cashBalance = CashBalance.builder()
                .account(savedAccount)
                .freeBalance(BigDecimal.ZERO)
                .blockedBalance(BigDecimal.ZERO)
                .build();
        cashBalanceRepository.save(cashBalance);

        entityManager.refresh(savedCustomer);
        entityManager.refresh(savedAccount);
        entityManager.flush();

        Customer customerWithAccounts = customerRepository.findByIdWithAccountsAndCashBalance(savedCustomer.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found after creation"));

        log.info("Customer created with ID: {}", savedCustomer.getCustomerId());
        return customerMapper.toCustomerResponse(customerWithAccounts);
    }

    @Override
    @Transactional
    public CustomerResponse updateCustomer(Integer customerId, CustomerUpdateRequest request) {
        log.info("Updating customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        if (!customer.getEmail().equals(request.getEmail()) && customerRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        if (!customer.getIdentityNumber().equals(request.getIdentityNumber()) && customerRepository.existsByIdentityNumber(request.getIdentityNumber())) {
            throw new ConflictException("Identity number already exists: " + request.getIdentityNumber());
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setTradingPermission(request.getTradingPermission());
        customer.setTradingEnabled(request.getTradingEnabled());
        customer.setIdentityNumber(request.getIdentityNumber());

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer updated with ID: {}", updatedCustomer.getCustomerId());

        return customerMapper.toCustomerResponse(updatedCustomer);
    }

    @Override
    @Transactional
    public CustomerResponse patchCustomer(Integer customerId, CustomerPatchRequest request) {
        log.info("Partially updating customer with ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        if (request.getEmail() != null && !customer.getEmail().equals(request.getEmail()) && customerRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        if (request.getIdentityNumber() != null && !customer.getIdentityNumber().equals(request.getIdentityNumber()) && customerRepository.existsByIdentityNumber(request.getIdentityNumber())) {
            throw new ConflictException("Identity number already exists: " + request.getIdentityNumber());
        }

        if (request.getFirstName() != null) {
            customer.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            customer.setLastName(request.getLastName());
        }
        if (request.getEmail() != null) {
            customer.setEmail(request.getEmail());
        }
        if (request.getTradingPermission() != null) {
            customer.setTradingPermission(request.getTradingPermission());
        }
        if (request.getTradingEnabled() != null) {
            customer.setTradingEnabled(request.getTradingEnabled());
        }
        if (request.getIdentityNumber() != null) {
            customer.setIdentityNumber(request.getIdentityNumber());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        log.info("Customer partially updated with ID: {}", updatedCustomer.getCustomerId());
        return customerMapper.toCustomerResponse(updatedCustomer);
    }

    public Map<Integer, List<EquityHoldingResponse>> getEquityHoldingsForAccounts(List<Integer> accountIds) {
        log.debug("Fetching equity holdings for {} accounts: {}", accountIds.size(), accountIds);

        List<EquityStock> equityStocks = equityStockRepository.findEquityHoldingsByAccountIds(accountIds);
        log.info("Found {} equity stocks from repository for accounts: {}", equityStocks.size(), accountIds);

        if (equityStocks.isEmpty()) {
            log.warn("No equity holdings found for any of the accounts: {}. Total equity stocks in database: {}", accountIds, equityStockRepository.count());
            return accountIds.stream().collect(Collectors.toMap(id -> id, id -> new ArrayList<>()));
        }

        log.debug("Equity stocks found, grouping by account...");
        Map<Integer, List<EquityStock>> stocksByAccount = equityStocks.stream()
                .collect(Collectors.groupingBy(es -> es.getAccount().getAccountId()));

        log.debug("Stocks grouped by account: {}", stocksByAccount.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().size())));

        Map<Integer, List<EquityHoldingResponse>> result = new HashMap<>();
        for (Integer accountId : accountIds) {
            List<EquityStock> accountStocks = stocksByAccount.getOrDefault(accountId, new ArrayList<>());
            log.debug("Processing {} equity stocks for account {}", accountStocks.size(), accountId);
            result.put(accountId, enrichWithDatabasePricing(accountStocks));
        }

        return result;
    }

    private List<EquityHoldingResponse> enrichWithDatabasePricing(List<EquityStock> equityStocks) {
        List<EquityHoldingResponse> holdings = new ArrayList<>();

        for (EquityStock stock : equityStocks) {
            try {
                BigDecimal currentPrice = getCurrentClosePriceFromDatabase(stock.getEquity().getEquityCode());
                EquityHoldingResponse holding = customerMapper.toEquityHoldingResponseWithPrice(stock, currentPrice);
                holdings.add(holding);

            } catch (Exception e) {
                log.error("Error processing equity holding for stock {}: {}", stock.getEquity().getEquityCode(), e.getMessage());

                EquityHoldingResponse holding = customerMapper.toEquityHoldingResponse(stock);
                holdings.add(holding);
            }
        }

        return holdings;
    }

    private BigDecimal getCurrentClosePriceFromDatabase(String assetCode) {
        try {
            log.debug("Fetching current price from database for asset: {}", assetCode);

            String fullAssetCode = assetCode.contains(".") ? assetCode : assetCode + ".E";
            EquityPriceResponse latestPrice = equityService.getLatestPriceByAssetCode(fullAssetCode);

            if (latestPrice != null && latestPrice.getClosePrice() != null) {
                log.debug("Found latest close price {} for asset {} from database", latestPrice.getClosePrice(), assetCode);
                return latestPrice.getClosePrice();
            }

            log.warn("No price data found in database for asset: {}", assetCode);
            return null;

        } catch (Exception e) {
            log.error("Error fetching price from database for asset {}: {}", assetCode, e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(Integer customerId) {
        log.info("Getting customer by ID: {}", customerId);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserEmail = authentication.getName();
        
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ResourceNotFoundException("Current user not found"));

        Customer customer = customerRepository.findByIdWithAccountsAndCashBalance(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

        if (authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_TRADER"))) {
            
            if (!customer.getUser().getId().equals(currentUser.getId())) {
                throw new ResourceNotFoundException("Customer not found with ID: " + customerId);
            }
        }

        if (customer.getAccounts() != null && !customer.getAccounts().isEmpty()) {
            List<Integer> accountIds = customer.getAccounts().stream()
                    .map(account -> account.getAccountId())
                    .collect(Collectors.toList());
            
            Map<Integer, List<EquityHoldingResponse>> equityHoldingsByAccount = 
                    getEquityHoldingsForAccounts(accountIds);
            
            return customerMapper.toCustomerResponseWithEquities(customer, equityHoldingsByAccount);
        }

        return customerMapper.toCustomerResponse(customer);
    }
}
