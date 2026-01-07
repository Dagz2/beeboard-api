package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to remove a tag from a note.
 */
public record RemoveTagFromNoteCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId,

        @NotNull(message = "Tag ID is required")
        UUID tagId
) {
}
