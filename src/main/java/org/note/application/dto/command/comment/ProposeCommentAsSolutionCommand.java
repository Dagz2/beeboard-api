package org.note.application.dto.command.comment;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to propose a comment as a solution.
 */
public record ProposeCommentAsSolutionCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId,

        @NotNull(message = "Comment ID is required")
        UUID commentId
) {
}
