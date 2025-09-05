package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.CustomerControllerDoc;
import com.fintra.stocktrading.model.dto.request.CustomerCreateRequest;
import com.fintra.stocktrading.model.dto.request.CustomerPatchRequest;
import com.fintra.stocktrading.model.dto.request.CustomerSearchRequest;
import com.fintra.stocktrading.model.dto.request.CustomerUpdateRequest;
import com.fintra.stocktrading.model.dto.response.CustomerResponse;
import com.fintra.stocktrading.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Slf4j
public class CustomerController implements CustomerControllerDoc {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ANALYST')")
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @Valid @ModelAttribute CustomerSearchRequest request) {

        log.info("ADMIN requesting all customers with request: {}", request);

        Page<CustomerResponse> customers = customerService.getAllCustomers(request);

        log.info("Retrieved {} customers out of {} total", customers.getNumberOfElements(), customers.getTotalElements());
        return ResponseEntity.ok(customers);
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasRole('TRADER')")
    public ResponseEntity<Page<CustomerResponse>> getAssignedCustomers(
            @Valid @ModelAttribute CustomerSearchRequest request) {

        log.info("TRADER requesting assigned customers with request: {}", request);

        Page<CustomerResponse> customers = customerService.getAssignedCustomers(request);

        log.info("Retrieved {} assigned customers out of {} total", customers.getNumberOfElements(), customers.getTotalElements());
        return ResponseEntity.ok(customers);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        log.info("ADMIN creating customer with email: {}", request.getEmail());

        CustomerResponse createdCustomer = customerService.createCustomer(request);

        log.info("Customer created successfully with ID: {}", createdCustomer.getCustomerId());
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCustomer);
    }

    @PutMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Integer customerId,
            @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerResponse response = customerService.updateCustomer(customerId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{customerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CustomerResponse> patchCustomer(
            @PathVariable Integer customerId,
            @Valid @RequestBody CustomerPatchRequest request) {
        CustomerResponse response = customerService.patchCustomer(customerId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRADER')")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Integer customerId) {
        log.info("Requesting customer details for ID: {}", customerId);

        CustomerResponse customer = customerService.getCustomerById(customerId);

        log.info("Customer retrieved successfully with ID: {}", customer.getCustomerId());
        return ResponseEntity.ok(customer);
    }
}
