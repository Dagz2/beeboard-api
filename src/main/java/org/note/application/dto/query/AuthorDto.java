package org.note.application.dto.query;

import java.util.UUID;

/**
 * DTO for author information in Note and Comment DTOs.
 *
 * <p>This DTO enriches API responses with author details, allowing clients
 * to display author names without making additional API calls.</p>
 *
 * <p>Data source: author_view table (CQRS read model synchronized from Access BC).</p>
 *
 * <h3>Design Note:</h3>
 * <p>Fields are intentionally minimal to keep response payloads small.
 * Only includes data needed for display purposes.</p>
 */
public record AuthorDto(
    UUID userId,
    String fullName
) {
    /**
     * Compact constructor with validation.
     */
    public AuthorDto {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName is required and cannot be blank");
        }
    }
}
