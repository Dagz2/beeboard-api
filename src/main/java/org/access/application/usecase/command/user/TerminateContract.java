package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.TerminateContractCommand;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

/**
 * Use case for terminating a user's contract.
 *
 * This command updates the user's contract end date to today
 * and locks the account, then persists the change using {@link UserRepository}.
 */
@ApplicationScoped
public class TerminateContract {

    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository for accessing and updating users
     */
    public TerminateContract(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Executes the command to terminate a user's contract.
     * The user's account will also be locked.
     *
     * @param command the command containing the user ID
     * @throws IllegalArgumentException if the user does not exist
     */
    @Transactional
    public void execute(TerminateContractCommand command) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.terminateContract();
        userRepository.update(user);
    }
}
