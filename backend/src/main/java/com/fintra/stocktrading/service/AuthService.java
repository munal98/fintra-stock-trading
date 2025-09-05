package com.fintra.stocktrading.service;

import com.fintra.stocktrading.model.dto.request.LoginRequest;
import com.fintra.stocktrading.model.dto.response.AuthResponse;

public interface AuthService {

    /**
     * Authenticates user and generates JWT token
     *
     * @param loginRequest the login credentials
     * @return authentication response with token and user details
     * @throws com.fintra.stocktrading.exception.UnauthorizedException if credentials are invalid
     */
    AuthResponse login(LoginRequest loginRequest);
}
