package org.note.exposition.dto;

/**
 * Standard error response DTO for API errors.
 */
public record ErrorResponse(
        String errorCode,
        String message
) {
}
