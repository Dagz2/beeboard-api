package org.note.application.dto.command.tag;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Command to create a new tag.
 */
public record CreateTagCommand(
        @NotBlank(message = "Tag name is required")
        @Size(max = 50, message = "Tag name must not exceed 50 characters")
        @Pattern(regexp = "^[a-z0-9-]+$", message = "Tag name must be lowercase with only letters, numbers, and hyphens")
        String name
) {
}
