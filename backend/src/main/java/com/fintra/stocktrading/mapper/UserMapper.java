package com.fintra.stocktrading.mapper;

import com.fintra.stocktrading.model.dto.request.UserCreateRequest;
import com.fintra.stocktrading.model.dto.response.UserResponse;
import com.fintra.stocktrading.model.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;

    public UserMapper(PasswordEncoder passwordEncoder, CustomerMapper customerMapper) {
        this.passwordEncoder = passwordEncoder;
        this.customerMapper = customerMapper;
    }

    public User toUser(UserCreateRequest request) {
        if (request == null) {
            return null;
        }

        return User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .role(request.getRole())
                .build();
    }

    public UserResponse toUserResponse(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .customers(user.getCustomers() != null ? user.getCustomers().stream()
                        .map(customerMapper::toCustomerResponse)
                        .collect(Collectors.toList()) : new ArrayList<>())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public UserResponse toUserResponseWithoutCustomers(User user) {
        if (user == null) {
            return null;
        }

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .customers(new ArrayList<>())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
