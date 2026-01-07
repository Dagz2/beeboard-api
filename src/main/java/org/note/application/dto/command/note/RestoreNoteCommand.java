package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to restore an archived note.
 */
public record RestoreNoteCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId
) {
}
