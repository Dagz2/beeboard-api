package org.note.infrastructure.mapper;

import org.note.domain.model.*;
import org.note.infrastructure.entity.AuditInfoEmbeddable;
import org.note.infrastructure.entity.CommentEntity;

import java.util.Objects;

/**
 * Mapper for converting between Comment domain objects and CommentEntity.
 *
 * <p>This mapper handles the transformation between the domain layer (Comment)
 * and the infrastructure layer (CommentEntity) while preserving all business
 * information including solution status, moderation status, and threading.
 */
public class CommentMapper {

    private CommentMapper() {
        // Utility class
    }

    /**
     * Converts a CommentEntity to a Comment domain object.
     *
     * @param entity the JPA entity from database
     * @return the reconstituted domain Comment
     */
    public static Comment toDomain(CommentEntity entity) {
        Objects.requireNonNull(entity, "CommentEntity cannot be null");

        return Comment.reconstitute(
                new CommentId(entity.getCommentId()),
                new AuthorId(entity.getAuthorId()),
                entity.getContent(),
                entity.getParentCommentId() != null ? new CommentId(entity.getParentCommentId()) : null,
                SolutionStatus.valueOf(entity.getSolutionStatus()),
                ModerationStatus.valueOf(entity.getModerationStatus()),
                new AuditInfo(
                        entity.getAuditInfo().getCreatedAt(),
                        entity.getAuditInfo().getUpdatedAt()
                )
        );
    }

    /**
     * Converts a Comment domain object to a CommentEntity.
     *
     * <p><strong>NOTE:</strong> The noteId must be set by the caller after
     * this method returns, as the Comment domain object doesn't store the
     * parent note ID (it's managed by the Note aggregate).
     *
     * @param comment the domain Comment
     * @return the JPA entity ready for persistence
     */
    public static CommentEntity toEntity(Comment comment) {
        Objects.requireNonNull(comment, "Comment cannot be null");

        CommentEntity entity = new CommentEntity();
        entity.setCommentId(comment.getId().id());
        // NOTE: noteId must be set by the repository layer
        entity.setAuthorId(comment.getAuthorId().id());
        entity.setContent(comment.getContent());
        entity.setParentCommentId(comment.getParentCommentId() != null ? comment.getParentCommentId().id() : null);
        entity.setSolutionStatus(comment.getSolutionStatus().name());
        entity.setModerationStatus(comment.getModerationStatus().name());

        AuditInfoEmbeddable auditInfo = new AuditInfoEmbeddable(
                comment.getAuditInfo().getCreatedAt(),
                comment.getAuditInfo().getUpdatedAt()
        );
        entity.setAuditInfo(auditInfo);

        return entity;
    }
}
