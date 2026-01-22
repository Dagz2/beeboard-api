package org.note.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for Note aggregate.
 * Tags, recipients, and comments are stored in separate tables with CASCADE DELETE.
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

    // Tags and recipients are now stored in separate tables (note_tag, note_recipient)
    // and loaded separately by the repository

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
