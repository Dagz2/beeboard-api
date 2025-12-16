package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.ChangePasswordCommand;
import org.access.domain.model.Password;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

/**
 * Use case for changing a user's password.
 *
 * This command updates the user's password in the domain model
 * and persists the change using the {@link UserRepository}.
 * The password is hashed using BCrypt before being stored.
 */
@ApplicationScoped
public class ChangePassword {

    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository for accessing and saving users
     */
    public ChangePassword(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Executes the command to change a user's password.
     *
     * @param command contains the user ID and the new password
     * @throws IllegalArgumentException if the user is not found or password validation fails
     */
    @Transactional
    public void execute(ChangePasswordCommand command) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.changePassword(Password.fromPlainText(command.newPassword()));
        userRepository.update(user);
    }
}
