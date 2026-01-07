package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to change a note's visibility.
 */
public record ChangeNoteVisibilityCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId,

        @NotNull(message = "Visibility is required")
        String visibility,

        UUID projectId  // Required when visibility is PROJECT
) {
}
