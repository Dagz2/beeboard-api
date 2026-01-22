package org.note.application.usecase.command.note;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.note.application.dto.command.note.CreateNoteCommand;
import org.note.domain.exception.TagNotFoundException;
import org.note.domain.model.*;
import org.note.domain.port.ProjectAccessPort;
import org.note.domain.repository.NoteRepository;
import org.note.domain.repository.TagRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CreateNote Use Case")
class CreateNoteTest {

    @Mock
    NoteRepository noteRepository;

    @Mock
    ProjectAccessPort projectAccessPort;

    @Mock
    TagRepository tagRepository;

    @InjectMocks
    CreateNote useCase;

    NoteUserContext userContext;

    @BeforeEach
    void setUp() {
        userContext = new NoteUserContext(
            UUID.randomUUID(),
            AccessLevel.COMMENTER
        );
    }

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("should create note with valid tags")
        void shouldCreateNoteWithTags() {
            // Given
            UUID tag1Id = UUID.randomUUID();
            UUID tag2Id = UUID.randomUUID();
            Set<UUID> tagIds = Set.of(tag1Id, tag2Id);

            Tag tag1 = mock(Tag.class);
            Tag tag2 = mock(Tag.class);

            when(tagRepository.findById(new TagId(tag1Id))).thenReturn(Optional.of(tag1));
            when(tagRepository.findById(new TagId(tag2Id))).thenReturn(Optional.of(tag2));

            CreateNoteCommand command = new CreateNoteCommand(
                "Test Title",
                "Test Content",
                "TEXT",
                "TIP",
                "SHARED",
                null,
                tagIds
            );

            // When
            useCase.execute(command, userContext);

            // Then
            ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
            verify(noteRepository).save(noteCaptor.capture());
            Note savedNote = noteCaptor.getValue();
            assertThat(savedNote.getTagIds()).hasSize(2);
            assertThat(savedNote.getTagIds()).contains(new TagId(tag1Id), new TagId(tag2Id));

            // Verify tags were incremented
            verify(tag1).incrementUsage();
            verify(tag2).incrementUsage();
            verify(tagRepository).update(tag1);
            verify(tagRepository).update(tag2);
        }

        @Test
        @DisplayName("should throw exception when tag not found")
        void shouldThrowExceptionWhenTagNotFound() {
            // Given
            UUID invalidTagId = UUID.randomUUID();
            Set<UUID> tagIds = Set.of(invalidTagId);

            when(tagRepository.findById(new TagId(invalidTagId))).thenReturn(Optional.empty());

            CreateNoteCommand command = new CreateNoteCommand(
                "Test Title",
                "Test Content",
                "TEXT",
                "TIP",
                "SHARED",
                null,
                tagIds
            );

            // When & Then
            assertThatThrownBy(() -> useCase.execute(command, userContext))
                .isInstanceOf(TagNotFoundException.class);

            // Verify note was NOT saved
            verify(noteRepository, never()).save(any());
        }

        @Test
        @DisplayName("should create note without tags for backward compatibility")
        void shouldCreateNoteWithoutTags() {
            // Given
            CreateNoteCommand command = new CreateNoteCommand(
                "Test Title",
                "Test Content",
                "TEXT",
                "TIP",
                "SHARED",
                null,
                null
            );

            // When
            useCase.execute(command, userContext);

            // Then
            ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
            verify(noteRepository).save(noteCaptor.capture());
            Note savedNote = noteCaptor.getValue();
            assertThat(savedNote.getTagIds()).isEmpty();

            // Verify no tag operations
            verify(tagRepository, never()).findById(any());
            verify(tagRepository, never()).update(any());
        }

        @Test
        @DisplayName("should create note with empty tag set")
        void shouldCreateNoteWithEmptyTagSet() {
            // Given
            CreateNoteCommand command = new CreateNoteCommand(
                "Test Title",
                "Test Content",
                "TEXT",
                "TIP",
                "SHARED",
                null,
                Set.of()
            );

            // When
            useCase.execute(command, userContext);

            // Then
            ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
            verify(noteRepository).save(noteCaptor.capture());
            Note savedNote = noteCaptor.getValue();
            assertThat(savedNote.getTagIds()).isEmpty();

            // Verify no tag operations
            verify(tagRepository, never()).findById(any());
            verify(tagRepository, never()).update(any());
        }

        @Test
        @DisplayName("should create note with basic data")
        void shouldCreateNoteWithBasicData() {
            // Given
            CreateNoteCommand command = new CreateNoteCommand(
                "Test Title",
                "Test Content",
                "TEXT",
                "QUESTION",
                "PRIVATE",
                null,
                null
            );

            // When
            useCase.execute(command, userContext);

            // Then
            verify(noteRepository).save(any(Note.class));
        }
    }
}
