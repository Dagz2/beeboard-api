package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to share a note with a user.
 */
public record ShareNoteCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId,

        @NotNull(message = "User ID is required")
        UUID userId
) {
}
