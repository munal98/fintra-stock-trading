package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.UnauthorizedException;
import com.fintra.stocktrading.model.dto.request.LoginRequest;
import com.fintra.stocktrading.model.dto.response.AuthResponse;
import com.fintra.stocktrading.model.entity.User;
import com.fintra.stocktrading.security.jwt.JwtTokenUtil;
import com.fintra.stocktrading.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        log.info("Attempting login for user: {}", loginRequest.getEmail());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );

            User user = (User) authentication.getPrincipal();
            String role = user.getRole().name();
            String token = jwtTokenUtil.generateToken(user);

            log.info("Login successful for user: {} with role: {}", user.getEmail(), role);

            return AuthResponse.builder()
                    .token(token)
                    .email(user.getEmail())
                    .role(role)
                    .id(user.getId())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .build();
        } catch (BadCredentialsException e) {
            log.warn("Failed login attempt for user: {}", loginRequest.getEmail());
            throw new UnauthorizedException("Invalid email or password");
        }
    }
}
