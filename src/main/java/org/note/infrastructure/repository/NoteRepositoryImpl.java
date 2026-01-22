package org.note.infrastructure.repository;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.infrastructure.entity.*;
import org.note.infrastructure.mapper.CommentMapper;
import org.note.infrastructure.mapper.NoteMapper;
import org.note.infrastructure.mapper.RecipientMapper;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * JPA/Panache implementation of NoteRepository.
 * Manages Note aggregates with separate comment persistence.
 */
@ApplicationScoped
public class NoteRepositoryImpl implements NoteRepository, PanacheRepository<NoteEntity> {

    @Override
    public Optional<Note> findById(NoteId id) {
        Optional<NoteEntity> entityOpt = find("noteId", id.id()).firstResultOptional();
        if (entityOpt.isEmpty()) {
            return Optional.empty();
        }

        NoteEntity entity = entityOpt.get();

        // Load collections separately
        Set<java.util.UUID> tagIds = loadTagIdsForNote(id);
        Set<RecipientEmbeddable> recipients = loadRecipientsForNote(id);
        List<Comment> comments = loadCommentsForNote(id);

        Note note = NoteMapper.toDomain(entity, tagIds, recipients);
        comments.forEach(note::addComment);

        return Optional.of(note);
    }

    @Override
    public List<Note> findByAuthorId(AuthorId authorId) {
        return find("authorId", authorId.id()).stream()
                .map(this::toDomainWithComments)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findByCategory(NoteCategory category) {
        return find("category", category.name()).stream()
                .map(this::toDomainWithComments)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findByVisibility(NoteVisibility visibility) {
        return find("visibility", visibility.name()).stream()
                .map(this::toDomainWithComments)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findByProjectId(ProjectId projectId) {
        return find("projectId", projectId.id()).stream()
                .map(this::toDomainWithComments)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findByTagId(TagId tagId) {
        return getEntityManager()
                .createQuery("SELECT DISTINCT n FROM NoteEntity n " +
                        "JOIN NoteTagEntity nt ON n.noteId = nt.noteId " +
                        "WHERE nt.tagId = :tagId", NoteEntity.class)
                .setParameter("tagId", tagId.id())
                .getResultList()
                .stream()
                .map(this::toDomainWithComments)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findByStatus(NoteStatus status) {
        return find("status", status.name()).stream()
                .map(this::toDomainWithComments)
                .collect(Collectors.toList());
    }

    @Override
    public List<Note> findAllActive() {
        return find("status", NoteStatus.ACTIVE.name()).stream()
                .map(this::toDomainWithComments)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void save(Note note) {
        NoteEntity entity = NoteMapper.toEntity(note);
        if (!existsById(note.getId())) {
            persist(entity);
            // Save associated collections separately
            saveTags(note);
            saveRecipients(note);
            saveComments(note);
        } else {
            getEntityManager().merge(entity);
            // Update associated collections (delete all and re-insert)
            updateTags(note);
            updateRecipients(note);
            updateComments(note);
        }
    }

    @Override
    @Transactional
    public void update(Note note) {
        NoteEntity entity = NoteMapper.toEntity(note);
        getEntityManager().merge(entity);
        // Update all associated collections (delete all and re-insert)
        updateTags(note);
        updateRecipients(note);
        updateComments(note);
    }

    @Override
    public boolean existsById(NoteId id) {
        return count("noteId", id.id()) > 0;
    }

    @Override
    @Transactional
    public void deleteById(NoteId id) {
        // Comments will be cascade deleted due to FK constraint
        delete("noteId", id.id());
    }

    // -------------------- Private Helper Methods --------------------

    /**
     * Loads a NoteEntity and its collections, returning a fully reconstituted Note aggregate.
     */
    private Note toDomainWithComments(NoteEntity entity) {
        NoteId noteId = new NoteId(entity.getNoteId());

        Set<java.util.UUID> tagIds = loadTagIdsForNote(noteId);
        Set<RecipientEmbeddable> recipients = loadRecipientsForNote(noteId);
        List<Comment> comments = loadCommentsForNote(noteId);

        Note note = NoteMapper.toDomain(entity, tagIds, recipients);
        comments.forEach(note::addComment);
        return note;
    }

    /**
     * Loads all comments for a given note from the database.
     */
    private List<Comment> loadCommentsForNote(NoteId noteId) {
        return getEntityManager()
                .createQuery("SELECT c FROM CommentEntity c WHERE c.noteId = :noteId ORDER BY c.auditInfo.createdAt",
                        CommentEntity.class)
                .setParameter("noteId", noteId.id())
                .getResultList()
                .stream()
                .map(CommentMapper::toDomain)
                .collect(Collectors.toList());
    }

    /**
     * Loads tag IDs for a given note from the note_tag table.
     */
    private Set<java.util.UUID> loadTagIdsForNote(NoteId noteId) {
        return getEntityManager()
                .createQuery("SELECT nt.tagId FROM NoteTagEntity nt WHERE nt.noteId = :noteId", java.util.UUID.class)
                .setParameter("noteId", noteId.id())
                .getResultStream()
                .collect(Collectors.toSet());
    }

    /**
     * Loads recipients for a given note from the note_recipient table.
     */
    private Set<RecipientEmbeddable> loadRecipientsForNote(NoteId noteId) {
        return getEntityManager()
                .createQuery("SELECT nr FROM NoteRecipientEntity nr WHERE nr.noteId = :noteId",
                        NoteRecipientEntity.class)
                .setParameter("noteId", noteId.id())
                .getResultStream()
                .map(entity -> new RecipientEmbeddable(entity.getUserId(), entity.getSharedAt()))
                .collect(Collectors.toSet());
    }

    /**
     * Saves tags for a note (used during initial creation).
     */
    private void saveTags(Note note) {
        EntityManager em = getEntityManager();
        note.getTagIds().forEach(tagId -> {
            NoteTagEntity entity = new NoteTagEntity(
                    note.getId().id(),
                    tagId.id(),
                    Instant.now()
            );
            em.merge(entity);  // Use merge() instead of persist() to handle both new and managed entities
        });
    }

    /**
     * Updates tags for a note by deleting existing ones and inserting new ones.
     */
    private void updateTags(Note note) {
        EntityManager em = getEntityManager();

        // Delete existing tags
        em.createQuery("DELETE FROM NoteTagEntity nt WHERE nt.noteId = :noteId")
                .setParameter("noteId", note.getId().id())
                .executeUpdate();

        // Flush to execute DELETE before INSERT
        em.flush();

        // Insert current tags
        saveTags(note);
    }

    /**
     * Saves recipients for a note (used during initial creation).
     */
    private void saveRecipients(Note note) {
        EntityManager em = getEntityManager();
        note.getRecipients().forEach(recipient -> {
            NoteRecipientEntity entity = new NoteRecipientEntity(
                    note.getId().id(),
                    recipient.userId().id(),
                    recipient.sharedAt()
            );
            em.merge(entity);  // Use merge() instead of persist() to handle both new and managed entities
        });
    }

    /**
     * Updates recipients for a note by deleting existing ones and inserting new ones.
     */
    private void updateRecipients(Note note) {
        EntityManager em = getEntityManager();

        // Delete existing recipients
        em.createQuery("DELETE FROM NoteRecipientEntity nr WHERE nr.noteId = :noteId")
                .setParameter("noteId", note.getId().id())
                .executeUpdate();

        // Flush to execute DELETE before INSERT
        em.flush();

        // Insert current recipients
        saveRecipients(note);
    }

    /**
     * Saves all comments for a note (used during initial creation).
     */
    private void saveComments(Note note) {
        EntityManager em = getEntityManager();
        // Use dependency-aware insertion to handle nested replies
        insertCommentsInDependencyOrder(note.getComments(), note.getId().id(), em, Collections.emptySet());
    }

    /**
     * Updates comments for a note using smart insert pattern.
     * Only inserts NEW comments (not already in database) in dependency order.
     * Updates existing comments using merge to reflect any changes.
     * This prevents foreign key constraint violations and improves performance.
     */
    private void updateComments(Note note) {
        EntityManager em = getEntityManager();

        // Query existing comment IDs from database
        Set<UUID> existingCommentIds = em.createQuery(
                "SELECT c.commentId FROM CommentEntity c WHERE c.noteId = :noteId", UUID.class)
                .setParameter("noteId", note.getId().id())
                .getResultStream()
                .collect(Collectors.toSet());

        // Partition comments: new vs existing
        List<Comment> newComments = note.getComments().stream()
                .filter(c -> !existingCommentIds.contains(c.getId().id()))
                .toList();

        List<Comment> existingComments = note.getComments().stream()
                .filter(c -> existingCommentIds.contains(c.getId().id()))
                .toList();

        // ONLY insert NEW comments in dependency order
        insertCommentsInDependencyOrder(newComments, note.getId().id(), em, existingCommentIds);

        // Update existing comments (handles edits, status changes)
        for (Comment comment : existingComments) {
            CommentEntity entity = CommentMapper.toEntity(comment);
            entity.setNoteId(note.getId().id());
            em.merge(entity);
        }
    }

    /**
     * Inserts comments in dependency order to avoid foreign key constraint violations.
     * Handles arbitrary nesting depth by iteratively inserting comments whose parents
     * have already been inserted.
     *
     * <p>Algorithm:
     * <ol>
     *   <li>Track which comment IDs have been inserted</li>
     *   <li>In each iteration, insert all comments whose parent is either:
     *     <ul>
     *       <li>null (root comments), OR</li>
     *       <li>already inserted</li>
     *     </ul>
     *   </li>
     *   <li>Repeat until all comments are inserted</li>
     * </ol>
     *
     * <p>This handles nested replies of any depth (reply to reply to reply...).
     *
     * @param comments the list of comments to insert
     * @param noteId the note ID to associate comments with
     * @param em the entity manager
     * @throws IllegalStateException if unable to insert comments due to unresolvable dependencies
     */
    private void insertCommentsInDependencyOrder(
            List<Comment> comments,
            UUID noteId,
            EntityManager em,
            Set<UUID> existingCommentIds) {
        if (comments.isEmpty()) {
            return;
        }

        // Pre-populate with existing comment IDs (already in database)
        Set<CommentId> insertedCommentIds = existingCommentIds.stream()
                .map(CommentId::new)
                .collect(Collectors.toSet());
        List<Comment> remainingComments = new ArrayList<>(comments);
        int previousSize = -1;

        // Keep iterating until all comments are inserted
        while (!remainingComments.isEmpty()) {
            // Safety check: if we made no progress in the last iteration, we have a circular dependency or orphan
            if (remainingComments.size() == previousSize) {
                // This should never happen if AddComment validation works correctly
                throw new IllegalStateException(
                    "Unable to insert comments due to unresolvable dependencies. " +
                    "Remaining comments: " + remainingComments.size());
            }
            previousSize = remainingComments.size();

            List<Comment> toInsert = new ArrayList<>();
            List<Comment> toDefer = new ArrayList<>();

            // Partition comments into those ready to insert vs those to defer
            for (Comment comment : remainingComments) {
                CommentId parentId = comment.getParentCommentId();

                // Can insert if: (1) root comment (no parent), OR (2) parent already inserted
                if (parentId == null || insertedCommentIds.contains(parentId)) {
                    toInsert.add(comment);
                } else {
                    toDefer.add(comment);
                }
            }

            // Insert all ready comments in this iteration
            for (Comment comment : toInsert) {
                CommentEntity entity = CommentMapper.toEntity(comment);
                entity.setNoteId(noteId);
                em.persist(entity);  // Use persist for new entities
                em.flush();          // Force immediate database write
                insertedCommentIds.add(comment.getId());
            }

            // Remaining comments for next iteration
            remainingComments = toDefer;
        }
    }
}
