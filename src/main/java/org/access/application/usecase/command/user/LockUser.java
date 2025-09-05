package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.LockUserCommand;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

/**
 * Use case for locking a user account.
 *
 * This command sets the user status to locked,
 * preventing authentication or system access.
 * The change is persisted through {@link UserRepository}.
 */
@ApplicationScoped
public class LockUser {

    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository used to access and update users
     */
    public LockUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Executes the command to lock a user account.
     *
     * @param command the command containing the user ID to lock
     * @throws IllegalArgumentException if the user does not exist
     * @throws IllegalStateException if the user is already locked
     */
    @Transactional
    public void execute(LockUserCommand command) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.lock();
        userRepository.update(user);
    }
}
