package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.AccountControllerDoc;
import com.fintra.stocktrading.model.dto.request.AccountCreateRequest;
import com.fintra.stocktrading.model.dto.request.AccountPatchRequest;
import com.fintra.stocktrading.model.dto.request.AccountUpdateRequest;
import com.fintra.stocktrading.model.dto.response.AccountResponse;
import com.fintra.stocktrading.model.entity.User;
import com.fintra.stocktrading.repository.CustomerRepository;
import com.fintra.stocktrading.service.AccountService;
import com.fintra.stocktrading.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
@Slf4j
public class AccountController implements AccountControllerDoc {

    private final AccountService accountService;
    private final UserService userService;
    private final CustomerRepository customerRepository;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody AccountCreateRequest request) {
        log.info("Received request to create a new account for customer ID: {}", request.getCustomerId());
        AccountResponse createdAccount = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdAccount);
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    public ResponseEntity<List<AccountResponse>> getAccountsByCustomerId(@PathVariable Integer customerId) {
        log.info("Received request to get all accounts for customer ID: {}", customerId);
        
        User currentUser = userService.getCurrentUser();
        List<AccountResponse> accounts = accountService.getAccountsByCustomerIdWithSecurity(customerId, currentUser);
        
        return ResponseEntity.ok(accounts);
    }
    
    @PutMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> updateAccount(
            @PathVariable Integer accountId,
            @Valid @RequestBody AccountUpdateRequest request) {
        log.info("Received request to update account with ID: {}", accountId);
        AccountResponse updatedAccount = accountService.updateAccount(accountId, request);
        return ResponseEntity.ok(updatedAccount);
    }
    
    @PatchMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AccountResponse> patchAccount(
            @PathVariable Integer accountId,
            @Valid @RequestBody AccountPatchRequest request) {
        log.info("Received request to partially update account with ID: {}", accountId);
        AccountResponse updatedAccount = accountService.patchAccount(accountId, request);
        return ResponseEntity.ok(updatedAccount);
    }
    
    @DeleteMapping("/{accountId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccount(@PathVariable Integer accountId) {
        log.info("Received request to delete account with ID: {}", accountId);
        accountService.deleteAccount(accountId);
        return ResponseEntity.noContent().build();
    }
}
