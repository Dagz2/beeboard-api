package org.note.exposition;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.note.application.dto.command.note.*;
import org.note.application.dto.query.NoteDto;
import org.note.application.usecase.command.note.*;
import org.note.application.usecase.query.NoteQueryService;
import org.note.exposition.service.NoteUserContextService;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import io.quarkus.logging.Log;

import java.util.List;
import java.util.UUID;

/**
 * JAX-RS resource for Note management endpoints.
 * Base path: /api/v1/notes
 */
@Path("/api/v1/notes")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Note Management", description = "Endpoints for managing notes, including CRUD operations, lifecycle, tags, and sharing")
@SecurityRequirement(name = "JWT")
@ApplicationScoped
public class NoteResource {

    @Inject
    NoteUserContextService userContextService;

    // Command use cases
    @Inject
    CreateNote createNote;

    @Inject
    UpdateNote updateNote;

    @Inject
    ArchiveNote archiveNote;

    @Inject
    RestoreNote restoreNote;

    @Inject
    DeleteNote deleteNote;

    @Inject
    AddTagToNote addTagToNote;

    @Inject
    RemoveTagFromNote removeTagFromNote;

    @Inject
    ShareNote shareNote;

    @Inject
    UnshareNote unshareNote;

    // Query service
    @Inject
    NoteQueryService noteQueryService;

    // ==================== Note CRUD Operations ====================

