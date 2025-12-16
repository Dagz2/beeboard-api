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

        String subject = "Réinitialisation de mot de passe - Beeboard";
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
                        <h1>Réinitialisation de mot de passe</h1>
                    </div>
                    <div class="content">
                        <p>Bonjour %s,</p>
                        <p>Vous avez demandé à réinitialiser votre mot de passe pour votre compte Beeboard.</p>
                        <p>Cliquez sur le bouton ci-dessous pour réinitialiser votre mot de passe :</p>
                        <p style="text-align: center;">
                            <a href="%s" class="button">Réinitialiser mon mot de passe</a>
                        </p>
                        <p class="warning">⚠️ Ce lien expire dans 15 minutes.</p>
                        <p>Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet email en toute sécurité.</p>
                        <p>Pour des raisons de sécurité, ce lien ne peut être utilisé qu'une seule fois.</p>
                    </div>
                    <div class="footer">
                        <p><strong>Beeboard - Système de gestion des accès</strong></p>
                        <p>Ceci est un email automatique, merci de ne pas répondre.</p>
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
            Réinitialisation de mot de passe - Beeboard

            Bonjour %s,

            Vous avez demandé à réinitialiser votre mot de passe pour votre compte Beeboard.

            Cliquez sur ce lien pour réinitialiser votre mot de passe :
            %s

            ⚠️ ATTENTION : Ce lien expire dans 15 minutes.

            Si vous n'avez pas demandé cette réinitialisation, vous pouvez ignorer cet email en toute sécurité.

            Pour des raisons de sécurité, ce lien ne peut être utilisé qu'une seule fois.

            ---
            Beeboard - Système de gestion des accès
            Ceci est un email automatique, merci de ne pas répondre.
            """.formatted(fullName, resetLink);
    }
}
