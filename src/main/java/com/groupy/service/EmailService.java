package com.groupy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendResetEmail(String toEmail, String token) {

        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset");
            helper.setText(
                    "<h3>Password Reset</h3>" +
                    "<p>Click below:</p>" +
                    "<a href='" + resetLink + "'>Reset Password</a>",
                    true
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Email sending failed");
        }
    }
}

