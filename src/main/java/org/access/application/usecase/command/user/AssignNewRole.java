package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import org.access.application.dto.command.user.AssignNewRoleCommand;
import org.access.domain.model.RoleId;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;

/**
 * Use case for assigning a new role to a user.
 *
 * This command changes the user's current role by updating their
 * role reference in the domain model and persisting the change
 * using the {@link UserRepository}.
 */
@ApplicationScoped
public class AssignNewRole {

    private final UserRepository userRepository;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository for accessing and saving users
     */
    public AssignNewRole(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Executes the command to assign a new role to a user.
     *
     * @param command contains the user ID and the new role ID
     * @throws IllegalArgumentException if the user is not found
     */
    public void execute(AssignNewRoleCommand command) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.assignRole(new RoleId(command.roleId()));
        userRepository.update(user);
    }
}
