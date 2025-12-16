package org.access.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity representing a password reset request in the system.
 *
 * Maps to the "password_reset_tokens" table and holds token-related data including
 * the hashed token, expiration time, usage status, and audit information.
 *
 * This entity is part of the infrastructure layer and mirrors the domain aggregate
 * {@code PasswordResetRequest}.
 */
@Entity
@Table(name = "password_reset_tokens")
public class PasswordResetRequestEntity extends PanacheEntityBase {

    /**
     * Unique identifier for the password reset request.
     */
    @Id
    @NotNull
    @Column(name = "request_id", nullable = false)
    private UUID requestId;

    /**
     * Identifier of the user who requested the password reset.
     * Stored as UUID to maintain aggregate boundary (DDD principle).
     */
    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    /**
     * SHA-256 hash of the reset token.
     * Stored as hash instead of plaintext for security.
     */
    @NotNull
    @Column(name = "token_hash", length = 64, nullable = false, unique = true)
    private String tokenHash;

    /**
     * Timestamp when the reset token expires.
     */
    @NotNull
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /**
     * Whether the reset token has been used.
     */
    @NotNull
    @Column(name = "is_used", nullable = false)
    private Boolean isUsed;

    /**
     * Audit information including creation and last modification timestamps.
     */
    @Embedded
    @NotNull
    private AuditInfoEmbeddable auditInfo;

    // -------------------- Getters & Setters --------------------

    public UUID getRequestId() {
        return requestId;
    }

    public void setRequestId(UUID requestId) {
        this.requestId = requestId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public AuditInfoEmbeddable getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoEmbeddable auditInfo) {
        this.auditInfo = auditInfo;
    }
}
