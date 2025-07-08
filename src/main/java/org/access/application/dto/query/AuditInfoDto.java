package org.access.application.dto.query;

import java.time.Instant;

/**
 * Data Transfer Object representing audit metadata for an entity.
 *
 * This DTO encapsulates the creation and last update timestamps of a resource.
 * It is commonly used to expose audit information in read/query responses,
 * especially in entities like User, Role, or Permission.
 */
public class AuditInfoDto {

    /**
     * The timestamp when the entity was originally created.
     */
    public Instant createdAt;

    /**
     * The timestamp when the entity was last updated.
     */
    public Instant updatedAt;

    /**
     * Default no-args constructor for frameworks and serialization.
     */
    public AuditInfoDto() {
    }

    /**
     * Full constructor to create a populated audit info object.
     *
     * @param createdAt  the creation timestamp
     * @param updatedAt  the last update timestamp
     */
    public AuditInfoDto(Instant createdAt, Instant updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    /**
     * Gets the creation timestamp.
     *
     * @return the Instant when the entity was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the creation timestamp.
     *
     * @param createdAt the Instant to set as creation time
     */
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Gets the last update timestamp.
     *
     * @return the Instant when the entity was last updated
     */
    public Instant getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the last update timestamp.
     *
     * @param updatedAt the Instant to set as last update time
     */
    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
