package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to unshare a note with a user.
 */
public record UnshareNoteCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId,

        @NotNull(message = "User ID is required")
        UUID userId
) {
}
