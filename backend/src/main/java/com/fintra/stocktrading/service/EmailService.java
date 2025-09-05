package com.fintra.stocktrading.service;

public interface EmailService {

    /**
     * Sends a password reset token to the user's email
     *
     * @param to recipient email address
     * @param token password reset token
     */
    void sendPasswordResetToken(String to, String token);
}
