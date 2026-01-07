package org.note.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.*;

/**
 * JPA entity for Note aggregate.
 * Contains embedded collections for comments, tags, and recipients.
 */
@Entity
@Table(name = "note")
public class NoteEntity extends PanacheEntityBase {

    @Id
    @Column(name = "note_id", nullable = false)
    private UUID noteId;

    @NotNull
    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @NotNull
    @Column(name = "content", length = 50000, nullable = false)
    private String content;

    @NotNull
    @Column(name = "content_type", length = 20, nullable = false)
    private String contentType;

    @NotNull
    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @NotNull
    @Column(name = "visibility", length = 20, nullable = false)
    private String visibility;

    @NotNull
    @Column(name = "status", length = 20, nullable = false)
    private String status;

    @NotNull
    @Column(name = "author_id", nullable = false)
    private UUID authorId;

    @Column(name = "project_id")
    private UUID projectId;

    // Store tag IDs as JSONB array
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tag_ids", columnDefinition = "jsonb")
    private Set<UUID> tagIds = new HashSet<>();

    // Store recipients as JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "recipients", columnDefinition = "jsonb")
    private Set<RecipientEmbeddable> recipients = new HashSet<>();

    // Store comments as JSONB
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "comments", columnDefinition = "jsonb")
    private List<CommentEmbeddable> comments = new ArrayList<>();

    @Embedded
    @NotNull
    private AuditInfoEmbeddable auditInfo;

    @Column(name = "archived_at")
    private Instant archivedAt;

    public NoteEntity() {
    }

    public UUID getNoteId() {
        return noteId;
    }

    public void setNoteId(UUID noteId) {
        this.noteId = noteId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public UUID getAuthorId() {
        return authorId;
    }

    public void setAuthorId(UUID authorId) {
        this.authorId = authorId;
    }

    public UUID getProjectId() {
        return projectId;
    }

    public void setProjectId(UUID projectId) {
        this.projectId = projectId;
    }

    public Set<UUID> getTagIds() {
        return tagIds;
    }

    public void setTagIds(Set<UUID> tagIds) {
        this.tagIds = tagIds;
    }

    public Set<RecipientEmbeddable> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<RecipientEmbeddable> recipients) {
        this.recipients = recipients;
    }

    public List<CommentEmbeddable> getComments() {
        return comments;
    }

    public void setComments(List<CommentEmbeddable> comments) {
        this.comments = comments;
    }

    public AuditInfoEmbeddable getAuditInfo() {
        return auditInfo;
    }

    public void setAuditInfo(AuditInfoEmbeddable auditInfo) {
        this.auditInfo = auditInfo;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }
}
