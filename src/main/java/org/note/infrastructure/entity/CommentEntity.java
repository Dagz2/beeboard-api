package org.note.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * JPA entity representing a Comment within the Note aggregate.
 *
 * <p><strong>DDD Pattern:</strong> Comments are entities within the Note aggregate boundary.
 * They are NOT aggregate roots and their lifecycle is controlled by the Note aggregate.
 * The ON DELETE CASCADE foreign key enforces this relationship at the database level.
 *
 * <p><strong>Design Decision:</strong> This entity does NOT use JPA relationships (@ManyToOne)
 * to reference the parent Note or parent Comment. Instead, it stores only the UUIDs (noteId, parentCommentId).
 * This keeps the design simple and avoids lazy loading complexities.
 */
@Entity
@Table(name = "comment")
public class CommentEntity extends PanacheEntityBase {

    @Id
    @Column(name = "comment_id", nullable = false)
    private UUID commentId;

    /**
     * Foreign key to the parent Note.
     * CASCADE DELETE ensures comments are deleted when the note is deleted.
     */
    @NotNull
    @Column(name = "note_id", nullable = false)
    private UUID noteId;

    @NotNull
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @NotNull
    @Column(name = "content", length = 5000, nullable = false)
    private String content;

    /**
     * Optional reference to a parent comment for threading (replies).
     * Null if this is a root-level comment.
     */
    @Column(name = "parent_comment_id")
    private UUID parentCommentId;

    @NotNull
    @Column(name = "solution_status", length = 20, nullable = false)
    private String solutionStatus;

    @NotNull
    @Column(name = "moderation_status", length = 20, nullable = false)
    private String moderationStatus;

    @Embedded
    @NotNull
    private AuditInfoEmbeddable auditInfo;

    // Default constructor for JPA
    public CommentEntity() {
    }

    // Getters and Setters

    public UUID getCommentId() {
        return commentId;
    }

    public void setCommentId(UUID commentId) {
        this.commentId = commentId;
    }

    public UUID getNoteId() {
        return noteId;
    }

    public void setNoteId(UUID noteId) {
        this.noteId = noteId;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UUID getParentCommentId() {
        return parentCommentId;
    }

    public void setParentCommentId(UUID parentCommentId) {
        this.parentCommentId = parentCommentId;
    }

    public String getSolutionStatus() {
        return solutionStatus;
    }

    public void setSolutionStatus(String solutionStatus) {
        this.solutionStatus = solutionStatus;
    }

    public String getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(String moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public AuditInfoEmbeddable getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoEmbeddable auditInfo) {
        this.auditInfo = auditInfo;
    }
}
