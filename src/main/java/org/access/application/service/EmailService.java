package org.access.application.service;

import io.quarkus.logging.Log;
import io.quarkus.mailer.Mail;
import io.quarkus.mailer.Mailer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.access.domain.model.EmailAddress;
import org.access.domain.model.FullName;
import org.access.domain.model.ResetToken;

/**
 * Service responsible for sending emails related to password reset operations.
 *
 * Uses Quarkus Mailer to send both HTML and plain text versions of emails.
 */
@ApplicationScoped
public class EmailService {

    @Inject
    Mailer mailer;

    @ConfigProperty(name = "frontend.base.url", defaultValue = "http://localhost:3000")
    String frontendBaseUrl;

    @ConfigProperty(name = "password.reset.enabled", defaultValue = "true")
    Boolean passwordResetEnabled;

    /**
     * Sends a password reset email to the specified user.
     *
     * The email contains a link with a reset token that is valid for 15 minutes.
     * Both HTML and plain text versions are included for compatibility.
     *
     * @param to the recipient's email address
     * @param token the reset token to include in the link
     * @param fullName the recipient's full name for personalization
     * @throws IllegalStateException if email sending fails
     */
    public void sendPasswordResetEmail(EmailAddress to, ResetToken token, FullName fullName) {
        if (!passwordResetEnabled) {
            Log.info("Password reset emails are disabled in configuration");
            return;
        }

        String resetLink = frontendBaseUrl + "/reset-password?token=" + token.value();
        String fullNameStr = fullName.firstName() + " " + fullName.lastName();

        String subject = "Password Reset - Beeboard";
        String htmlBody = buildPasswordResetEmailHtml(fullNameStr, resetLink);
        String textBody = buildPasswordResetEmailText(fullNameStr, resetLink);

        try {
            mailer.send(Mail.withHtml(to.value(), subject, htmlBody)
                           .setText(textBody));

            Log.info("Password reset email sent to: " + to.value());
        } catch (Exception e) {
            Log.error("Failed to send password reset email to: " + to.value(), e);
            throw new IllegalStateException("Failed to send reset email. Please try again later.");
        }
    }

    /**
     * Builds the HTML version of the password reset email.
     *
     * @param fullName the recipient's full name
     * @param resetLink the password reset link with token
     * @return the HTML email content
     */
    private String buildPasswordResetEmailHtml(String fullName, String resetLink) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        line-height: 1.6;
                        color: #333;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #f9f9f9;
                    }
                    .header {
                        background-color: #007bff;
                        color: white;
                        padding: 20px;
                        text-align: center;
                    }
                    .content {
                        padding: 30px;
                        background-color: white;
                    }
                    .button {
                        display: inline-block;
                        background-color: #007bff;
                        color: white;
                        padding: 12px 24px;
                        text-decoration: none;
                        border-radius: 4px;
                        margin: 20px 0;
                    }
                    .warning {
                        color: #dc3545;
                        font-weight: bold;
                        margin: 20px 0;
                    }
                    .footer {
                        padding: 20px;
                        text-align: center;
                        color: #666;
                        font-size: 12px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Password Reset</h1>
                        <p style="font-style: italic; margin-top: 10px;">"Identity theft is not a joke, Jim! Millions of families suffer every year!" - Dwight Schrute</p>
                    </div>
                    <div class="content">
                        <p>Hello %s,</p>
                        <p>You have requested to reset your password for your Beeboard account.</p>
                        <p>Click the button below to reset your password:</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Reset my password</a>
                        </p>
                        <p class="warning">This link expires in 15 minutes.</p>
                        <p>If you did not request this reset, you can safely ignore this email.</p>
                        <p>For security reasons, this link can only be used once.</p>
                    </div>
                    <div class="footer">
                        <p><strong>Beeboard - Access Management System</strong></p>
                        <p>This is an automated email, please do not reply.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, resetLink);
    }

    /**
     * Builds the plain text version of the password reset email.
     *
     * @param fullName the recipient's full name
     * @param resetLink the password reset link with token
     * @return the plain text email content
     */
    private String buildPasswordResetEmailText(String fullName, String resetLink) {
        return """
            Password Reset - Beeboard
            "Identity theft is not a joke, Jim! Millions of families suffer every year!" - Dwight Schrute

            Hello %s,

            You have requested to reset your password for your Beeboard account.

            Click this link to reset your password:
            %s

            WARNING: This link expires in 15 minutes.

            If you did not request this reset, you can safely ignore this email.

            For security reasons, this link can only be used once.

            ---
            Beeboard - Access Management System
            This is an automated email, please do not reply.
            """.formatted(fullName, resetLink);
    }
}
