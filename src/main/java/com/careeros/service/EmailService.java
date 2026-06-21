package com.careeros.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Email service using Resend API.
 * Sends transactional emails for password reset and welcome messages.
 */
@Slf4j
@Service
public class EmailService {

    @Value("${resend.api-key}")
    private String apiKey;

    @Value("${resend.from:onboarding@resend.dev}")
    private String fromAddress;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Send password reset email with a reset link.
     */
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        String resetUrl = frontendUrl + "/reset-password?token=" + resetToken;

        String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: -apple-system, sans-serif; background: #070B14; color: #e2e8f0; padding: 40px;">
                  <div style="max-width: 480px; margin: 0 auto;">
                    <h1 style="color: #22D3EE; font-size: 24px; margin-bottom: 8px;">CareerOS</h1>
                    <p style="color: #94a3b8; margin-bottom: 32px;">Your AI career operating system</p>
                    <h2 style="font-size: 20px; color: #f1f5f9;">Reset your password</h2>
                    <p style="color: #94a3b8;">Click the button below to reset your password. This link expires in 15 minutes.</p>
                    <a href="%s"
                       style="display: inline-block; margin: 24px 0; padding: 12px 24px;
                              background: #22D3EE; color: #070B14; font-weight: 600;
                              text-decoration: none; border-radius: 8px;">
                      Reset Password
                    </a>
                    <p style="color: #64748b; font-size: 13px;">
                      If you didn't request this, you can safely ignore this email.
                    </p>
                  </div>
                </body>
                </html>
                """.formatted(resetUrl);

        sendEmail(toEmail, "Reset your CareerOS password", html);
    }

    /**
     * Send welcome email after OAuth registration.
     */
    public void sendWelcomeEmail(String toEmail, String name) {
        String html = """
                <!DOCTYPE html>
                <html>
                <body style="font-family: -apple-system, sans-serif; background: #070B14; color: #e2e8f0; padding: 40px;">
                  <div style="max-width: 480px; margin: 0 auto;">
                    <h1 style="color: #22D3EE; font-size: 24px; margin-bottom: 8px;">CareerOS</h1>
                    <p style="color: #94a3b8; margin-bottom: 32px;">Your AI career operating system</p>
                    <h2 style="font-size: 20px; color: #f1f5f9;">Welcome, %s!</h2>
                    <p style="color: #94a3b8;">Your account has been created. Start by uploading your resume to get an ATS score.</p>
                    <a href="%s/dashboard"
                       style="display: inline-block; margin: 24px 0; padding: 12px 24px;
                              background: #22D3EE; color: #070B14; font-weight: 600;
                              text-decoration: none; border-radius: 8px;">
                      Go to Dashboard
                    </a>
                  </div>
                </body>
                </html>
                """.formatted(name, frontendUrl);

        sendEmail(toEmail, "Welcome to CareerOS!", html);
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            Resend resend = new Resend(apiKey);
            CreateEmailOptions options = CreateEmailOptions.builder()
                    .from(fromAddress)
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();
            resend.emails().send(options);
            log.info("Email sent to {}: {}", to, subject);
        } catch (ResendException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            // Don't throw — email failure shouldn't break the main flow
        }
    }
}
