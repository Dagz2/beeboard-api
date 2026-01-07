package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to archive a note.
 */
public record ArchiveNoteCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId
) {
}
