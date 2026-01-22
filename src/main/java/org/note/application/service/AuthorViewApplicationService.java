package org.note.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.domain.model.AccessLevel;
import org.note.domain.repository.AuthorViewRepository;
import org.shared.domain.AccountStatus;

import java.util.UUID;

/**
 * Application service for managing AuthorView (CQRS read model).
 *
 * <p>This service coordinates AuthorView repository operations with proper
 * transaction boundaries. It's invoked by UserEventHandler when processing
 * domain events from Access BC.</p>
 *
 * <p>Design rationale:</p>
 * <ul>
 *   <li>Provides a single transaction boundary for each operation</li>
 *   <li>Decouples event handlers from repository implementation details</li>
 *   <li>Makes it easier to add cross-cutting concerns (logging, metrics, etc.)</li>
 * </ul>
 */
@ApplicationScoped
public class AuthorViewApplicationService {

    @Inject
    AuthorViewRepository authorViewRepository;

    /**
     * Updates the full name of an author.
     *
     * <p>Called when UserFullNameChangedEvent is received.</p>
     *
     * @param userId the user ID to update
     * @param fullName the new full name
     */
    @Transactional
    public void updateFullName(UUID userId, String fullName) {
        authorViewRepository.updateFullName(userId, fullName);
    }

    /**
     * Updates the access level of an author.
     *
     * <p>Called when UserRoleChangedEvent or RoleApplicationRoleChangedEvent is received.</p>
     *
     * @param userId the user ID to update
     * @param accessLevel the new access level
     */
    @Transactional
    public void updateAccessLevel(UUID userId, AccessLevel accessLevel) {
        authorViewRepository.updateAccessLevel(userId, accessLevel);
    }

    /**
     * Updates the account status of an author.
     *
     * <p>Called when UserLockedEvent, UserUnlockedEvent, or UserContractTerminatedEvent is received.</p>
     *
     * @param userId the user ID to update
     * @param accountStatus the new account status
     */
    @Transactional
    public void updateAccountStatus(UUID userId, AccountStatus accountStatus) {
        authorViewRepository.updateAccountStatus(userId, accountStatus);
    }

    /**
     * Deletes an author from the view.
     *
     * <p>Called when UserDeletedEvent is received.
     * Part of GDPR compliance - removes user data from Note BC.</p>
     *
     * @param userId the user ID to delete
     */
    @Transactional
    public void deleteAuthor(UUID userId) {
        authorViewRepository.deleteById(userId);
    }
}
