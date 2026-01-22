package org.note.infrastructure.repository;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.*;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@QuarkusTest
@DisplayName("NoteRepository Integration")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class NoteRepositoryIT {

    @Inject
    NoteRepository noteRepository;

    static NoteId testNoteId;
    static AuthorId testAuthorId;

    @Nested
    @DisplayName("Comment Reply Foreign Key Constraint")
    class CommentReplyConstraint {

        @Test
        @Order(1)
        @Transactional
        @DisplayName("should insert comment replies without foreign key constraint violation")
        void shouldInsertCommentRepliesWithoutForeignKeyViolation() {
            // Given - Create a note with an initial comment
            testAuthorId = new AuthorId(UUID.randomUUID());
            Note note = Note.create(
                new NoteTitle("Test Note with Comments"),
                new NoteContent("This is a test note for comment replies.", ContentType.TEXT),
                NoteCategory.QUESTION,
                NoteVisibility.SHARED,
                testAuthorId,
                null
            );

            // Add a root comment
            Comment rootComment = Comment.create(
                testAuthorId,
                "This is the root comment"
            );
            note.addComment(rootComment);

            // Save the note with the root comment
            noteRepository.save(note);
            testNoteId = note.getId();

            // When - Retrieve the note and add a reply comment
            Note retrieved = noteRepository.findById(testNoteId).orElseThrow();
            Comment parentComment = retrieved.getComments().get(0);

            Comment replyComment = Comment.create(
                new AuthorId(UUID.randomUUID()),
                "This is a reply to the root comment",
                parentComment.getId()  // Set parent comment ID
            );
            retrieved.addComment(replyComment);

            // Then - Update should not throw foreign key constraint violation
            assertDoesNotThrow(() -> noteRepository.update(retrieved));

            // Verify both comments exist in the database
            Note updated = noteRepository.findById(testNoteId).orElseThrow();
            assertThat(updated.getComments()).hasSize(2);

            // Verify the reply has the correct parent
            Comment savedReply = updated.getComments().stream()
                .filter(Comment::isReply)
                .findFirst()
                .orElseThrow();
            assertThat(savedReply.getParentCommentId()).isEqualTo(parentComment.getId());
            assertThat(savedReply.getContent()).isEqualTo("This is a reply to the root comment");
        }

        @Test
        @Order(2)
        @Transactional
        @DisplayName("should handle multiple replies correctly")
        void shouldHandleMultipleRepliesCorrectly() {
            // Given - Note exists with 2 comments from previous test
            Note note = noteRepository.findById(testNoteId).orElseThrow();
            Comment rootComment = note.getComments().stream()
                .filter(c -> !c.isReply())
                .findFirst()
                .orElseThrow();

            // When - Add multiple replies to the root comment
            Comment reply2 = Comment.create(
                new AuthorId(UUID.randomUUID()),
                "Second reply to root comment",
                rootComment.getId()
            );
            Comment reply3 = Comment.create(
                new AuthorId(UUID.randomUUID()),
                "Third reply to root comment",
                rootComment.getId()
            );

            note.addComment(reply2);
            note.addComment(reply3);

            // Then - Should save without foreign key violations
            assertDoesNotThrow(() -> noteRepository.update(note));

            // Verify all comments are saved correctly
            Note updated = noteRepository.findById(testNoteId).orElseThrow();
            assertThat(updated.getComments()).hasSize(4);

            // Verify root comments and replies
            long rootCount = updated.getComments().stream()
                .filter(c -> !c.isReply())
                .count();
            long replyCount = updated.getComments().stream()
                .filter(Comment::isReply)
                .count();

            assertThat(rootCount).isEqualTo(1);
            assertThat(replyCount).isEqualTo(3);
        }

        @Test
        @Order(3)
        @Transactional
        @DisplayName("should handle mixed root comments and replies")
        void shouldHandleMixedRootCommentsAndReplies() {
            // Given - Note exists from previous tests
            Note note = noteRepository.findById(testNoteId).orElseThrow();
            Comment firstRootComment = note.getComments().stream()
                .filter(c -> !c.isReply())
                .findFirst()
                .orElseThrow();

            // When - Add a new root comment and a reply to it
            Comment secondRootComment = Comment.create(
                new AuthorId(UUID.randomUUID()),
                "Second root comment"
            );
            note.addComment(secondRootComment);

            Comment replyToSecond = Comment.create(
                new AuthorId(UUID.randomUUID()),
                "Reply to second root comment",
                secondRootComment.getId()
            );
            note.addComment(replyToSecond);

            // Add another reply to the first root comment
            Comment replyToFirst = Comment.create(
                new AuthorId(UUID.randomUUID()),
                "Another reply to first root",
                firstRootComment.getId()
            );
            note.addComment(replyToFirst);

            // Then - Should save without foreign key violations
            assertDoesNotThrow(() -> noteRepository.update(note));

            // Verify the structure
            Note updated = noteRepository.findById(testNoteId).orElseThrow();
            assertThat(updated.getComments()).hasSize(7);

            long rootCount = updated.getComments().stream()
                .filter(c -> !c.isReply())
                .count();
            long replyCount = updated.getComments().stream()
                .filter(Comment::isReply)
                .count();

            assertThat(rootCount).isEqualTo(2);
            assertThat(replyCount).isEqualTo(5);
        }

        @Test
        @Order(4)
        @Transactional
        @DisplayName("should insert deeply nested replies (3+ levels) without foreign key constraint violation")
        void shouldInsertDeeplyNestedRepliesWithoutForeignKeyViolation() {
            // Given - Create a new note with a fresh comment structure for nested replies
            AuthorId author = new AuthorId(UUID.randomUUID());
            Note note = Note.create(
                new NoteTitle("Test Note for Nested Replies"),
                new NoteContent("Testing deeply nested comment replies.", ContentType.TEXT),
                NoteCategory.QUESTION,
                NoteVisibility.SHARED,
                author,
                null
            );

            // Create comment hierarchy: A (root) -> B (reply to A) -> C (reply to B) -> D (reply to C)
            Comment commentA = Comment.create(author, "Root comment A");
            note.addComment(commentA);

            Comment commentB = Comment.create(author, "Reply B to A", commentA.getId());
            note.addComment(commentB);

            Comment commentC = Comment.create(author, "Reply C to B", commentB.getId());
            note.addComment(commentC);

            Comment commentD = Comment.create(author, "Reply D to C", commentC.getId());
            note.addComment(commentD);

            // When - Save the note with 4 levels of nesting
            assertDoesNotThrow(() -> noteRepository.save(note));

            // Then - Verify all 4 comments exist with correct parent relationships
            Note saved = noteRepository.findById(note.getId()).orElseThrow();
            assertThat(saved.getComments()).hasSize(4);

            // Verify hierarchy is preserved
            Comment savedA = saved.getComments().stream()
                .filter(c -> c.getContent().equals("Root comment A"))
                .findFirst().orElseThrow();
            assertThat(savedA.getParentCommentId()).isNull();

            Comment savedB = saved.getComments().stream()
                .filter(c -> c.getContent().equals("Reply B to A"))
                .findFirst().orElseThrow();
            assertThat(savedB.getParentCommentId()).isEqualTo(savedA.getId());

            Comment savedC = saved.getComments().stream()
                .filter(c -> c.getContent().equals("Reply C to B"))
                .findFirst().orElseThrow();
            assertThat(savedC.getParentCommentId()).isEqualTo(savedB.getId());

            Comment savedD = saved.getComments().stream()
                .filter(c -> c.getContent().equals("Reply D to C"))
                .findFirst().orElseThrow();
            assertThat(savedD.getParentCommentId()).isEqualTo(savedC.getId());

            // Cleanup
            noteRepository.deleteById(note.getId());
        }

        @Test
        @Order(5)
        @Transactional
        @DisplayName("should handle adding nested reply to existing note without foreign key constraint violation")
        void shouldHandleAddingNestedReplyToExistingNote() {
            // Given - Create a note with initial comments
            AuthorId author = new AuthorId(UUID.randomUUID());
            Note note = Note.create(
                new NoteTitle("Test Note for Adding Nested Reply"),
                new NoteContent("Testing adding nested reply to existing note.", ContentType.TEXT),
                NoteCategory.QUESTION,
                NoteVisibility.SHARED,
                author,
                null
            );

            Comment commentA = Comment.create(author, "Root comment");
            note.addComment(commentA);
            noteRepository.save(note);

            // Add first level reply
            Note retrieved = noteRepository.findById(note.getId()).orElseThrow();
            Comment commentB = Comment.create(author, "Reply to root", commentA.getId());
            retrieved.addComment(commentB);
            noteRepository.update(retrieved);

            // When - Add nested reply (reply to a reply)
            Note noteWithNestedReply = noteRepository.findById(note.getId()).orElseThrow();
            Comment existingReply = noteWithNestedReply.getComments().stream()
                .filter(c -> c.getContent().equals("Reply to root"))
                .findFirst().orElseThrow();
            Comment commentC = Comment.create(author, "Reply to reply", existingReply.getId());
            noteWithNestedReply.addComment(commentC);

            // Then - Should not throw foreign key constraint violation
            assertDoesNotThrow(() -> noteRepository.update(noteWithNestedReply));

            // Verify all 3 comments exist with correct hierarchy
            Note updated = noteRepository.findById(note.getId()).orElseThrow();
            assertThat(updated.getComments()).hasSize(3);

            Comment savedC = updated.getComments().stream()
                .filter(c -> c.getContent().equals("Reply to reply"))
                .findFirst().orElseThrow();
            assertThat(savedC.getParentCommentId()).isNotNull();

            // Cleanup
            noteRepository.deleteById(note.getId());
        }

        @Test
        @Order(6)
        @Transactional
        @DisplayName("should handle multiple reply branches with different nesting depths")
        void shouldHandleMultipleReplyBranches() {
            // Given - Create note with branching structure:
            // A (root) -> B (reply to A) -> D (reply to B)
            //          -> C (reply to A) -> E (reply to C) -> F (reply to E)
            AuthorId author = new AuthorId(UUID.randomUUID());
            Note note = Note.create(
                new NoteTitle("Test Note for Branch Structure"),
                new NoteContent("Testing multiple branches with different depths.", ContentType.TEXT),
                NoteCategory.QUESTION,
                NoteVisibility.SHARED,
                author,
                null
            );

            Comment commentA = Comment.create(author, "Root A");
            Comment commentB = Comment.create(author, "Reply B to A", commentA.getId());
            Comment commentC = Comment.create(author, "Reply C to A", commentA.getId());
            Comment commentD = Comment.create(author, "Reply D to B", commentB.getId());
            Comment commentE = Comment.create(author, "Reply E to C", commentC.getId());
            Comment commentF = Comment.create(author, "Reply F to E", commentE.getId());

            note.addComment(commentA);
            note.addComment(commentB);
            note.addComment(commentC);
            note.addComment(commentD);
            note.addComment(commentE);
            note.addComment(commentF);

            // When - Save with complex branching structure
            assertDoesNotThrow(() -> noteRepository.save(note));

            // Then - Verify all comments are saved with correct hierarchy
            Note saved = noteRepository.findById(note.getId()).orElseThrow();
            assertThat(saved.getComments()).hasSize(6);

            // Verify branch 1: A -> B -> D (depth 2)
            Comment savedD = saved.getComments().stream()
                .filter(c -> c.getContent().equals("Reply D to B"))
                .findFirst().orElseThrow();
            Comment savedB = saved.getComments().stream()
                .filter(c -> c.getId().equals(savedD.getParentCommentId()))
                .findFirst().orElseThrow();
            assertThat(savedB.getContent()).isEqualTo("Reply B to A");

            // Verify branch 2: A -> C -> E -> F (depth 3)
            Comment savedF = saved.getComments().stream()
                .filter(c -> c.getContent().equals("Reply F to E"))
                .findFirst().orElseThrow();
            Comment savedE = saved.getComments().stream()
                .filter(c -> c.getId().equals(savedF.getParentCommentId()))
                .findFirst().orElseThrow();
            assertThat(savedE.getContent()).isEqualTo("Reply E to C");
            Comment savedC = saved.getComments().stream()
                .filter(c -> c.getId().equals(savedE.getParentCommentId()))
                .findFirst().orElseThrow();
            assertThat(savedC.getContent()).isEqualTo("Reply C to A");

            // Cleanup
            noteRepository.deleteById(note.getId());
        }

        @Test
        @Order(99)
        @Transactional
        @DisplayName("cleanup - delete test note")
        void cleanupDeleteTestNote() {
            // Cleanup
            if (testNoteId != null && noteRepository.existsById(testNoteId)) {
                noteRepository.deleteById(testNoteId);
            }
        }
    }

    @Nested
    @DisplayName("CRUD Operations")
    class CrudOperations {

        @Test
        @Transactional
        @DisplayName("should save and retrieve note")
        void shouldSaveAndRetrieveNote() {
            // Given
            AuthorId authorId = new AuthorId(UUID.randomUUID());
            Note note = Note.create(
                new NoteTitle("CRUD Test Note"),
                new NoteContent("This is a test note for CRUD operations.", ContentType.TEXT),
                NoteCategory.DOCUMENTATION,
                NoteVisibility.SHARED,
                authorId,
                null
            );
            NoteId noteId = note.getId();

            // When
            noteRepository.save(note);
            Optional<Note> found = noteRepository.findById(noteId);

            // Then
            assertThat(found).isPresent();
            assertThat(found.get().getTitle().value()).isEqualTo("CRUD Test Note");
            assertThat(found.get().getCategory()).isEqualTo(NoteCategory.DOCUMENTATION);
            assertThat(found.get().getVisibility()).isEqualTo(NoteVisibility.SHARED);

            // Cleanup
            noteRepository.deleteById(noteId);
        }

        @Test
        @Transactional
        @DisplayName("should check note existence")
        void shouldCheckNoteExistence() {
            // Given
            AuthorId authorId = new AuthorId(UUID.randomUUID());
            Note note = Note.create(
                new NoteTitle("Existence Test"),
                new NoteContent("Testing existence check.", ContentType.TEXT),
                NoteCategory.DOCUMENTATION,
                NoteVisibility.SHARED,
                authorId,
                null
            );

            // When
            noteRepository.save(note);

            // Then
            assertThat(noteRepository.existsById(note.getId())).isTrue();
            assertThat(noteRepository.existsById(NoteId.newId())).isFalse();

            // Cleanup
            noteRepository.deleteById(note.getId());
        }

        @Test
        @Transactional
        @DisplayName("should delete note by id")
        void shouldDeleteNoteById() {
            // Given
            AuthorId authorId = new AuthorId(UUID.randomUUID());
            Note note = Note.create(
                new NoteTitle("Delete Test"),
                new NoteContent("This note will be deleted.", ContentType.TEXT),
                NoteCategory.DOCUMENTATION,
                NoteVisibility.SHARED,
                authorId,
                null
            );
            noteRepository.save(note);
            NoteId noteId = note.getId();
            assertThat(noteRepository.existsById(noteId)).isTrue();

            // When
            noteRepository.deleteById(noteId);

            // Then
            assertThat(noteRepository.existsById(noteId)).isFalse();
        }

        @Test
        @Transactional
        @DisplayName("should return empty when note not found")
        void shouldReturnEmptyWhenNotFound() {
            // Given
            NoteId nonExistentId = NoteId.newId();

            // When
            Optional<Note> found = noteRepository.findById(nonExistentId);

            // Then
            assertThat(found).isEmpty();
        }
    }
}