    /**
     * POST /notes
     * Creates a new note.
     *
     * @param command the create note command
     * @return 201 Created
     */
    @POST
    @Operation(
        summary = "Create a new note",
        description = "Creates a new note with title, content, category, and visibility settings. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "201",
        description = "Note created successfully"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid input data"
    )
    @RequestBody(
        description = "Note creation data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Create Note Example",
                value = """
                {
                    "title": "How to fix null pointer exception",
                    "content": "Always check for null before dereferencing objects",
                    "contentType": "TEXT",
                    "category": "TIP",
                    "visibility": "SHARED",
                    "projectId": null
                }
                """
            )
        )
    )
    public Response createNote(@Valid CreateNoteCommand command) {
        Log.info("Request received: POST /notes - Creating note: " + command.title());
        try {
            createNote.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: POST /notes - Note created successfully");
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            Log.error("Request failed: POST /notes - " + e.getMessage());
            throw e;
        }
    }

    /**
     * PUT /notes/{noteId}
     * Updates a note's title and content.
     *
     * @param noteId the note ID
     * @param command the update command
     * @return 200 OK
     */
    @PUT
    @Path("/{noteId}")
    @Operation(
        summary = "Update note",
        description = "Updates a note's title and content. Only the note author can update it. **Required permission: Note author**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Note updated successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    @RequestBody(
        description = "Note update data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Update Note Example",
                value = """
                {
                    "title": "Updated: How to fix null pointer exception",
                    "content": "Always check for null before dereferencing objects. Also use Optional when appropriate.",
                    "contentType": "TEXT"
                }
                """
            )
        )
    )
    public Response updateNote(@PathParam("noteId") UUID noteId,
                               @Valid UpdateNoteCommand command) {
        Log.info("Request received: PUT /notes/" + noteId + " - Updating note");
        try {
            updateNote.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: PUT /notes/" + noteId + " - Note updated successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * GET /notes/{noteId}
     * Gets a note by ID.
     *
     * @param noteId the note ID
     * @return 200 OK with note DTO, or 404 Not Found
     */
    @GET
    @Path("/{noteId}")
    @Operation(
        summary = "Get note by ID",
        description = "Retrieves a specific note by its unique identifier. Access is granted based on visibility rules. **Required permission: Based on visibility**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Note found successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Note Response",
                value = """
                {
                    "noteId": "550e8400-e29b-41d4-a716-446655440000",
                    "title": "How to fix null pointer exception",
                    "content": "Always check for null before dereferencing objects",
                    "contentType": "TEXT",
                    "category": "TIP",
                    "visibility": "SHARED",
                    "status": "ACTIVE",
                    "authorId": "660e8400-e29b-41d4-a716-446655440001",
                    "projectId": null,
                    "tagIds": [],
                    "recipients": [],
                    "comments": [],
                    "createdAt": "2024-01-15T10:00:00Z",
                    "updatedAt": "2024-01-15T10:00:00Z",
                    "archivedAt": null
                }
                """
            )
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    public Response getNoteById(@PathParam("noteId") UUID noteId) {
        Log.info("Request received: GET /notes/" + noteId + " - Retrieving note by ID");
        try {
            var note = noteQueryService.findById(noteId);
            if (note.isPresent()) {
                Log.info("Request successful: GET /notes/" + noteId + " - Note found");
                return Response.ok(note.get()).build();
            }
            Log.info("Request completed: GET /notes/" + noteId + " - Note not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/" + noteId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * DELETE /notes/{noteId}
     * Deletes a note (soft delete).
     *
     * @param noteId the note ID
     * @return 204 No Content
     */
    @DELETE
    @Path("/{noteId}")
    @Operation(
        summary = "Delete note",
        description = "Soft deletes a note. Only the author or SUPERVISOR can delete. **Required permission: Note author or SUPERVISOR**"
    )
    @APIResponse(
        responseCode = "204",
        description = "Note deleted successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    public Response deleteNote(@PathParam("noteId") UUID noteId) {
        Log.info("Request received: DELETE /notes/" + noteId + " - Deleting note");
        try {
            deleteNote.execute(
                    new DeleteNoteCommand(noteId),
                    userContextService.getCurrentContext()
            );
            Log.info("Request successful: DELETE /notes/" + noteId + " - Note deleted successfully");
            return Response.noContent().build();
        } catch (Exception e) {
            Log.error("Request failed: DELETE /notes/" + noteId + " - " + e.getMessage());
            throw e;
        }
    }

    // ==================== Note Queries ====================

    /**
     * GET /notes
     * Gets all active notes (filtered by access rules in application layer).
     *
     * @return 200 OK with list of notes
     */
    @GET
    @Operation(
        summary = "Get all active notes",
        description = "Returns all active notes that the user has access to based on visibility rules. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Notes retrieved successfully"
    )
    public Response getAllNotes() {
        Log.info("Request received: GET /notes - Retrieving all active notes");
        try {
            List<NoteDto> notes = noteQueryService.findAllActive();
            Log.info("Request successful: GET /notes - Retrieved " + notes.size() + " notes");
            return Response.ok(notes).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes - " + e.getMessage());
            throw e;
        }
    }

    /**
     * GET /notes/author/{authorId}
     * Gets all notes by a specific author.
     *
     * @param authorId the author ID
     * @return 200 OK with list of notes
     */
    @GET
    @Path("/author/{authorId}")
    @Operation(
        summary = "Get notes by author",
        description = "Returns all notes created by a specific author. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Notes retrieved successfully"
    )
    public Response getNotesByAuthor(@PathParam("authorId") UUID authorId) {
        Log.info("Request received: GET /notes/author/" + authorId + " - Retrieving notes by author");
        try {
            List<NoteDto> notes = noteQueryService.findByAuthorId(authorId);
            Log.info("Request successful: GET /notes/author/" + authorId + " - Retrieved " + notes.size() + " notes");
            return Response.ok(notes).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/author/" + authorId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * GET /notes/category/{category}
     * Gets all notes in a specific category.
     *
     * @param category the category name
     * @return 200 OK with list of notes
     */
    @GET
    @Path("/category/{category}")
    @Operation(
        summary = "Get notes by category",
        description = "Returns all notes in a specific category (BUG, TIP, DOCUMENTATION, etc.). **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Notes retrieved successfully"
    )
    public Response getNotesByCategory(@PathParam("category") String category) {
        Log.info("Request received: GET /notes/category/" + category + " - Retrieving notes by category");
        try {
            List<NoteDto> notes = noteQueryService.findByCategory(category);
            Log.info("Request successful: GET /notes/category/" + category + " - Retrieved " + notes.size() + " notes");
            return Response.ok(notes).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/category/" + category + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * GET /notes/project/{projectId}
     * Gets all notes linked to a specific project.
     *
     * @param projectId the project ID
     * @return 200 OK with list of notes
     */
    @GET
    @Path("/project/{projectId}")
    @Operation(
        summary = "Get notes by project",
        description = "Returns all notes linked to a specific project. **Required permission: Project member**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Notes retrieved successfully"
    )
    public Response getNotesByProject(@PathParam("projectId") UUID projectId) {
        Log.info("Request received: GET /notes/project/" + projectId + " - Retrieving notes by project");
        try {
            List<NoteDto> notes = noteQueryService.findByProjectId(projectId);
            Log.info("Request successful: GET /notes/project/" + projectId + " - Retrieved " + notes.size() + " notes");
            return Response.ok(notes).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/project/" + projectId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * GET /notes/tag/{tagId}
     * Gets all notes with a specific tag.
     *
     * @param tagId the tag ID
     * @return 200 OK with list of notes
     */
    @GET
    @Path("/tag/{tagId}")
    @Operation(
        summary = "Get notes by tag",
        description = "Returns all notes that have been tagged with a specific tag. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Notes retrieved successfully"
    )
    public Response getNotesByTag(@PathParam("tagId") UUID tagId) {
        Log.info("Request received: GET /notes/tag/" + tagId + " - Retrieving notes by tag");
        try {
            List<NoteDto> notes = noteQueryService.findByTagId(tagId);
            Log.info("Request successful: GET /notes/tag/" + tagId + " - Retrieved " + notes.size() + " notes");
            return Response.ok(notes).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/tag/" + tagId + " - " + e.getMessage());
            throw e;
        }
    }

    // ==================== Note Lifecycle ====================

    /**
     * PUT /notes/{noteId}/archive
     * Archives a note.
     *
     * @param noteId the note ID
     * @return 200 OK
     */
    @PUT
    @Path("/{noteId}/archive")
    @Operation(
        summary = "Archive note",
        description = "Archives a note. Only the author or SUPERVISOR can archive. **Required permission: Note author or SUPERVISOR**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Note archived successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    public Response archiveNote(@PathParam("noteId") UUID noteId) {
        Log.info("Request received: PUT /notes/" + noteId + "/archive - Archiving note");
        try {
            archiveNote.execute(
                    new ArchiveNoteCommand(noteId),
                    userContextService.getCurrentContext()
            );
            Log.info("Request successful: PUT /notes/" + noteId + "/archive - Note archived successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + "/archive - " + e.getMessage());
            throw e;
        }
    }

    /**
     * PUT /notes/{noteId}/restore
     * Restores an archived note.
     *
     * @param noteId the note ID
     * @return 200 OK
     */
    @PUT
    @Path("/{noteId}/restore")
    @Operation(
        summary = "Restore archived note",
        description = "Restores an archived note back to active status. Only the author or SUPERVISOR can restore. **Required permission: Note author or SUPERVISOR**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Note restored successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    public Response restoreNote(@PathParam("noteId") UUID noteId) {
        Log.info("Request received: PUT /notes/" + noteId + "/restore - Restoring note");
        try {
            restoreNote.execute(
                    new RestoreNoteCommand(noteId),
                    userContextService.getCurrentContext()
            );
            Log.info("Request successful: PUT /notes/" + noteId + "/restore - Note restored successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + "/restore - " + e.getMessage());
            throw e;
        }
    }

    // ==================== Tag Management ====================

    /**
     * POST /notes/{noteId}/tags
     * Adds a tag to a note.
     *
     * @param noteId the note ID
     * @param command the add tag command
     * @return 200 OK
     */
    @POST
    @Path("/{noteId}/tags")
    @Operation(
        summary = "Add tag to note",
        description = "Adds a tag to a note. Only the note author can add tags. **Required permission: Note author**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Tag added successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or tag not found"
    )
    @RequestBody(
        description = "Add tag data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Add Tag Example",
                value = """
                {
                    "noteId": "550e8400-e29b-41d4-a716-446655440000",
                    "tagId": "770e8400-e29b-41d4-a716-446655440002"
                }
                """
            )
        )
    )
    public Response addTag(@PathParam("noteId") UUID noteId,
                          @Valid AddTagToNoteCommand command) {
        Log.info("Request received: POST /notes/" + noteId + "/tags - Adding tag to note");
        try {
            addTagToNote.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: POST /notes/" + noteId + "/tags - Tag added successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: POST /notes/" + noteId + "/tags - " + e.getMessage());
            throw e;
        }
    }

    /**
     * DELETE /notes/{noteId}/tags/{tagId}
     * Removes a tag from a note.
     *
     * @param noteId the note ID
     * @param tagId the tag ID
     * @return 204 No Content
     */
    @DELETE
    @Path("/{noteId}/tags/{tagId}")
    @Operation(
        summary = "Remove tag from note",
        description = "Removes a tag from a note. Only the note author can remove tags. **Required permission: Note author**"
    )
    @APIResponse(
        responseCode = "204",
        description = "Tag removed successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or tag not found"
    )
    public Response removeTag(@PathParam("noteId") UUID noteId,
                             @PathParam("tagId") UUID tagId) {
        Log.info("Request received: DELETE /notes/" + noteId + "/tags/" + tagId + " - Removing tag from note");
        try {
            removeTagFromNote.execute(
                    new RemoveTagFromNoteCommand(noteId, tagId),
                    userContextService.getCurrentContext()
            );
            Log.info("Request successful: DELETE /notes/" + noteId + "/tags/" + tagId + " - Tag removed successfully");
            return Response.noContent().build();
        } catch (Exception e) {
            Log.error("Request failed: DELETE /notes/" + noteId + "/tags/" + tagId + " - " + e.getMessage());
            throw e;
        }
    }

    // ==================== Sharing ====================

    /**
     * POST /notes/{noteId}/share
     * Shares a note with a user.
     *
     * @param noteId the note ID
     * @param command the share command
     * @return 200 OK
     */
    @POST
    @Path("/{noteId}/share")
    @Operation(
        summary = "Share note with user",
        description = "Shares a note with a specific user. Only the note author can share notes. **Required permission: Note author**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Note shared successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    @RequestBody(
        description = "Share note data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Share Note Example",
                value = """
                {
                    "noteId": "550e8400-e29b-41d4-a716-446655440000",
                    "userId": "880e8400-e29b-41d4-a716-446655440003",
                    "accessLevel": "VIEWER"
                }
                """
            )
        )
    )
    public Response shareNote(@PathParam("noteId") UUID noteId,
                             @Valid ShareNoteCommand command) {
        Log.info("Request received: POST /notes/" + noteId + "/share - Sharing note");
        try {
            shareNote.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: POST /notes/" + noteId + "/share - Note shared successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: POST /notes/" + noteId + "/share - " + e.getMessage());
            throw e;
        }
    }

    /**
     * DELETE /notes/{noteId}/share/{userId}
     * Unshares a note with a user.
     *
     * @param noteId the note ID
     * @param userId the user ID
     * @return 204 No Content
     */
    @DELETE
    @Path("/{noteId}/share/{userId}")
    @Operation(
        summary = "Unshare note with user",
        description = "Removes a user from the note's recipients list. Only the note author can unshare notes. **Required permission: Note author**"
    )
    @APIResponse(
        responseCode = "204",
        description = "Note unshared successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    public Response unshareNote(@PathParam("noteId") UUID noteId,
                                @PathParam("userId") UUID userId) {
        Log.info("Request received: DELETE /notes/" + noteId + "/share/" + userId + " - Unsharing note");
        try {
            unshareNote.execute(
                    new UnshareNoteCommand(noteId, userId),
                    userContextService.getCurrentContext()
            );
            Log.info("Request successful: DELETE /notes/" + noteId + "/share/" + userId + " - Note unshared successfully");
            return Response.noContent().build();
        } catch (Exception e) {
            Log.error("Request failed: DELETE /notes/" + noteId + "/share/" + userId + " - " + e.getMessage());
            throw e;
        }
    }
}
