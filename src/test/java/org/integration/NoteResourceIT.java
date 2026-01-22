package org.integration;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.note.domain.model.*;
import org.note.domain.repository.NoteRepository;
import org.note.domain.repository.TagRepository;
import org.shared.domain.AccountStatus;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for Note creation with tags.
 *
 * Tests the complete flow of creating notes with tags via the REST API.
 */
@QuarkusTest
@DisplayName("Note Resource Integration Tests")
class NoteResourceIT {

    @Inject
    NoteRepository noteRepository;

    @Inject
    TagRepository tagRepository;

    private static final String NOTE_PATH = "/api/v1/notes";

    private UUID userId;
    private String token;

    @BeforeEach
    @Transactional
    void setup() {
        // Create a test user ID for the context
        userId = UUID.randomUUID();

        // Create a simple JWT token for authentication
        // In a real scenario, this would come from the auth service
        token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";
    }

    @Test
    @DisplayName("should create note with tags via API")
    @Transactional
    void shouldCreateNoteWithTagsViaAPI() {
        // Given: Create some tags first
        UUID tag1Id = createTag("java", "Java programming language");
        UUID tag2Id = createTag("spring", "Spring framework");

        String requestBody = String.format("""
            {
                "title": "Spring Boot Guide",
                "content": "How to set up Spring Boot with Java",
                "contentType": "TEXT",
                "category": "DOCUMENTATION",
                "visibility": "SHARED",
                "projectId": null,
                "tagIds": ["%s", "%s"]
            }
            """, tag1Id, tag2Id);

        // When
        String responseBody = given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post(NOTE_PATH)
        .then()
            .statusCode(201)
            .extract()
            .asString();

        // Then: Verify note was created with tags
        // Note: In a real test, we'd parse the response and get the note ID
        // For now, we'll verify by querying the repository
        NoteId noteId = findMostRecentNote();
        Note createdNote = noteRepository.findById(noteId).orElseThrow();

        assertThat(createdNote.getTagIds()).hasSize(2);
        assertThat(createdNote.getTagIds()).contains(new TagId(tag1Id), new TagId(tag2Id));

        // Verify tags usage counts were incremented
        Tag tag1 = tagRepository.findById(new TagId(tag1Id)).orElseThrow();
        Tag tag2 = tagRepository.findById(new TagId(tag2Id)).orElseThrow();
        assertThat(tag1.getUsageCount()).isEqualTo(1);
        assertThat(tag2.getUsageCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("should create note without tags for backward compatibility")
    @Transactional
    void shouldCreateNoteWithoutTags() {
        // Given
        String requestBody = """
            {
                "title": "Simple Note",
                "content": "A note without any tags",
                "contentType": "TEXT",
                "category": "TIP",
                "visibility": "SHARED",
                "projectId": null
            }
            """;

        // When
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post(NOTE_PATH)
        .then()
            .statusCode(201);

        // Then: Verify note was created without tags
        NoteId noteId = findMostRecentNote();
        Note createdNote = noteRepository.findById(noteId).orElseThrow();

        assertThat(createdNote.getTagIds()).isEmpty();
    }

    @Test
    @DisplayName("should return 400 when tag does not exist")
    @Transactional
    void shouldReturn400WhenTagDoesNotExist() {
        // Given: Use a non-existent tag ID
        UUID nonExistentTagId = UUID.randomUUID();

        String requestBody = String.format("""
            {
                "title": "Note with Invalid Tag",
                "content": "This should fail",
                "contentType": "TEXT",
                "category": "TIP",
                "visibility": "SHARED",
                "projectId": null,
                "tagIds": ["%s"]
            }
            """, nonExistentTagId);

        // When/Then
        given()
            .contentType(ContentType.JSON)
            .body(requestBody)
        .when()
            .post(NOTE_PATH)
        .then()
            .statusCode(400);
    }

    // Helper methods

    @Transactional
    private UUID createTag(String name, String description) {
        Tag tag = Tag.create(new TagName(name));
        tagRepository.save(tag);
        return tag.getId().id();
    }

    @Transactional
    private NoteId findMostRecentNote() {
        // Find the most recently created note
        // This is a simplified approach for testing
        return noteRepository.findAllActive().stream()
            .reduce((first, second) -> second)
            .map(Note::getId)
            .orElseThrow(() -> new AssertionError("No note found"));
    }
}
