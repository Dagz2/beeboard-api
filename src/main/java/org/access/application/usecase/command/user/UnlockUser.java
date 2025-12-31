package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.UnlockUserCommand;
import org.access.application.service.AuditService;
import org.access.domain.model.SecurityContext;
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
    private final AuditService auditService;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository for accessing and updating users
     * @param auditService   the service for audit logging
     */
    @Inject
    public UnlockUser(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    /**
     * Executes the command to unlock a user account.
     *
     * @param command         the command containing the user ID
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the user is not found
     */
    @Transactional
    public void execute(UnlockUserCommand command, SecurityContext securityContext) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.unlock();
        userRepository.update(user);

        // Audit the operation
        auditService.record(
            securityContext,
            "UNLOCK_USER",
            "USER",
            command.userId().toString(),
            "{\"isLocked\": true}",
            "{\"isLocked\": false}"
        );
    }
}
