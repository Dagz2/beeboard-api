package org.note.infrastructure.mapper;

import org.note.domain.model.*;
import org.note.infrastructure.entity.AuditInfoEmbeddable;
import org.note.infrastructure.entity.CommentEmbeddable;
import org.note.infrastructure.entity.NoteEntity;
import org.note.infrastructure.entity.RecipientEmbeddable;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
     */
    public static Note toDomain(NoteEntity entity) {
        Objects.requireNonNull(entity, "NoteEntity cannot be null");

        // Convert tag IDs
        Set<TagId> tagIds = entity.getTagIds().stream()
                .map(TagId::new)
                .collect(Collectors.toSet());

        // Convert recipients
        Set<Recipient> recipients = entity.getRecipients().stream()
                .map(RecipientMapper::toDomain)
                .collect(Collectors.toSet());

        // Convert comments
        List<Comment> comments = entity.getComments().stream()
                .map(CommentMapper::toDomain)
                .collect(Collectors.toList());

        return Note.reconstitute(
                new NoteId(entity.getNoteId()),
                new NoteTitle(entity.getTitle()),
                new NoteContent(entity.getContent(), ContentType.valueOf(entity.getContentType())),
                NoteCategory.valueOf(entity.getCategory()),
                NoteVisibility.valueOf(entity.getVisibility()),
                NoteStatus.valueOf(entity.getStatus()),
                new AuthorId(entity.getAuthorId()),
                entity.getProjectId() != null ? new ProjectId(entity.getProjectId()) : null,
                tagIds,
                recipients,
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

        // Convert tag IDs
        Set<java.util.UUID> tagIds = note.getTagIds().stream()
                .map(TagId::id)
                .collect(Collectors.toSet());
        entity.setTagIds(tagIds);

        // Convert recipients
        Set<RecipientEmbeddable> recipients = note.getRecipients().stream()
                .map(RecipientMapper::toEmbeddable)
                .collect(Collectors.toSet());
        entity.setRecipients(recipients);

        // Convert comments
        List<CommentEmbeddable> comments = note.getComments().stream()
                .map(CommentMapper::toEmbeddable)
                .collect(Collectors.toList());
        entity.setComments(comments);

        AuditInfoEmbeddable auditInfo = new AuditInfoEmbeddable(
                note.getAuditInfo().getCreatedAt(),
                note.getAuditInfo().getUpdatedAt()
        );
        entity.setAuditInfo(auditInfo);

        entity.setArchivedAt(note.getArchivedAt());

        return entity;
    }
}
