package org.note.application.dto.command.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Command to rename an existing tag.
 */
public record RenameTagCommand(
        @NotNull(message = "Tag ID is required")
        UUID tagId,

        @NotBlank(message = "New tag name is required")
        @Size(max = 50, message = "Tag name must not exceed 50 characters")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Tag name must be lowercase with only letters, numbers, and hyphens")
        String newName
) {
}
