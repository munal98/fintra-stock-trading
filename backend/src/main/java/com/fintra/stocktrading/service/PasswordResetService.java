package com.fintra.stocktrading.service;

public interface PasswordResetService {

    /**
     * Initiates the password reset process by generating a token and sending it to the user's email
     *
     * @param email the user's email address
     * @throws com.fintra.stocktrading.exception.NotFoundException if user not found
     * @throws com.fintra.stocktrading.exception.EmailServiceException if email sending fails
     */
    void initiatePasswordReset(String email);

    /**
     * Verifies if the provided token is valid for the given email
     *
     * @param email the user's email address
     * @param token the password reset token
     * @throws com.fintra.stocktrading.exception.NotFoundException if user not found
     * @throws com.fintra.stocktrading.exception.BadRequestException if token is invalid or expired
     */
    void verifyPasswordResetToken(String email, String token);

    /**
     * Convenience method to verify token - delegates to verifyPasswordResetToken
     *
     * @param email the user's email address
     * @param token the password reset token
     * @throws com.fintra.stocktrading.exception.NotFoundException if user not found
     * @throws com.fintra.stocktrading.exception.BadRequestException if token is invalid or expired
     */
    default void verifyToken(String email, String token) {
        verifyPasswordResetToken(email, token);
    }

    /**
     * Completes the password reset process by updating the user's password
     *
     * @param email the user's email address
     * @param token the password reset token
     * @param newPassword the new password
     * @throws com.fintra.stocktrading.exception.NotFoundException if user not found
     * @throws com.fintra.stocktrading.exception.BadRequestException if token is invalid or expired
     */
    void resetPassword(String email, String token, String newPassword);
}
