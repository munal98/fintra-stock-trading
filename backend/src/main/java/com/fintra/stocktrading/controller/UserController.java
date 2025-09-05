package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.UserControllerDoc;
import com.fintra.stocktrading.model.dto.request.CustomerAssignmentUpdateRequest;
import com.fintra.stocktrading.model.dto.request.CustomerIdsRequest;
import com.fintra.stocktrading.model.dto.request.UserCreateRequest;
import com.fintra.stocktrading.model.dto.request.UserPatchRequest;
import com.fintra.stocktrading.model.dto.request.UserSearchRequest;
import com.fintra.stocktrading.model.dto.request.UserUpdateRequest;
import com.fintra.stocktrading.model.dto.response.UserResponse;
import com.fintra.stocktrading.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController implements UserControllerDoc {

    private final UserService userService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(@ParameterObject @Valid UserSearchRequest request) {
        log.info("GET /api/users - Retrieving users with pagination and filtering");
        Page<UserResponse> users = userService.getAllUsers(request);
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest request) {
        log.info("POST /api/users - Creating new user with email: {}", request.getEmail());
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer userId) {
        log.info("GET /api/users/{} - Retrieving user by ID", userId);
        UserResponse response = userService.getUserById(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable Integer userId,
            @Valid @RequestBody UserUpdateRequest request) {
        log.info("PUT /api/users/{} - Updating user", userId);
        UserResponse response = userService.updateUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> patchUser(
            @PathVariable Integer userId,
            @Valid @RequestBody UserPatchRequest request) {
        log.info("PATCH /api/users/{} - Partially updating user", userId);
        UserResponse response = userService.patchUser(userId, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{traderId}/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> addCustomersToTrader(
            @PathVariable Integer traderId,
            @Valid @RequestBody CustomerIdsRequest request) {
        log.info("POST /api/users/{}/customers - Adding customers to trader incrementally", traderId);
        UserResponse response = userService.addCustomersToTrader(traderId, request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{traderId}/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> replaceAllCustomers(
            @PathVariable Integer traderId,
            @Valid @RequestBody CustomerIdsRequest request) {
        log.info("PUT /api/users/{}/customers - Replacing all customer assignments for trader", traderId);
        UserResponse response = userService.replaceAllCustomers(traderId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{traderId}/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> updateCustomerAssignments(
            @PathVariable Integer traderId,
            @Valid @RequestBody CustomerAssignmentUpdateRequest request) {
        log.info("PATCH /api/users/{}/customers - Updating customer assignments for trader", traderId);
        UserResponse response = userService.updateCustomerAssignments(traderId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{traderId}/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> removeCustomersFromTrader(
            @PathVariable Integer traderId,
            @Valid @RequestBody CustomerIdsRequest request) {
        log.info("DELETE /api/users/{}/customers - Removing specific customers from trader", traderId);
        UserResponse response = userService.removeCustomersFromTrader(traderId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{traderId}/customers/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> removeAllCustomersFromTrader(@PathVariable Integer traderId) {
        log.info("DELETE /api/users/{}/customers/all - Removing all customers from trader", traderId);
        UserResponse response = userService.removeAllCustomersFromTrader(traderId);
        return ResponseEntity.ok(response);
    }
}
