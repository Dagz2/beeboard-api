package org.note.application.dto.command.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Command to add a comment to a note.
 */
public record AddCommentCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId,

        @NotBlank(message = "Content is required")
        @Size(max = 5000, message = "Content must not exceed 5000 characters")
        String content,

        UUID parentCommentId  // Optional, for threaded replies
) {
}
