package com.groupy.service;

import java.time.Year;

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
        	
        	String currentYear = String.valueOf(Year.now().getValue());
        	
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true);

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset");
            helper.setText(
                    "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 20px;'>"
                            + "<h2 style='color: #333;'>Password Reset Request</h2>"
                            + "<p>Hello,</p>"
                            + "<p>We received a request to reset your password.</p>"

                            + "<div style='text-align: center; margin: 30px 0;'>"
                            + "<a href='" + resetLink + "' "
                            + "style='background-color: #4CAF50; color: white; padding: 12px 20px; "
                            + "text-decoration: none; border-radius: 5px; display: inline-block;'>"
                            + "Reset Password"
                            + "</a>"
                            + "</div>"

                            + "<p>This link expires in <strong>15 minutes</strong>.</p>"

                            + "<hr style='margin: 30px 0;'>"

                            + "<p style='font-size: 12px; color: gray;'>"
                            + "© " + currentYear + " Groupy. All rights reserved."
                            + "</p>"
                            + "</div>",
                    true
            );

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Email sending failed" + e.getMessage());
        }
    }
}

