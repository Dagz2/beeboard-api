package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.ChangeFullNameCommand;
import org.access.domain.model.FullName;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

/**
 * Use case for changing the full name of a user.
 *
 * This command updates the user's full name in the domain model
 * and persists the change using the {@link UserRepository}.
 */
@ApplicationScoped
public class ChangeFullName {

    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository for accessing and saving users
     */
    public ChangeFullName(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Executes the command to change a user's full name.
     *
     * @param userId      the ID of the user to update
     * @param newFullName the new full name to assign
     * @throws IllegalArgumentException if the user does not exist
     */
    @Transactional
    public void execute(ChangeFullNameCommand command) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.changeFullName(new FullName(command.firstName(), command.lastName()));
        userRepository.update(user);
    }
}
