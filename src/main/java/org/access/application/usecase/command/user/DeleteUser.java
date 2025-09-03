package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.command.user.DeleteUserCommand;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

/**
 * Use case for permanently deleting a user from the system.
 *
 * This command performs a hard delete operation, completely removing the user
 * from the database. It validates that the user is locked before deletion
 * to ensure proper business rules are followed.
 */
@ApplicationScoped
public class DeleteUser {

    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository used to access and delete users
     */
    public DeleteUser(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Executes the command to permanently delete a user.
     *
     * @param command the command containing the user ID to delete
     * @throws IllegalArgumentException if the user does not exist
     * @throws IllegalStateException if the user is not locked (must be locked before deletion)
     */
    public void execute(DeleteUserCommand command) {
        UserId userId = new UserId(command.userId());
        
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        if (!user.isLocked()) {
            throw new IllegalStateException("User must be locked before deletion. User ID: " + command.userId());
        }

        userRepository.deleteById(userId);
    }
}