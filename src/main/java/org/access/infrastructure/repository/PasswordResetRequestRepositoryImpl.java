package org.access.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import org.access.domain.model.PasswordResetRequest;
import org.access.domain.model.ResetToken;
import org.access.domain.model.UserId;
import org.access.domain.repository.PasswordResetRequestRepository;
import org.access.infrastructure.entity.PasswordResetRequestEntity;
import org.access.infrastructure.mapper.PasswordResetRequestMapper;

import java.time.Instant;
import java.util.Optional;

/**
 * JPA-based implementation of the {@link PasswordResetRequestRepository} interface using Panache.
 *
 * This class provides persistence logic for {@link PasswordResetRequest} aggregate roots,
 * delegating object-relational mapping to {@link PasswordResetRequestMapper}.
 * It extends {@link PanacheRepository} for convenient database access methods.
 */
@ApplicationScoped
public class PasswordResetRequestRepositoryImpl
    implements PasswordResetRequestRepository, PanacheRepository<PasswordResetRequestEntity> {

    /**
     * Retrieves a password reset request by its token.
     *
     * The token is hashed before querying the database since tokens are stored
     * as SHA-256 hashes for security.
     *
     * @param token the reset token
     * @return an {@link Optional} containing the request if found, or empty if not found
     */
    @Override
    public Optional<PasswordResetRequest> findByToken(ResetToken token) {
        String tokenHash = token.hash();
        return find("tokenHash", tokenHash)
            .firstResultOptional()
            .map(PasswordResetRequestMapper::toDomain);
    }

    /**
     * Saves a new password reset request to the database.
     *
     * @param request the password reset request to persist
     */
    @Override
    public void save(PasswordResetRequest request) {
        persist(PasswordResetRequestMapper.toEntity(request));
    }

    /**
     * Updates an existing password reset request in the database.
     *
     * @param request the password reset request to update
     */
    @Override
    public void update(PasswordResetRequest request) {
        PasswordResetRequestEntity entity = PasswordResetRequestMapper.toEntity(request);
        getEntityManager().merge(entity);
    }

    /**
     * Invalidates (marks as used) all active password reset tokens for a given user.
     *
     * This ensures that when a new reset request is created, all previous tokens
     * for the same user become invalid.
     *
     * @param userId the user ID for which to invalidate all tokens
     */
    @Override
    public void invalidateAllForUser(UserId userId) {
        update("isUsed = true WHERE userId = ?1 AND isUsed = false", userId.id());
    }

    /**
     * Deletes all expired and used password reset tokens from the database.
     *
     * A token is considered deletable if:
     * - It has been used, OR
     * - It has expired (expiresAt is in the past)
     *
     * This method is typically called by a scheduled cleanup job.
     */
    @Override
    public void deleteExpiredTokens() {
        Instant now = Instant.now();
        delete("isUsed = true OR expiresAt < ?1", now);
    }
}
