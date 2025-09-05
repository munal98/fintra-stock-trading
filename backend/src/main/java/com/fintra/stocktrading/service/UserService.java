package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.request.CustomerAssignmentUpdateRequest;
import com.fintra.stocktrading.model.dto.request.CustomerIdsRequest;
import com.fintra.stocktrading.model.dto.request.UserCreateRequest;
import com.fintra.stocktrading.model.dto.request.UserPatchRequest;
import com.fintra.stocktrading.model.dto.request.UserSearchRequest;
import com.fintra.stocktrading.model.dto.request.UserUpdateRequest;
import com.fintra.stocktrading.model.dto.response.UserResponse;
import com.fintra.stocktrading.model.entity.User;
import org.springframework.data.domain.Page;

public interface UserService {

    /**
     * Gets the currently authenticated user
     *
     * @return The current user entity
     */
    User getCurrentUser();

    /**
     * Get all users with pagination and filtering (ROLE_ADMIN only)
     *
     * @param request Search and pagination request
     * @return Page of users matching the criteria
     */
    Page<UserResponse> getAllUsers(UserSearchRequest request);

    /**
     * Create a new user (ROLE_ADMIN only)
     *
     * @param request User creation request
     * @return Created user information
     */
    UserResponse createUser(UserCreateRequest request);

    /**
     * Update user information (ROLE_ADMIN only)
     * Updates all fields with the provided values
     *
     * @param userId User ID to update
     * @param request User update request with all required fields
     * @return Updated user information
     */
    UserResponse updateUser(Integer userId, UserUpdateRequest request);

    /**
     * Partially update user information (ROLE_ADMIN only)
     * Updates only the provided fields, leaving others unchanged
     *
     * @param userId User ID to update
     * @param request User patch request with optional fields
     * @return Updated user information
     */
    UserResponse patchUser(Integer userId, UserPatchRequest request);

    /**
     * Get user by ID (ROLE_ADMIN only)
     *
     * @param userId User ID
     * @return User information
     */
    UserResponse getUserById(Integer userId);

    /**
     * Add customers to a trader user incrementally (ROLE_ADMIN only)
     * Adds new customers to existing assignments without removing current ones
     *
     * @param traderId Trader user ID
     * @param request Customer IDs to add
     * @return Updated trader user information
     */
    UserResponse addCustomersToTrader(Integer traderId, CustomerIdsRequest request);

    /**
     * Replace all customer assignments for a trader user (ROLE_ADMIN only)
     * Removes all existing assignments and sets new ones
     *
     * @param traderId Trader user ID
     * @param request Customer IDs to assign
     * @return Updated trader user information
     */
    UserResponse replaceAllCustomers(Integer traderId, CustomerIdsRequest request);

    /**
     * Update customer assignments for a trader user (ROLE_ADMIN only)
     * Adds and/or removes specific customers from the trader
     *
     * @param traderId Trader user ID
     * @param request Customer assignment update request
     * @return Updated trader user information with modified assignments
     */
    UserResponse updateCustomerAssignments(Integer traderId, CustomerAssignmentUpdateRequest request);

    /**
     * Remove specific customers from a trader user (ROLE_ADMIN only)
     *
     * @param traderId Trader user ID
     * @param request Customer IDs to remove
     * @return Updated trader user information
     */
    UserResponse removeCustomersFromTrader(Integer traderId, CustomerIdsRequest request);

    /**
     * Remove all customers from a trader user (ROLE_ADMIN only)
     *
     * @param traderId Trader user ID
     * @return Updated trader user information
     */
    UserResponse removeAllCustomersFromTrader(Integer traderId);
}