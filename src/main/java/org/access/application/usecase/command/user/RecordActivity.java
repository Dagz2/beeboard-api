package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.RecordActivityCommand;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

/**
 * Use case for recording user activity.
 *
 * This command updates the user's last activity timestamp
 * and persists the change using the {@link UserRepository}.
 */
@ApplicationScoped
public class RecordActivity {

    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository for accessing and updating users
     */
    public RecordActivity(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Executes the command to record a user's activity.
     *
     * @param command the command containing the user ID
     * @throws IllegalArgumentException if the user does not exist
     */
    @Transactional
    public void execute(RecordActivityCommand command) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.recordActivity();
        userRepository.update(user);
    }
}
