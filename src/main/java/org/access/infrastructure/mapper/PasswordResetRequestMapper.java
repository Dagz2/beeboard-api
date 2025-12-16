package org.access.infrastructure.mapper;

import org.access.domain.model.*;
import org.access.infrastructure.entity.PasswordResetRequestEntity;
import org.access.infrastructure.entity.AuditInfoEmbeddable;

import java.util.Objects;

/**
 * Mapper responsible for converting between {@link PasswordResetRequestEntity} (infrastructure)
 * and {@link PasswordResetRequest} (domain model).
 *
 * This class ensures isolation between the domain layer and the persistence layer,
 * handling the conversion between technical representations (JPA) and business logic objects.
 *
 * Note: The token is stored as a SHA-256 hash in the database for security.
 * When reconstructing from the entity, the hash is used to create a ResetToken object.
 */
public class PasswordResetRequestMapper {

    /**
     * Converts a JPA {@link PasswordResetRequestEntity} to a domain {@link PasswordResetRequest} aggregate.
     *
     * Note: The token in the returned object represents the hash, not the original token value,
     * since the original token is not stored in the database.
     *
     * @param entity the PasswordResetRequestEntity from the persistence layer
     * @return the domain PasswordResetRequest object
     * @throws NullPointerException if the input entity is null
     */
    public static PasswordResetRequest toDomain(PasswordResetRequestEntity entity) {
        Objects.requireNonNull(entity, "PasswordResetRequestEntity cannot be null");

        // Note: We reconstruct a ResetToken from the hash.
        // This is acceptable since SHA-256 produces a 64-character hex string,
        // which matches the ResetToken format.
        ResetToken token = new ResetToken(entity.getTokenHash());

        return new PasswordResetRequest(
            new PasswordResetRequestId(entity.getRequestId()),
            new UserId(entity.getUserId()),
            token,
            entity.getExpiresAt(),
            entity.getIsUsed(),
            new AuditInfo(
                entity.getAuditInfo().getCreatedAt(),
                entity.getAuditInfo().getUpdatedAt()
            )
        );
    }

    /**
     * Converts a domain {@link PasswordResetRequest} to a JPA {@link PasswordResetRequestEntity}.
     *
     * The token is hashed using SHA-256 before being stored in the entity.
     *
     * @param request the domain PasswordResetRequest aggregate
     * @return the infrastructure-level PasswordResetRequestEntity
     * @throws NullPointerException if the input request is null
     */
    public static PasswordResetRequestEntity toEntity(PasswordResetRequest request) {
        Objects.requireNonNull(request, "PasswordResetRequest cannot be null");

        PasswordResetRequestEntity entity = new PasswordResetRequestEntity();
        entity.setRequestId(request.getId().id());
        entity.setUserId(request.getUserId().id());

        // Hash the token before storing
        entity.setTokenHash(request.getToken().hash());

        entity.setExpiresAt(request.getExpiresAt());
        entity.setIsUsed(request.getIsUsed());

        AuditInfoEmbeddable audit = new AuditInfoEmbeddable();
        audit.setCreatedAt(request.getAuditInfo().getCreatedAt());
        audit.setUpdatedAt(request.getAuditInfo().getUpdatedAt());
        entity.setAuditInfo(audit);

        return entity;
    }
}
