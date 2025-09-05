package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.AuthControllerDoc;
import com.fintra.stocktrading.model.dto.request.LoginRequest;
import com.fintra.stocktrading.model.dto.response.AuthResponse;
import com.fintra.stocktrading.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController implements AuthControllerDoc {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login attempt for email: {}", loginRequest.getEmail());
        
        AuthResponse authResponse = authService.login(loginRequest);
        
        return ResponseEntity.ok(authResponse);
    }
}
