package org.note.application.dto.query;

import java.time.Instant;

/**
 * DTO for audit information.
 */
public class AuditInfoDto {
    private Instant createdAt;
    private Instant updatedAt;

    public AuditInfoDto() {
    }

    public AuditInfoDto(Instant createdAt, Instant updatedAt) {
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

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
