package org.note.application.dto.command.tag;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to delete a tag.
 * Tag must be unused (usageCount = 0) to be deleted.
 */
public record DeleteTagCommand(
        @NotNull(message = "Tag ID is required")
        UUID tagId
) {
}
