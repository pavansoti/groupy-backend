package com.groupy.service;

import java.time.Year;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.mail.sendgrid-api-key}")
    private String sendGridApiKey;

    public void sendResetEmail(String toEmail, String token) {

        String resetLink = frontendUrl + "/auth/reset-password?token=" + token;
        String currentYear = String.valueOf(Year.now().getValue());

        try {

            log.info("Starting password reset email process for: {}", toEmail);
            log.info("Email sending from: {}", fromEmail);

            String htmlContent =
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
                            + "</div>";

            Email from = new Email(fromEmail);
            Email to = new Email(toEmail);
            Content content = new Content("text/html", htmlContent);
            Mail mail = new Mail(from, "Password Reset", to, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            log.info("SendGrid response status: {}", response.getStatusCode());

            if (response.getStatusCode() >= 400) {
                throw new RuntimeException("SendGrid error: " + response.getBody());
            }

            log.info("Password reset email successfully sent to: {}", toEmail);

        } catch (Exception e) {
            log.error("Email sending failed for: {}", toEmail, e);
            log.error("Email sending failed from: {}", fromEmail);
            throw new RuntimeException("Email sending failed: " + e.getMessage(), e);
        }
    }
}
