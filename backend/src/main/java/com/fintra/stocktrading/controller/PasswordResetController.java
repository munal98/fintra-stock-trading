package com.fintra.stocktrading.controller;

import com.fintra.stocktrading.controller.doc.PasswordResetControllerDoc;
import com.fintra.stocktrading.model.dto.request.PasswordResetRequest;
import com.fintra.stocktrading.model.dto.request.PasswordUpdateRequest;
import com.fintra.stocktrading.model.dto.request.PasswordVerifyRequest;
import com.fintra.stocktrading.model.dto.response.MessageResponse;
import com.fintra.stocktrading.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/password")
@RequiredArgsConstructor
@Slf4j
public class PasswordResetController implements PasswordResetControllerDoc {

    private final PasswordResetService passwordResetService;

    @PostMapping("/reset-request")
    public ResponseEntity<MessageResponse> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        log.info("Password reset requested for email: {}", request.getEmail());
        
        passwordResetService.initiatePasswordReset(request.getEmail());
        
        MessageResponse response = MessageResponse.builder()
                .message("Password reset email sent successfully")
                .build();
        
        log.info("Password reset email sent successfully to: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-token")
    public ResponseEntity<MessageResponse> verifyToken(@Valid @RequestBody PasswordVerifyRequest request) {
        log.info("Token verification attempt for email: {}", request.getEmail());
        
        passwordResetService.verifyToken(request.getEmail(), request.getToken());
        
        MessageResponse response = MessageResponse.builder()
                .message("Token verified successfully")
                .build();
        
        log.info("Token verified successfully for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-complete")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody PasswordUpdateRequest request) {
        log.info("Password reset attempt for email: {}", request.getEmail());
        
        passwordResetService.resetPassword(request.getEmail(), request.getToken(), request.getNewPassword());
        
        MessageResponse response = MessageResponse.builder()
                .message("Password reset successfully")
                .build();
        
        log.info("Password reset completed successfully for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }
}
