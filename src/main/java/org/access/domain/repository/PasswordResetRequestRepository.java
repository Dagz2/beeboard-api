package org.access.domain.repository;

import org.access.domain.model.PasswordResetRequest;
import org.access.domain.model.ResetToken;
import org.access.domain.model.UserId;

import java.util.Optional;

/**
 * Repository interface for accessing and persisting {@link PasswordResetRequest} aggregates.
 *
 * This is a Domain-Driven Design (DDD) style repository, representing
 * the contract for retrieving and storing {@code PasswordResetRequest} aggregates without
 * exposing infrastructure details (like JPA or database logic).
 */
public interface PasswordResetRequestRepository {

    /**
     * Retrieves a password reset request by its token.
     *
     * @param token the reset token
     * @return an {@link Optional} containing the request if found, or empty if not found
     */
    Optional<PasswordResetRequest> findByToken(ResetToken token);

    /**
     * Saves a new password reset request.
     *
     * @param request the password reset request to persist
     */
    void save(PasswordResetRequest request);

    /**
     * Updates an existing password reset request.
     *
     * @param request the password reset request to update
     */
    void update(PasswordResetRequest request);

    /**
     * Invalidates (marks as used) all active password reset tokens for a given user.
     * This is typically called when creating a new reset request to ensure only the latest token is valid.
     *
     * @param userId the user ID for which to invalidate all tokens
     */
    void invalidateAllForUser(UserId userId);

    /**
     * Deletes all expired and used password reset tokens.
     * This is typically called by a scheduled cleanup job to maintain database hygiene.
     */
    void deleteExpiredTokens();
}
