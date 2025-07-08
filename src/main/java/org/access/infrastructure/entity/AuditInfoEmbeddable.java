package org.access.infrastructure.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;

/**
 * Embeddable component holding audit metadata for an entity.
 *
 * This class is meant to be embedded in other entities (e.g., User, Role)
 * to track creation and modification timestamps without duplicating fields.
 *
 * It maps two columns in the parent table: {@code created_at} and {@code updated_at}.
 */
@Embeddable
public class AuditInfoEmbeddable {

    /**
     * Timestamp indicating when the entity was created.
     */
    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    /**
     * Timestamp indicating the last time the entity was updated.
     */
    @NotNull
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // -------------------- Getters & Setters --------------------

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
