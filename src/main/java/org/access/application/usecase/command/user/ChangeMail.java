package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.ChangeMailCommand;
import org.access.domain.model.EmailAddress;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

/**
 * Use case for changing the email address of a user.
 *
 * This command updates the user's email in the domain model
 * and persists the change using the {@link UserRepository}.
 */
@ApplicationScoped
public class ChangeMail {

    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository for accessing and saving users
     */
    public ChangeMail(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Executes the command to change a user's email.
     *
     * @param command contains the user ID and the new email address
     * @throws IllegalArgumentException if the user is not found
     */
    @Transactional
    public void execute(ChangeMailCommand command) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.changeEmail(new EmailAddress(command.email()));
        userRepository.update(user);
    }
}
