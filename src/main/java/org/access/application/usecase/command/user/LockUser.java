package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.LockUserCommand;
import org.access.application.service.AuditService;
import org.access.domain.model.SecurityContext;
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
    private final AuditService auditService;

    /**
     * Constructs the use case with the required repository.
     *
     * @param userRepository the repository used to access and update users
     * @param auditService   the service for audit logging
     */
    @Inject
    public LockUser(UserRepository userRepository, AuditService auditService) {
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    /**
     * Executes the command to lock a user account.
     *
     * @param command         the command containing the user ID to lock
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the user does not exist
     * @throws IllegalStateException if the user is already locked
     */
    @Transactional
    public void execute(LockUserCommand command, SecurityContext securityContext) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        user.lock();
        userRepository.update(user);

        // Audit the operation
        auditService.record(
            securityContext,
            "LOCK_USER",
            "USER",
            command.userId().toString(),
            "{\"isLocked\": false}",
            "{\"isLocked\": true}"
        );
    }
}
