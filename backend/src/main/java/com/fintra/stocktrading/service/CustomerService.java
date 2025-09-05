package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.request.CustomerCreateRequest;
import com.fintra.stocktrading.model.dto.request.CustomerSearchRequest;
import com.fintra.stocktrading.model.dto.request.CustomerUpdateRequest;
import com.fintra.stocktrading.model.dto.request.CustomerPatchRequest;
import com.fintra.stocktrading.model.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;

public interface CustomerService {

    /**
     * Get all customers with pagination and filtering (ROLE_ADMIN only)
     *
     * @param request Search and pagination request
     * @return Page of customers matching the criteria
     */
    Page<CustomerResponse> getAllCustomers(CustomerSearchRequest request);

    /**
     * Get assigned customers with pagination and filtering (ROLE_TRADER)
     * Returns only customers assigned to the current authenticated trader
     * Note: tradingEnabled parameter is ignored - only tradingEnabled=true customers are returned
     *
     * @param request Search and pagination request
     * @return Page of assigned customers matching the criteria
     */
    Page<CustomerResponse> getAssignedCustomers(CustomerSearchRequest request);

    /**
     * Create a new customer with account (ROLE_ADMIN only)
     * Also creates an Account with the specified accountType and initializes CashBalance
     *
     * @param request Customer creation request
     * @return Created customer information
     */
    CustomerResponse createCustomer(CustomerCreateRequest request);

    /**
     * Update customer information (ROLE_ADMIN only)
     * Updates customer's basic information including firstName, lastName, email, tradingPermission, and tradingEnabled
     *
     * @param customerId Customer ID to update
     * @param request Customer update request
     * @return Updated customer information
     */
    CustomerResponse updateCustomer(Integer customerId, CustomerUpdateRequest request);

    /**
     * Partially update customer information (ROLE_ADMIN only)
     * Updates only the provided fields, leaving others unchanged
     *
     * @param customerId Customer ID to update
     * @param request Customer patch request with optional fields
     * @return Updated customer information
     */
    CustomerResponse patchCustomer(Integer customerId, CustomerPatchRequest request);

    /**
     * Get customer by ID
     *
     * @param customerId Customer ID
     * @return Customer information
     */
    CustomerResponse getCustomerById(Integer customerId);
}
