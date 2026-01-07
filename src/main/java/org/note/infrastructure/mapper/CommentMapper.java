package org.note.infrastructure.mapper;

import org.note.domain.model.*;
import org.note.infrastructure.entity.AuditInfoEmbeddable;
import org.note.infrastructure.entity.CommentEmbeddable;

import java.util.Objects;

/**
 * Mapper for converting between Comment domain objects and CommentEmbeddable.
 */
public class CommentMapper {

    private CommentMapper() {
        // Utility class
    }

    /**
     * Converts a CommentEmbeddable to a Comment domain object.
     */
    public static Comment toDomain(CommentEmbeddable embeddable) {
        Objects.requireNonNull(embeddable, "CommentEmbeddable cannot be null");

        return Comment.reconstitute(
                new CommentId(embeddable.getCommentId()),
                new AuthorId(embeddable.getAuthorId()),
                embeddable.getContent(),
                embeddable.getParentCommentId() != null ? new CommentId(embeddable.getParentCommentId()) : null,
                SolutionStatus.valueOf(embeddable.getSolutionStatus()),
                ModerationStatus.valueOf(embeddable.getModerationStatus()),
                new AuditInfo(
                        embeddable.getAuditInfo().getCreatedAt(),
                        embeddable.getAuditInfo().getUpdatedAt()
                )
        );
    }

    /**
     * Converts a Comment domain object to a CommentEmbeddable.
     */
    public static CommentEmbeddable toEmbeddable(Comment comment) {
        Objects.requireNonNull(comment, "Comment cannot be null");

        AuditInfoEmbeddable auditInfo = new AuditInfoEmbeddable(
                comment.getAuditInfo().getCreatedAt(),
                comment.getAuditInfo().getUpdatedAt()
        );

        return new CommentEmbeddable(
                comment.getId().id(),
                comment.getAuthorId().id(),
                comment.getContent(),
                comment.getParentCommentId() != null ? comment.getParentCommentId().id() : null,
                comment.getSolutionStatus().name(),
                comment.getModerationStatus().name(),
                auditInfo
        );
    }
}
