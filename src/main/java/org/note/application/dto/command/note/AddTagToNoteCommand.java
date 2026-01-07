package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to add a tag to a note.
 */
public record AddTagToNoteCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId,

        @NotNull(message = "Tag ID is required")
        UUID tagId
) {
}
