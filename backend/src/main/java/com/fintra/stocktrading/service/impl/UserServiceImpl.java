package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.ConflictException;
import com.fintra.stocktrading.exception.ResourceNotFoundException;
import com.fintra.stocktrading.mapper.UserMapper;
import com.fintra.stocktrading.model.dto.request.*;
import com.fintra.stocktrading.model.dto.response.UserResponse;
import com.fintra.stocktrading.model.entity.Customer;
import com.fintra.stocktrading.model.entity.User;
import com.fintra.stocktrading.model.enums.Role;
import com.fintra.stocktrading.repository.CustomerRepository;
import com.fintra.stocktrading.repository.UserRepository;
import com.fintra.stocktrading.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final CustomerRepository customerRepository;

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(UserSearchRequest request) {
        log.info("ADMIN requesting all users - page: {}, size: {}, search: '{}'",
                request.getPage(), request.getSize(), request.getSearch());

        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("id").ascending());

        Page<User> users = userRepository.findAllUsersWithFilters(
                request.getSearch(), pageable);

        log.info("Retrieved {} users out of {} total", users.getNumberOfElements(), users.getTotalElements());
        return users.map(userMapper::toUserResponseWithoutCustomers);
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Creating user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        User user = userMapper.toUser(request);
        User savedUser = userRepository.save(user);

        log.info("User created with ID: {}", savedUser.getId());
        return userMapper.toUserResponseWithoutCustomers(savedUser);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        log.info("Updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (!user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setRole(request.getRole());
        user.setEnabled(request.getEnabled());

        User updatedUser = userRepository.save(user);
        log.info("User updated with ID: {}", updatedUser.getId());

        return userMapper.toUserResponseWithoutCustomers(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse patchUser(Integer userId, UserPatchRequest request) {
        log.info("Partially updating user with ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        if (request.getEmail() != null && !user.getEmail().equals(request.getEmail()) && userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists: " + request.getEmail());
        }

        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }
        if (request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User updatedUser = userRepository.save(user);
        log.info("User partially updated with ID: {}", updatedUser.getId());
        return userMapper.toUserResponseWithoutCustomers(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Integer userId) {
        log.info("Getting user by ID: {}", userId);

        User user = userRepository.findByIdWithCustomers(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse addCustomersToTrader(Integer traderId, CustomerIdsRequest request) {
        log.info("Adding customers to trader with ID: {}, customers: {}", traderId, request.getCustomerIds());

        User trader = userRepository.findByIdWithCustomers(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader user not found with ID: " + traderId));

        if (trader.getRole() != Role.ROLE_TRADER) {
            throw new ConflictException("User with ID " + traderId + " is not a TRADER");
        }

        if (!trader.isEnabled()) {
            throw new ConflictException("Cannot assign customers to disabled trader with ID: " + traderId);
        }

        for (Integer customerId : request.getCustomerIds()) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

            if (customer.getUser() != null && customer.getUser().getId().equals(traderId)) {
                log.warn("Customer {} is already assigned to trader {}", customerId, traderId);
                continue;
            }

            if (customer.getUser() != null) {
                User previousTrader = customer.getUser();
                previousTrader.getCustomers().remove(customer);
                userRepository.save(previousTrader);
                log.info("Removed customer {} from previous trader {}", customerId, previousTrader.getId());
            }

            customer.setUser(trader);
            customerRepository.save(customer);
            trader.getCustomers().add(customer);
            log.info("Added customer {} to trader {}", customerId, traderId);
        }

        User updatedTrader = userRepository.save(trader);
        log.info("Successfully added customers to trader with ID: {}", traderId);
        return userMapper.toUserResponse(updatedTrader);
    }

    @Override
    @Transactional
    public UserResponse replaceAllCustomers(Integer traderId, CustomerIdsRequest request) {
        log.info("Replacing all customers for trader with ID: {}, new customers: {}", traderId, request.getCustomerIds());

        User trader = userRepository.findByIdWithCustomers(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader user not found with ID: " + traderId));

        if (trader.getRole() != Role.ROLE_TRADER) {
            throw new ConflictException("User with ID " + traderId + " is not a TRADER");
        }

        if (!trader.isEnabled()) {
            throw new ConflictException("Cannot assign customers to disabled trader with ID: " + traderId);
        }

        for (Customer existingCustomer : new ArrayList<>(trader.getCustomers())) {
            existingCustomer.setUser(null);
            customerRepository.save(existingCustomer);
        }
        trader.getCustomers().clear();

        for (Integer customerId : request.getCustomerIds()) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

            if (customer.getUser() != null && !customer.getUser().getId().equals(traderId)) {
                User previousTrader = customer.getUser();
                previousTrader.getCustomers().remove(customer);
                userRepository.save(previousTrader);
                log.info("Removed customer {} from previous trader {}", customerId, previousTrader.getId());
            }

            customer.setUser(trader);
            customerRepository.save(customer);
            trader.getCustomers().add(customer);
            log.info("Assigned customer {} to trader {}", customerId, traderId);
        }

        User updatedTrader = userRepository.save(trader);
        log.info("Successfully replaced all customers for trader with ID: {}", traderId);
        return userMapper.toUserResponse(updatedTrader);
    }

    @Override
    @Transactional
    public UserResponse updateCustomerAssignments(Integer traderId, CustomerAssignmentUpdateRequest request) {
        log.info("Updating customer assignments for trader with ID: {}, adding: {}, removing: {}", 
                traderId, request.getAddCustomers(), request.getRemoveCustomers());

        User trader = userRepository.findByIdWithCustomers(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader user not found with ID: " + traderId));

        if (trader.getRole() != Role.ROLE_TRADER) {
            throw new ConflictException("User with ID " + traderId + " is not a TRADER");
        }

        if (!trader.isEnabled()) {
            throw new ConflictException("Cannot assign customers to disabled trader with ID: " + traderId);
        }
        
        if (request.getRemoveCustomers() != null && !request.getRemoveCustomers().isEmpty()) {
            for (Integer customerId : request.getRemoveCustomers()) {
                Customer customer = customerRepository.findById(customerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

                if (customer.getUser() != null && customer.getUser().getId().equals(traderId)) {
                    customer.setUser(null);
                    customerRepository.save(customer);
                    trader.getCustomers().remove(customer);
                    log.info("Removed customer {} from trader {}", customerId, traderId);
                }
            }
        }
        
        if (request.getAddCustomers() != null && !request.getAddCustomers().isEmpty()) {
            for (Integer customerId : request.getAddCustomers()) {
                Customer customer = customerRepository.findById(customerId)
                        .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

                if (customer.getUser() != null && customer.getUser().getId().equals(traderId)) {
                    log.warn("Customer {} is already assigned to trader {}", customerId, traderId);
                    continue;
                }

                if (customer.getUser() != null) {
                    User previousTrader = customer.getUser();
                    previousTrader.getCustomers().remove(customer);
                    userRepository.save(previousTrader);
                    log.info("Removed customer {} from previous trader {}", customerId, previousTrader.getId());
                }

                customer.setUser(trader);
                customerRepository.save(customer);
                trader.getCustomers().add(customer);
                log.info("Added customer {} to trader {}", customerId, traderId);
            }
        }

        User updatedTrader = userRepository.save(trader);
        log.info("Successfully updated customer assignments for trader with ID: {}", traderId);
        return userMapper.toUserResponse(updatedTrader);
    }

    @Override
    @Transactional
    public UserResponse removeCustomersFromTrader(Integer traderId, CustomerIdsRequest request) {
        log.info("Removing customers from trader with ID: {}, customers: {}", traderId, request.getCustomerIds());

        User trader = userRepository.findByIdWithCustomers(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader user not found with ID: " + traderId));

        if (trader.getRole() != Role.ROLE_TRADER) {
            throw new ConflictException("User with ID " + traderId + " is not a TRADER");
        }

        for (Integer customerId : request.getCustomerIds()) {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found with ID: " + customerId));

            if (customer.getUser() != null && customer.getUser().getId().equals(traderId)) {
                customer.setUser(null);
                customerRepository.save(customer);
                trader.getCustomers().remove(customer);
                log.info("Removed customer {} from trader {}", customerId, traderId);
            } else {
                log.warn("Customer {} is not assigned to trader {}", customerId, traderId);
            }
        }

        User updatedTrader = userRepository.save(trader);
        log.info("Successfully removed customers from trader with ID: {}", traderId);
        return userMapper.toUserResponse(updatedTrader);
    }

    @Override
    @Transactional
    public UserResponse removeAllCustomersFromTrader(Integer traderId) {
        log.info("Removing all customers from trader with ID: {}", traderId);

        User trader = userRepository.findByIdWithCustomers(traderId)
                .orElseThrow(() -> new ResourceNotFoundException("Trader user not found with ID: " + traderId));

        if (trader.getRole() != Role.ROLE_TRADER) {
            throw new ConflictException("User with ID " + traderId + " is not a TRADER");
        }

        for (Customer customer : new ArrayList<>(trader.getCustomers())) {
            customer.setUser(null);
            customerRepository.save(customer);
            log.info("Removed customer {} from trader {}", customer.getCustomerId(), traderId);
        }
        trader.getCustomers().clear();

        User updatedTrader = userRepository.save(trader);
        log.info("Successfully removed all customers from trader with ID: {}", traderId);
        return userMapper.toUserResponse(updatedTrader);
    }
}
