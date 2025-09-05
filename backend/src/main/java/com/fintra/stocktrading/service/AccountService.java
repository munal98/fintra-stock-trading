package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.request.AccountCreateRequest;
import com.fintra.stocktrading.model.dto.request.AccountPatchRequest;
import com.fintra.stocktrading.model.dto.request.AccountUpdateRequest;
import com.fintra.stocktrading.model.dto.response.AccountResponse;
import com.fintra.stocktrading.model.entity.User;

import java.util.List;

public interface AccountService {

    /**
     * Creates a new account for an existing customer.
     *
     * @param request The account creation request containing customer ID and account type
     * @return The created account response
     */
    AccountResponse createAccount(AccountCreateRequest request);

    /**
     * Retrieves all accounts for a specific customer.
     *
     * @param customerId The ID of the customer
     * @return List of accounts belonging to the customer
     */
    List<AccountResponse> getAccountsByCustomerId(Integer customerId);
    
    /**
     * Updates an existing account.
     *
     * @param accountId The ID of the account to update
     * @param request The account update request containing the new account type
     * @return The updated account response
     */
    AccountResponse updateAccount(Integer accountId, AccountUpdateRequest request);
    
    /**
     * Partially updates an existing account.
     * Updates only the provided fields, leaving others unchanged.
     *
     * @param accountId The ID of the account to update
     * @param request The account patch request with optional fields
     * @return The updated account response
     */
    AccountResponse patchAccount(Integer accountId, AccountPatchRequest request);
    
    /**
     * Deletes an account by its ID.
     *
     * @param accountId The ID of the account to delete
     */
    void deleteAccount(Integer accountId);
    
    /**
     * Retrieves an account by its ID.
     *
     * @param accountId The ID of the account to retrieve
     * @return The account response
     */
    AccountResponse getAccountById(Integer accountId);

    /**
     * Retrieves all accounts for a specific customer with security check.
     * For TRADER role, checks if the customer is assigned to the current user.
     *
     * @param customerId The ID of the customer
     * @param currentUser The currently authenticated user
     * @return List of accounts belonging to the customer
     */
    List<AccountResponse> getAccountsByCustomerIdWithSecurity(Integer customerId, User currentUser);
}
