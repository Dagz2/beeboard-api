package org.note.application.usecase.command.tag;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.note.application.dto.command.tag.CreateTagCommand;
import org.note.domain.model.NoteUserContext;
import org.note.domain.model.Tag;
import org.note.domain.model.TagName;
import org.note.domain.repository.TagRepository;

/**
 * Use case for creating a new tag.
 */
@ApplicationScoped
public class CreateTag {

    @Inject
    TagRepository tagRepository;

    /**
     * Executes the create tag command.
     *
     * @param command the command
     * @param userContext the user context for audit trail
     */
    @Transactional
    public void execute(CreateTagCommand command, NoteUserContext userContext) {
        // Check if tag already exists
        TagName tagName = new TagName(command.name());
        if (tagRepository.existsByName(tagName)) {
            throw new IllegalArgumentException("Tag with name '" + command.name() + "' already exists");
        }

        // Create tag
        Tag tag = Tag.create(tagName);

        // Persist
        tagRepository.save(tag);
    }
}
