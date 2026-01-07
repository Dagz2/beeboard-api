package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Command to update an existing note's title and content.
 */
public record UpdateNoteCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId,

        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Content is required")
        @Size(max = 50000, message = "Content must not exceed 50000 characters")
        String content,

        @NotNull(message = "Content type is required")
        String contentType
) {
}
