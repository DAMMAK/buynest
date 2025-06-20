package dev.dammak.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmailVerification(String toEmail, String verificationToken) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("Email Verification - E-commerce Platform");

            String verificationUrl = frontendUrl + "/verify-email?token=" + verificationToken;
            String text = String.format(
                    "Welcome to our E-commerce Platform!\n\n" +
                            "Please click the link below to verify your email address:\n" +
                            "%s\n\n" +
                            "This link will expire in 24 hours.\n\n" +
                            "If you didn't create an account, please ignore this email.\n\n" +
                            "Best regards,\n" +
                            "E-commerce Platform Team",
                    verificationUrl
            );

            message.setText(text);
            mailSender.send(message);

            log.info("Email verification sent to: {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email verification to: {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}