package org.note.exposition.service;

import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.note.domain.model.NoteUserContext;

import java.util.UUID;

/**
 * Service to extract and build NoteUserContext from JWT token.
 * Uses caching to reduce lookups (15-minute TTL).
 */
@ApplicationScoped
public class NoteUserContextService {

    @Inject
    JsonWebToken jwt;

    /**
     * Gets the NoteUserContext for the current authenticated user.
     * Extracts information from JWT claims and translates to Note BC vocabulary.
     *
     * @return NoteUserContext for the current user
     */
    @CacheResult(cacheName = "note-user-context")
    public NoteUserContext getCurrentContext() {
        if (jwt == null || jwt.getSubject() == null) {
            throw new IllegalStateException("No authenticated user");
        }

        // Extract from JWT claims
        UUID userId = UUID.fromString(jwt.getSubject());
        String externalRole = jwt.getClaim("roleName");  // e.g., "ADMIN", "MANAGER", "MEMBER"
        String externalAuthType = jwt.getClaim("authType");  // "INTERNAL" or "EXTERNAL"

        // Translate to Note BC vocabulary via anti-corruption layer
        return NoteUserContext.from(userId, externalRole, externalAuthType);
    }

    /**
     * Invalidates the cached context for a specific user.
     * Should be called when user's role or permissions change.
     *
     * @param userId the user ID
     */
    public void invalidate(UUID userId) {
        // Cache invalidation is handled by Caffeine cache expiration
        // This method is a placeholder for explicit invalidation if needed
    }
}
