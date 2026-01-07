package org.note.application.dto.command.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Command to edit an existing comment.
 */
public record EditCommentCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId,

        @NotNull(message = "Comment ID is required")
        UUID commentId,

        @NotBlank(message = "Content is required")
        @Size(max = 5000, message = "Content must not exceed 5000 characters")
        String content
) {
}
