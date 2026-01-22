package org.note.infrastructure.mapper;

import org.note.domain.model.*;
import org.note.infrastructure.entity.AuditInfoEmbeddable;
import org.note.infrastructure.entity.NoteEntity;
import org.note.infrastructure.entity.RecipientEmbeddable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Note domain objects and NoteEntity.
 */
public class NoteMapper {

    private NoteMapper() {
        // Utility class
    }

    /**
     * Converts a NoteEntity to a Note domain object.
     * Tags and recipients are loaded separately by the repository and passed as parameters.
     */
    public static Note toDomain(NoteEntity entity, Set<UUID> tagIds, Set<RecipientEmbeddable> recipients) {
        Objects.requireNonNull(entity, "NoteEntity cannot be null");
        Objects.requireNonNull(tagIds, "tagIds cannot be null");
        Objects.requireNonNull(recipients, "recipients cannot be null");

        // Convert tag IDs
        Set<TagId> convertedTagIds = tagIds.stream()
                .map(TagId::new)
                .collect(Collectors.toSet());

        // Convert recipients
        Set<Recipient> convertedRecipients = recipients.stream()
                .map(RecipientMapper::toDomain)
                .collect(Collectors.toSet());

        // Comments are loaded separately by the repository
        List<Comment> comments = new ArrayList<>();

        return Note.reconstitute(
                new NoteId(entity.getNoteId()),
                new NoteTitle(entity.getTitle()),
                new NoteContent(entity.getContent(), ContentType.valueOf(entity.getContentType())),
                NoteCategory.valueOf(entity.getCategory()),
                NoteVisibility.valueOf(entity.getVisibility()),
                NoteStatus.valueOf(entity.getStatus()),
                new AuthorId(entity.getAuthorId()),
                entity.getProjectId() != null ? new ProjectId(entity.getProjectId()) : null,
                convertedTagIds,
                convertedRecipients,
                comments,
                new AuditInfo(
                        entity.getAuditInfo().getCreatedAt(),
                        entity.getAuditInfo().getUpdatedAt()
                ),
                entity.getArchivedAt()
        );
    }

    /**
     * Converts a Note domain object to a NoteEntity.
     */
    public static NoteEntity toEntity(Note note) {
        Objects.requireNonNull(note, "Note cannot be null");

        NoteEntity entity = new NoteEntity();
        entity.setNoteId(note.getId().id());
        entity.setTitle(note.getTitle().value());
        entity.setContent(note.getContent().body());
        entity.setContentType(note.getContent().type().name());
        entity.setCategory(note.getCategory().name());
        entity.setVisibility(note.getVisibility().name());
        entity.setStatus(note.getStatus().name());
        entity.setAuthorId(note.getAuthorId().id());
        entity.setProjectId(note.getProjectId() != null ? note.getProjectId().id() : null);

        // Tags, recipients, and comments are persisted separately by the repository

        AuditInfoEmbeddable auditInfo = new AuditInfoEmbeddable(
                note.getAuditInfo().getCreatedAt(),
                note.getAuditInfo().getUpdatedAt()
        );
        entity.setAuditInfo(auditInfo);

        entity.setArchivedAt(note.getArchivedAt());

        return entity;
    }
}
