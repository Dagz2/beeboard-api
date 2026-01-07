package org.note.infrastructure.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Embeddable object representing a recipient of a note.
 */
@Embeddable
public class RecipientEmbeddable {

    @NotNull
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "shared_at", nullable = false)
    private Instant sharedAt;

    public RecipientEmbeddable() {
    }

    public RecipientEmbeddable(UUID userId, Instant sharedAt) {
        this.userId = userId;
        this.sharedAt = sharedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Instant getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(Instant sharedAt) {
        this.sharedAt = sharedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RecipientEmbeddable that)) return false;
        return userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }
}
