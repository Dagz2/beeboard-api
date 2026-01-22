package org.note.exposition.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.note.domain.model.AuthorView;
import org.note.domain.model.NoteUserContext;
import org.note.domain.repository.AuthorViewRepository;

import java.util.UUID;

/**
 * Service to retrieve NoteUserContext for authorization decisions.
 *
 * <p>This service queries the local author_view CQRS read model instead of
 * calling Access BC via REST. No caching needed since queries are local and fast (&lt;5ms).</p>
 *
 * <p>The Anti-Corruption Layer translation (ApplicationRole → AccessLevel) has
 * already been done by {@link org.note.application.event.UserEventHandler} when
 * storing data in author_view. This service simply retrieves the translated data.</p>
 *
 * <h3>Architecture Evolution:</h3>
 * <ul>
 *   <li><b>Old</b>: JWT → REST call to Access BC → Cache → ACL translation → NoteUserContext</li>
 *   <li><b>New</b>: JWT → Local DB query (author_view) → NoteUserContext</li>
 * </ul>
 *
 * <h3>Benefits:</h3>
 * <ul>
 *   <li><b>Performance</b>: &lt;5ms local query vs 50-200ms REST call</li>
 *   <li><b>Resilience</b>: Works even if Access BC is down</li>
 *   <li><b>Simplicity</b>: No cache management, no ACL translation at query time</li>
 * </ul>
 */
@ApplicationScoped
public class NoteUserContextService {

    @Inject
    JsonWebToken jwt;

    @Inject
    AuthorViewRepository authorViewRepository;

    /**
     * Gets the NoteUserContext for the current authenticated user.
     *
     * <p>Extracts userId from JWT and queries the local author_view table.</p>
     *
     * <p>This method is a convenience wrapper around {@link #getContextForUser(UUID)}
     * that extracts the userId from the current JWT token.</p>
     *
     * @return NoteUserContext for the current user
     * @throws IllegalStateException if no authenticated user or user not found in author_view
     */
    public NoteUserContext getCurrentContext() {
        if (jwt == null || jwt.getSubject() == null) {
            throw new IllegalStateException("No authenticated user");
        }

        // Extract userId from JWT (authentication mechanism)
        UUID userId = UUID.fromString(jwt.getSubject());

        // Delegate to query method
        return getContextForUser(userId);
    }

    /**
     * Gets the NoteUserContext for a specific user.
     *
     * <p>Queries local author_view table. No caching needed (query is &lt;5ms).</p>
     *
     * <p>The AccessLevel in author_view has already been translated from ApplicationRole
     * by {@link org.note.application.event.UserEventHandler}, so we just need to
     * retrieve and convert to NoteUserContext.</p>
     *
     * <h3>Flow:</h3>
     * <pre>
     * 1. Query author_view by userId (local DB, &lt;5ms)
     * 2. Convert AuthorView → NoteUserContext (already has AccessLevel)
     * 3. Return NoteUserContext (ready for authorization)
     * </pre>
     *
     * @param userId the user ID to fetch context for
     * @return NoteUserContext for the specified user
     * @throws IllegalStateException if user not found in author_view
     */
    public NoteUserContext getContextForUser(UUID userId) {
        // Query local author_view (CQRS read model)
        AuthorView authorView = authorViewRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException(
                "User not found in author_view: " + userId +
                ". The user may not exist or author_view is out of sync. " +
                "Run POST /api/v1/internal/note/sync-author-view to resynchronize."
            ));

        // AuthorView already has AccessLevel (translated in event handlers)
        // Just convert to NoteUserContext
        return authorView.toUserContext();
    }
}
