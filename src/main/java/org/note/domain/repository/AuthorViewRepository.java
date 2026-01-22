package org.note.domain.repository;

import org.note.domain.model.AccessLevel;
import org.note.domain.model.AuthorView;
import org.shared.domain.AccountStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Repository for AuthorView CQRS read model.
 *
 * <p>This repository manages the author_view table, which is a materialized projection
 * of user data from Access BC, synchronized via domain events.</p>
 *
 * <p>Unlike typical repositories, this one is optimized for:</p>
 * <ul>
 *   <li><b>Fast reads</b>: Queries are local, no cross-BC REST calls</li>
 *   <li><b>Event-driven writes</b>: Updates triggered by domain events, not use cases</li>
 *   <li><b>Partial updates</b>: Dedicated methods for updating specific fields</li>
 * </ul>
 */
public interface AuthorViewRepository {

    /**
     * Finds an author by user ID.
     *
     * <p>This is the primary query method, used by NoteUserContextService
     * to resolve user context for authorization.</p>
     *
     * @param userId the user's unique identifier
     * @return Optional containing the AuthorView if found, empty otherwise
     */
    Optional<AuthorView> findById(UUID userId);

    /**
     * Finds all authors in the system.
     *
     * <p>Use with caution - this can return a large dataset.
     * Consider pagination if needed for admin UIs.</p>
     *
     * @return List of all AuthorView entries
     */
    List<AuthorView> findAllAuthors();

    /**
     * Finds multiple authors by their user IDs in bulk.
     *
     * <p>This method is optimized for bulk queries, using a WHERE IN clause
     * to fetch all matching authors in a single database query.</p>
     *
     * <p>Use case: Enriching multiple NoteDtos or CommentDtos with author
     * information without causing N+1 query problems.</p>
     *
     * @param userIds the set of user IDs to fetch
     * @return List of AuthorView entries matching the IDs (may be fewer than requested)
     */
    List<AuthorView> findAllByIds(Set<UUID> userIds);

    /**
     * Saves a new AuthorView entry.
     *
     * <p>Called by UserEventHandler when receiving UserCreatedEvent.</p>
     *
     * @param authorView the author view to persist
     */
    void save(AuthorView authorView);

    /**
     * Updates only the access level of an existing author.
     *
     * <p>Optimized update method called by UserEventHandler when receiving:</p>
     * <ul>
     *   <li>UserRoleChangedEvent</li>
     *   <li>RoleApplicationRoleChangedEvent</li>
     * </ul>
     *
     * @param userId the user ID to update
     * @param newAccessLevel the new access level
     */
    void updateAccessLevel(UUID userId, AccessLevel newAccessLevel);

    /**
     * Updates only the account status of an existing author.
     *
     * <p>Optimized update method called by UserEventHandler when receiving:</p>
     * <ul>
     *   <li>UserLockedEvent</li>
     *   <li>UserUnlockedEvent</li>
     *   <li>UserContractTerminatedEvent</li>
     * </ul>
     *
     * @param userId the user ID to update
     * @param newStatus the new account status
     */
    void updateAccountStatus(UUID userId, AccountStatus newStatus);

    /**
     * Updates only the full name of an existing author.
     *
     * <p>Optimized update method called by UserEventHandler when receiving:</p>
     * <ul>
     *   <li>UserFullNameChangedEvent</li>
     * </ul>
     *
     * @param userId the user ID to update
     * @param fullName the new full name
     */
    void updateFullName(UUID userId, String fullName);

    /**
     * Deletes an author view entry.
     *
     * <p>Called by UserEventHandler when receiving UserDeletedEvent.
     * Part of GDPR compliance - removes user data from Note BC.</p>
     *
     * @param userId the user ID to delete
     */
    void deleteById(UUID userId);

    /**
     * Checks if an author view entry exists.
     *
     * <p>Useful for synchronization logic to avoid duplicate inserts.</p>
     *
     * @param userId the user ID to check
     * @return true if an entry exists, false otherwise
     */
    boolean existsById(UUID userId);
}
