package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.command.user.ExtendContractCommand;
import org.access.domain.model.ContractEndDate;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

/**
 * Use case for extending a user's contract.
 *
 * This command updates the user's contract end date
 * and persists the updated user through the {@link UserRepository}.
 */
@ApplicationScoped
public class ExtendContract {

    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository for accessing and saving users
     */
    public ExtendContract(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Executes the contract extension command for a given user.
     *
     * @param command the command containing the user ID and new contract end date
     * @throws IllegalArgumentException if the user does not exist or the new date is invalid
     */
    public void execute(ExtendContractCommand command) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.extendContract(new ContractEndDate(command.newEndDate()));
        userRepository.update(user);
    }
}
