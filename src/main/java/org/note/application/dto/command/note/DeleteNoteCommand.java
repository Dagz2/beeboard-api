package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to delete a note (soft delete).
 */
public record DeleteNoteCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId
) {
}
