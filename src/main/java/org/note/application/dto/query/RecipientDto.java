package org.note.application.dto.query;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for Recipient information.
 */
public class RecipientDto {
    private UUID userId;
    private Instant sharedAt;

    public RecipientDto() {
    }

    public RecipientDto(UUID userId, Instant sharedAt) {
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
}
