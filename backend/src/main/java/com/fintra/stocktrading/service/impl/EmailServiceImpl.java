package com.fintra.stocktrading.service.impl;

import com.fintra.stocktrading.exception.EmailServiceException;
import com.fintra.stocktrading.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.email.from:noreply@fintra.com.tr}")
    private String fromEmail;

    @Value("${app.email.password-reset.subject:Fintra Stock Trading - Password Reset}")
    private String passwordResetSubject;

    @Value("${app.email.password-reset.template:password-reset}")
    private String passwordResetTemplate;

    @Override
    public void sendPasswordResetToken(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(passwordResetSubject);

            Context context = createPasswordResetContext(to, token);
            String emailContent = templateEngine.process(passwordResetTemplate, context);
            helper.setText(emailContent, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new EmailServiceException("Failed to send password reset email", e);
        } catch (Exception e) {
            log.error("Unexpected error while sending email to: {}", to, e);
            throw new EmailServiceException("Email service error", e);
        }
    }

    private Context createPasswordResetContext(String email, String token) {
        Context context = new Context();
        context.setVariable("token", token);
        context.setVariable("userName", extractUsernameFromEmail(email));
        context.setVariable("email", email);
        context.setVariable("companyName", "Fintra Stock Trading");
        return context;
    }

    private String extractUsernameFromEmail(String email) {
        return email.contains("@") ? email.split("@")[0] : email;
    }
}
