package org.access.domain.model;

import java.time.Instant;
import java.util.Objects;

/**
 * Aggregate root representing a password reset request.
 *
 * Manages the lifecycle of password reset tokens, including creation,
 * expiration tracking, and usage validation.
 */
public class PasswordResetRequest {
    private final PasswordResetRequestId id;
    private final UserId userId;
    private final ResetToken token;
    private final Instant expiresAt;
    private boolean isUsed;
    private final AuditInfo auditInfo;

    // Token validity duration: 15 minutes in seconds
    private static final long TOKEN_VALIDITY_SECONDS = 15 * 60;

    // -------------------- Creation --------------------

    /**
     * Private constructor for PasswordResetRequest.
     *
     * @param id unique identifier of the reset request
     * @param userId identifier of the user requesting the reset
     * @param token the reset token
     * @param expiresAt timestamp when the token expires
     * @param isUsed whether the token has been used
     * @param auditInfo audit information value object
     */
    public PasswordResetRequest(PasswordResetRequestId id, UserId userId, ResetToken token,
                                Instant expiresAt, boolean isUsed, AuditInfo auditInfo) {
        this.id = Objects.requireNonNull(id, "Id cannot be null.");
        this.userId = Objects.requireNonNull(userId, "UserId cannot be null.");
        this.token = Objects.requireNonNull(token, "Token cannot be null.");
        this.expiresAt = Objects.requireNonNull(expiresAt, "ExpiresAt cannot be null.");
        this.isUsed = isUsed;
        this.auditInfo = Objects.requireNonNull(auditInfo, "AuditInfo cannot be null.");
    }

    /**
     * Factory method to create a new password reset request.
     *
     * The token will be valid for 15 minutes from the creation time.
     *
     * @param userId identifier of the user requesting the reset
     * @param token the reset token to use
     * @return a new PasswordResetRequest instance
     */
    public static PasswordResetRequest create(UserId userId, ResetToken token) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(TOKEN_VALIDITY_SECONDS);
        AuditInfo auditInfo = AuditInfo.of(now);

        return new PasswordResetRequest(
            PasswordResetRequestId.newId(),
            userId,
            token,
            expiresAt,
            false, // Not used initially
            auditInfo
        );
    }

    // -------------------- Domain methods --------------------

    /**
     * Checks if the reset token has expired.
     *
     * @return true if the current time is after the expiration time, false otherwise
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Checks if the token can be used for password reset.
     *
     * A token can be used only if it has not been used before and has not expired.
     *
     * @return true if the token can be used, false otherwise
     */
    public boolean canBeUsed() {
        return !isUsed && !isExpired();
    }

    /**
     * Marks the token as used, preventing further use.
     *
     * @throws IllegalStateException if the token has already been used or is expired
     */
    public void markAsUsed() {
        if (isUsed) {
            throw new IllegalStateException("Token has already been used.");
        }
        if (isExpired()) {
            throw new IllegalStateException("Cannot use an expired token.");
        }
        this.isUsed = true;
        auditInfo.touch();
    }

    // -------------------- Getters --------------------

    /**
     * Gets the unique identifier of this reset request.
     *
     * @return the request ID
     */
    public PasswordResetRequestId getId() {
        return id;
    }

    /**
     * Gets the identifier of the user who requested the password reset.
     *
     * @return the user ID
     */
    public UserId getUserId() {
        return userId;
    }

    /**
     * Gets the reset token.
     *
     * @return the reset token
     */
    public ResetToken getToken() {
        return token;
    }

    /**
     * Gets the expiration timestamp of the token.
     *
     * @return the expiration instant
     */
    public Instant getExpiresAt() {
        return expiresAt;
    }

    /**
     * Checks if the token has been used.
     *
     * @return true if used, false otherwise
     */
    public boolean getIsUsed() {
        return isUsed;
    }

    /**
     * Gets the audit information.
     *
     * @return the audit info
     */
    public AuditInfo getAuditInfo() {
        return auditInfo;
    }

    // -------------------- Overrides --------------------

    /**
     * Two PasswordResetRequest instances are considered equal if they have the same ID.
     *
     * @param o the object to compare with
     * @return true if both instances have the same ID, false otherwise
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PasswordResetRequest)) return false;
        PasswordResetRequest that = (PasswordResetRequest) o;
        return id.equals(that.id);
    }

    /**
     * Computes the hash code based on the ID.
     *
     * @return hash code derived from the ID
     */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /**
     * Returns a string representation of the PasswordResetRequest.
     *
     * @return string representation
     */
    @Override
    public String toString() {
        return "PasswordResetRequest{" +
                "id=" + id +
                ", userId=" + userId +
                ", expiresAt=" + expiresAt +
                ", isUsed=" + isUsed +
                '}';
    }
}