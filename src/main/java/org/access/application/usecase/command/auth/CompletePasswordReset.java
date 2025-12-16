package org.access.application.usecase.command.auth;

import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.access.application.dto.command.auth.CompletePasswordResetCommand;
import org.access.domain.model.*;
import org.access.domain.repository.PasswordResetRequestRepository;
import org.access.domain.repository.UserRepository;

/**
 * Use case for completing a password reset using a reset token.
 *
 * This command validates the reset token and updates the user's password by:
 * 1. Validating the token format and retrieving the reset request
 * 2. Checking that the token hasn't been used and hasn't expired
 * 3. Finding the associated user
 * 4. Verifying the user account is not locked
 * 5. Changing the user's password
 * 6. Marking the token as used
 */
@ApplicationScoped
public class CompletePasswordReset {

    private final UserRepository userRepository;
    private final PasswordResetRequestRepository resetRepository;

    /**
     * Constructs the use case with required dependencies.
     *
     * @param userRepository repository for accessing and updating users
     * @param resetRepository repository for password reset requests
     */
    public CompletePasswordReset(UserRepository userRepository,
                                 PasswordResetRequestRepository resetRepository) {
        this.userRepository = userRepository;
        this.resetRepository = resetRepository;
    }

    /**
     * Executes the password reset completion.
     *
     * @param command contains the reset token and new password
     * @throws IllegalArgumentException if the token is invalid, expired, or used
     * @throws IllegalStateException if the user is not found or is locked
     */
    @Transactional
    public void execute(CompletePasswordResetCommand command) {
        // 1. Parse and validate token format (validated by DTO, but create value object)
        ResetToken token = new ResetToken(command.token());

        // 2. Find reset request by token
        PasswordResetRequest resetRequest = resetRepository.findByToken(token)
            .orElseThrow(() -> new IllegalArgumentException(
                "Invalid or expired reset token"
            ));

        // 3. Validate token can be used (not used, not expired)
        if (!resetRequest.canBeUsed()) {
            throw new IllegalArgumentException(
                "Invalid or expired reset token"
            );
        }

        // 4. Find user
        User user = userRepository.findById(resetRequest.getUserId())
            .orElseThrow(() -> new IllegalStateException(
                "User not found for reset request"
            ));

        // 5. Check user is not locked
        if (user.isLocked()) {
            throw new IllegalStateException(
                "Cannot reset password for locked account"
            );
        }

        // 6. Change password (Password value object validates the format)
        Password newPassword = Password.fromPlainText(command.newPassword());
        user.changePassword(newPassword);

        // 7. Mark token as used
        resetRequest.markAsUsed();

        // 8. Save changes
        userRepository.update(user);
        resetRepository.update(resetRequest);

        Log.info("Password successfully reset for user: " + user.getEmail());
    }
}
