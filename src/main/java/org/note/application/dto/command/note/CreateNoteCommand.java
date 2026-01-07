package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Command to create a new note.
 */
public record CreateNoteCommand(
        @NotBlank(message = "Title is required")
        @Size(max = 200, message = "Title must not exceed 200 characters")
        String title,

        @NotBlank(message = "Content is required")
        @Size(max = 50000, message = "Content must not exceed 50000 characters")
        String content,

        @NotNull(message = "Content type is required")
        String contentType,

        @NotNull(message = "Category is required")
        String category,

        @NotNull(message = "Visibility is required")
        String visibility,

        UUID projectId  // Optional, required only for PROJECT visibility
) {
}
