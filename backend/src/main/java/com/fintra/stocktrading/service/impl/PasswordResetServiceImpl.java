package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.BadRequestException;
import com.fintra.stocktrading.exception.NotFoundException;
import com.fintra.stocktrading.model.entity.User;
import com.fintra.stocktrading.model.entity.PasswordResetToken;
import com.fintra.stocktrading.repository.PasswordResetTokenRepository;
import com.fintra.stocktrading.repository.UserRepository;
import com.fintra.stocktrading.service.EmailService;
import com.fintra.stocktrading.service.PasswordResetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetServiceImpl implements PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.password-reset.token-expiration-minutes}")
    private int tokenExpirationMinutes;

    @Override
    @Transactional
    public void initiatePasswordReset(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isEmpty()) {
            log.warn("Password reset requested for non-existent email: {}", email);
            generateRandomToken();
            throw new NotFoundException("User not found with email: " + email);
        }

        User user = userOptional.get();

        passwordResetTokenRepository.findByUser(user)
                .ifPresent(passwordResetTokenRepository::delete);

        String token = generateRandomToken();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusMinutes(tokenExpirationMinutes));

        passwordResetTokenRepository.save(passwordResetToken);

        emailService.sendPasswordResetToken(email, token);

        log.info("Password reset token generated for user: {}", email);
    }

    @Override
    public void verifyPasswordResetToken(String email, String token) {
        findValidToken(email, token);
    }

    @Override
    @Transactional
    public void resetPassword(String email, String token, String newPassword) {
        PasswordResetToken passwordResetToken = findValidToken(email, token);
        User user = passwordResetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(passwordResetToken);

        log.info("Password reset completed for user: {}", email);
    }

    private PasswordResetToken findValidToken(String email, String token) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            log.warn("Token verification requested for non-existent email: {}", email);
            throw new NotFoundException("User not found with email: " + email);
        }

        User user = userOptional.get();
        Optional<PasswordResetToken> tokenOptional = passwordResetTokenRepository.findByUser(user);

        if (tokenOptional.isEmpty()) {
            log.warn("No token found for user: {}", email);
            throw new BadRequestException("No password reset token found for this user");
        }

        PasswordResetToken passwordResetToken = tokenOptional.get();

        if (passwordResetToken.isExpired()) {
            log.warn("Expired token used for user: {}", email);
            // Clean up expired token
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new BadRequestException("Password reset token has expired");
        }

        if (!passwordResetToken.getToken().equals(token)) {
            log.warn("Invalid token provided for user: {}", email);
            throw new BadRequestException("Invalid password reset token");
        }

        return passwordResetToken;
    }

    private String generateRandomToken() {
        int token = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(token);
    }
}
