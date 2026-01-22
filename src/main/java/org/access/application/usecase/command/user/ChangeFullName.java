package org.access.application.usecase.command.user;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.access.application.dto.command.user.ChangeFullNameCommand;
import org.access.domain.event.UserFullNameChangedEvent;
import org.access.domain.model.FullName;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;
import org.shared.event.EventPublisher;

/**
 * Use case for changing the full name of a user.
 *
 * This command updates the user's full name in the domain model
 * and persists the change using the {@link UserRepository}.
 *
 * Publishes a {@link UserFullNameChangedEvent} for cross-BC synchronization.
 */
@ApplicationScoped
public class ChangeFullName {

    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;

    /**
     * Constructs the use case with the required dependencies.
     *
     * @param userRepository the repository for accessing and saving users
     * @param eventPublisher the publisher for domain events
     */
    @Inject
    public ChangeFullName(UserRepository userRepository, EventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Executes the command to change a user's full name.
     *
     * @param command         the command containing the user ID and new name
     * @param securityContext the security context of the actor performing the operation
     * @throws IllegalArgumentException if the user does not exist
     */
    @Transactional
    public void execute(ChangeFullNameCommand command, SecurityContext securityContext) {
        User user = userRepository.findById(new UserId(command.userId()))
            .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + command.userId()));

        // Capture old name BEFORE change
        FullName oldFullName = user.getFullName();

        // Apply domain change
        user.changeFullName(new FullName(command.firstName(), command.lastName()));
        userRepository.update(user);

        // Publish event AFTER successful update (within transaction)
        eventPublisher.publishAsync(new UserFullNameChangedEvent(
            null,  // eventId (auto-generated)
            user.getId().id(),
            oldFullName.firstName(),
            oldFullName.lastName(),
            command.firstName(),
            command.lastName(),
            securityContext.actorId().id(),
            null   // occurredAt (auto-generated)
        ));
    }
}
