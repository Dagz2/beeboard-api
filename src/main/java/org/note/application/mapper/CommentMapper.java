package org.note.application.mapper;

import org.note.application.dto.query.AuditInfoDto;
import org.note.application.dto.query.CommentDto;
import org.note.domain.model.Comment;

/**
 * Mapper for converting Comment domain objects to DTOs.
 */
public class CommentMapper {

    private CommentMapper() {
        // Utility class
    }

    /**
     * Converts a Comment domain object to a CommentDto.
     */
    public static CommentDto toDto(Comment comment) {
        if (comment == null) {
            return null;
        }

        return new CommentDto(
                comment.getId().id(),
                comment.getAuthorId().id(),
                comment.getContent(),
                comment.getParentCommentId() != null ? comment.getParentCommentId().id() : null,
                comment.getSolutionStatus().name(),
                comment.getModerationStatus().name(),
                new AuditInfoDto(
                        comment.getAuditInfo().getCreatedAt(),
                        comment.getAuditInfo().getUpdatedAt()
                )
        );
    }
}
