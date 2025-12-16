package org.access.application.usecase.command.auth;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import org.access.application.dto.command.auth.RequestPasswordResetCommand;
import org.access.application.service.EmailService;
import org.access.application.service.RateLimitService;
import org.access.domain.model.*;
import org.access.domain.repository.PasswordResetRequestRepository;
import org.access.domain.repository.UserRepository;

import java.util.Optional;

/**
 * Use case for requesting a password reset.
 *
 * This command initiates the password reset flow by:
 * 1. Checking rate limits to prevent abuse
 * 2. Validating the user exists and is not locked
 * 3. Invalidating any existing reset tokens
 * 4. Generating a new reset token
 * 5. Sending a reset email to the user
 *
 * For security, this use case returns success even if the email doesn't exist
 * or the account is locked, to prevent user enumeration attacks.
 */
@ApplicationScoped
public class RequestPasswordReset {

    private final UserRepository userRepository;
    private final PasswordResetRequestRepository resetRepository;
    private final EmailService emailService;
    private final RateLimitService rateLimitService;

    /**
     * Constructs the use case with required dependencies.
     *
     * @param userRepository repository for accessing users
     * @param resetRepository repository for password reset requests
     * @param emailService service for sending emails
     * @param rateLimitService service for rate limiting
     */
    public RequestPasswordReset(UserRepository userRepository,
                                PasswordResetRequestRepository resetRepository,
                                EmailService emailService,
                                RateLimitService rateLimitService) {
        this.userRepository = userRepository;
        this.resetRepository = resetRepository;
        this.emailService = emailService;
        this.rateLimitService = rateLimitService;
    }

    /**
     * Executes the password reset request.
     *
     * @param command contains the email address
     * @throws ClientErrorException if rate limit is exceeded (HTTP 429)
     */
    @Transactional
    public void execute(RequestPasswordResetCommand command) {
        String email = command.email();

        // 1. Check rate limit
        if (rateLimitService.isRateLimitExceeded(email)) {
            Log.warn("Rate limit exceeded for password reset request: " + email);
            throw new ClientErrorException(
                "Too many password reset requests. Please try again later.",
                Response.Status.TOO_MANY_REQUESTS
            );
        }

        // 2. Find user by email (silent fail if not found - security)
        Optional<User> userOpt = userRepository.findByMail(new EmailAddress(email));

        if (userOpt.isEmpty()) {
            Log.info("Password reset requested for non-existent email: " + email);
            // Still increment rate limit to prevent email enumeration
            rateLimitService.incrementRateLimit(email);
            // Return silently (no error) to prevent user enumeration
            return;
        }

        User user = userOpt.get();

        // 3. Check if user is locked (silent fail - security)
        if (user.isLocked()) {
            Log.warn("Password reset attempted for locked account: " + user.getEmail());
            rateLimitService.incrementRateLimit(email);
            // Return silently to prevent account status enumeration
            return;
        }

        // 4. Invalidate any existing active tokens for this user
        resetRepository.invalidateAllForUser(user.getId());

        // 5. Generate new token
        ResetToken token = ResetToken.generate();

        // 6. Create password reset request
        PasswordResetRequest resetRequest = PasswordResetRequest.create(user.getId(), token);

        // 7. Save to database
        resetRepository.save(resetRequest);

        // 8. Send email
        try {
            emailService.sendPasswordResetEmail(
                user.getEmail(),
                token,
                user.getFullName()
            );
            Log.info("Password reset initiated for user: " + user.getEmail());
        } catch (Exception e) {
            Log.error("Failed to send password reset email to: " + user.getEmail(), e);
            // The token is already saved, so we throw an exception
            // to inform the user to try again
            throw new IllegalStateException(
                "Failed to send reset email. Please try again later."
            );
        }

        // 9. Increment rate limit (after successful email send)
        rateLimitService.incrementRateLimit(email);
    }
}
