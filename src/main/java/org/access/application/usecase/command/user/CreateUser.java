package org.access.application.usecase.command.user;

import org.access.application.dto.command.user.CreateUserCommand;
import org.access.domain.model.*;
import org.access.domain.repository.UserRepository;
import org.access.domain.repository.RoleRepository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

/**
 * Use case responsible for creating a new {@link User} in the system.
 *
 * This class encapsulates the application logic for creating users, ensuring
 * that all domain constraints are respected, and that the user is persisted
 * via the appropriate repository.
 */
@ApplicationScoped
public class CreateUser {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Constructs the use case with the required repositories.
     *
     * @param userRepository the repository used to persist the new user
     * @param roleRepository the repository used to verify role existence
     */
    public CreateUser(UserRepository userRepository, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    /**
     * Executes the use case to create a new user.
     *
     * @param command the input data needed to create the user
     * @throws IllegalArgumentException if the role does not exist or input is invalid
     */
    @Transactional
    public void execute(CreateUserCommand command) {
        // Validate role existence
        RoleId roleId = new RoleId(command.roleId());
        if (!roleRepository.existsById(roleId)) {
            throw new IllegalArgumentException("Role not found with ID: " + command.roleId());
        }

        // Construct value objects from command input
        FullName fullName = new FullName(command.firstName(), command.lastName());
        EmailAddress email = new EmailAddress(command.email());
        ContractEndDate contractEndDate = new ContractEndDate(command.contractEndDate());

        // Create new user aggregate
        User user = User.create(fullName, email, roleId, contractEndDate);

        // Persist user
        userRepository.save(user);
    }
}
