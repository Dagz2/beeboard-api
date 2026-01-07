package org.note.application.dto.command.note;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Command to change a note's category.
 */
public record ChangeNoteCategoryCommand(
        @NotNull(message = "Note ID is required")
        UUID noteId,

        @NotNull(message = "Category is required")
        String category
) {
}
