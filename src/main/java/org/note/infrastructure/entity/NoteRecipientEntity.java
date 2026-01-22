package org.note.infrastructure.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * JPA entity representing recipients of a note.
 * This is a table representation of the Recipient value object within the Note aggregate.
 */
@Entity
@Table(name = "note_recipient")
@IdClass(NoteRecipientEntity.NoteRecipientId.class)
public class NoteRecipientEntity extends PanacheEntityBase {

    @Id
    @Column(name = "note_id", nullable = false)
    private UUID noteId;

    @Id
    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @NotNull
    @Column(name = "shared_at", nullable = false)
    private Instant sharedAt;

    public NoteRecipientEntity() {
    }

    public NoteRecipientEntity(UUID noteId, UUID userId, Instant sharedAt) {
        this.noteId = noteId;
        this.userId = userId;
        this.sharedAt = sharedAt;
    }

    public UUID getNoteId() {
        return noteId;
    }

    public void setNoteId(UUID noteId) {
        this.noteId = noteId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public Instant getSharedAt() {
        return sharedAt;
    }

    public void setSharedAt(Instant sharedAt) {
        this.sharedAt = sharedAt;
    }

    /**
     * Composite primary key class for NoteRecipient.
     * Equality based on both note_id and user_id to prevent duplicate entries.
     */
    public static class NoteRecipientId implements Serializable {
        private UUID noteId;
        private UUID userId;

        public NoteRecipientId() {
        }

        public NoteRecipientId(UUID noteId, UUID userId) {
            this.noteId = noteId;
            this.userId = userId;
        }

        public UUID getNoteId() {
            return noteId;
        }

        public void setNoteId(UUID noteId) {
            this.noteId = noteId;
        }

        public UUID getUserId() {
            return userId;
        }

        public void setUserId(UUID userId) {
            this.userId = userId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NoteRecipientId that)) return false;
            return Objects.equals(noteId, that.noteId) && Objects.equals(userId, that.userId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(noteId, userId);
        }
    }
}
