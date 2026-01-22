package org.note.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA entity representing the many-to-many relationship between Notes and Tags.
 * This is NOT an aggregate root - it exists solely to support the Note aggregate.
 */
@Entity
@Table(name = "note_tag")
@IdClass(NoteTagEntity.NoteTagId.class)
public class NoteTagEntity extends PanacheEntityBase {

    @Id
    @Column(name = "note_id", nullable = false)
    private UUID noteId;

    @Id
    @Column(name = "tag_id", nullable = false)
    private UUID tagId;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public NoteTagEntity() {
    }

    public NoteTagEntity(UUID noteId, UUID tagId, Instant createdAt) {
        this.noteId = noteId;
        this.tagId = tagId;
        this.createdAt = createdAt;
    }

    public UUID getNoteId() {
        return noteId;
    }

    public void setNoteId(UUID noteId) {
        this.noteId = noteId;
    }

    public UUID getTagId() {
        return tagId;
    }

    public void setTagId(UUID tagId) {
        this.tagId = tagId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Composite primary key class for NoteTag.
     */
    public static class NoteTagId implements Serializable {
        private UUID noteId;
        private UUID tagId;

        public NoteTagId() {
        }

        public NoteTagId(UUID noteId, UUID tagId) {
            this.noteId = noteId;
            this.tagId = tagId;
        }

        public UUID getNoteId() {
            return noteId;
        }

        public void setNoteId(UUID noteId) {
            this.noteId = noteId;
        }

        public UUID getTagId() {
            return tagId;
        }

        public void setTagId(UUID tagId) {
            this.tagId = tagId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NoteTagId that)) return false;
            return Objects.equals(noteId, that.noteId) && Objects.equals(tagId, that.tagId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(noteId, tagId);
        }
    }
}
