package org.note.application.mapper;

import org.note.application.dto.query.AuditInfoDto;
import org.note.application.dto.query.NoteDto;
import org.note.application.dto.query.RecipientDto;
import org.note.domain.model.Note;
import org.note.domain.model.TagId;

import java.util.stream.Collectors;

/**
 * Mapper for converting Note domain objects to DTOs.
 */
public class NoteMapper {

    private NoteMapper() {
        // Utility class
    }

    /**
     * Converts a Note domain object to a NoteDto.
     */
    public static NoteDto toDto(Note note) {
        if (note == null) {
            return null;
        }

        return new NoteDto(
                note.getId().id(),
                note.getTitle().value(),
                note.getContent().body(),
                note.getContent().type().name(),
                note.getCategory().name(),
                note.getVisibility().name(),
                note.getStatus().name(),
                note.getAuthorId().id(),
                note.getProjectId() != null ? note.getProjectId().id() : null,
                note.getTagIds().stream()
                        .map(TagId::id)
                        .collect(Collectors.toSet()),
                note.getRecipients().stream()
                        .map(r -> new RecipientDto(r.userId().id(), r.sharedAt()))
                        .collect(Collectors.toSet()),
                note.getComments().stream()
                        .map(CommentMapper::toDto)
                        .collect(Collectors.toList()),
                new AuditInfoDto(
                        note.getAuditInfo().getCreatedAt(),
                        note.getAuditInfo().getUpdatedAt()
                ),
                note.getArchivedAt()
        );
    }
}
