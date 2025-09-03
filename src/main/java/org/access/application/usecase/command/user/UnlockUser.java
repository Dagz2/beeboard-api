package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.command.user.UnlockUserCommand;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

/**
 * Use case for unlocking a user account.
 *
 * This command reactivates the user's account by setting the `isLocked` flag to false,
 * and persists the change using {@link UserRepository}.
 */
@ApplicationScoped
public class UnlockUser {

    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository for accessing and updating users
     */
    public UnlockUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Executes the command to unlock a user account.
     *
     * @param command the command containing the user ID
     * @throws IllegalArgumentException if the user is not found
     */
    public void execute(UnlockUserCommand command) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.unlock();
        userRepository.update(user);
    }
}
