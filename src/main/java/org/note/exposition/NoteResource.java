package org.note.exposition;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.note.application.dto.command.note.*;
import org.note.application.dto.query.CommentDto;
import org.note.application.dto.query.NoteDto;
import org.note.application.usecase.command.note.*;
import org.note.application.usecase.query.CommentQueryService;
import org.note.application.usecase.query.NoteQueryService;
import org.note.domain.model.NoteUserContext;
import org.note.exposition.service.NoteUserContextService;

import java.util.Optional;

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

    @Inject
    ChangeNoteCategory changeNoteCategory;

    @Inject
    ChangeNoteVisibility changeNoteVisibility;

    // Query services
    @Inject
    NoteQueryService noteQueryService;

    @Inject
    CommentQueryService commentQueryService;

    // ========================================
    // CRUD OPERATIONS
    // ========================================

    /**
     * Creates a new note in the system.
     *
     * Validates the input data and creates a note with the specified title,
     * content, category (QUESTION, TIP, DOCUMENTATION), visibility, and
     * optional tags. The author is automatically set to the current
     * authenticated user.
     *
     * Visibility controls who can access the note:
     * - PRIVATE: Visible only to the author and explicit recipients
     * - SHARED: Visible to all authenticated users
     * - PROJECT: Visible only within the context of a project (projectId required)
     *
     * Tags can be specified at creation time by providing a set of tag IDs.
     * All tag IDs must correspond to existing tags in the system, otherwise
     * the operation fails with a 400 error.
     *
     * If visibility=PROJECT, a valid projectId must be provided, otherwise the
     * operation fails. Created notes are in ACTIVE status by default and can
     * immediately receive comments.
     *
     * @param command validated data including title, content, category,
     *                visibility, and optional tag IDs
     * @return HTTP 201 Created upon successful creation
     * @throws IllegalArgumentException if visibility=PROJECT without a valid projectId
     * @throws org.note.domain.exception.TagNotFoundException if any tag ID doesn't exist
     */
    @POST
    @Operation(
        summary = "Create a new note",
        description = "Creates a new note with title, content, category, visibility settings, and optional tags. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "201",
        description = "Note created successfully"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid input data (missing required fields, invalid visibility/projectId combination, or tag not found)"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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
                    "projectId": null,
                    "tagIds": [
                        "880e8400-e29b-41d4-a716-446655440010",
                        "880e8400-e29b-41d4-a716-446655440011"
                    ]
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
     * Modifies the title and content of an existing note.
     *
     * Allows the note author to update their note's title and text content
     * at any time after creation. The modification does not allow changing
     * the category or visibility (dedicated endpoints available).
     *
     * Only the original note author can perform this modification.
     * Administrators, moderators, and other users cannot modify others' note
     * content to preserve intellectual integrity and content attribution.
     *
     * The modification date (updatedAt) is automatically updated to indicate
     * that the content has been modified. The note remains in ACTIVE status
     * and retains all its existing comments, tags, and shares.
     *
     * @param noteId the unique identifier of the note to modify
     * @param command validated data including the new title and content
     * @return HTTP 200 OK upon successful modification
     * @throws jakarta.ws.rs.ForbiddenException if the user is not the author
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - only note author can update"
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
     * Retrieves a specific note by its identifier.
     *
     * Returns the complete details of a note using its UUID, including title,
     * content, category, visibility, status, author, tags, recipients, comments,
     * and audit metadata (creation, modification, and archiving dates).
     *
     * Access is subject to the note's visibility rules:
     * - PRIVATE: Accessible only to the author and explicit recipients
     * - SHARED: Accessible to all authenticated users
     * - PROJECT: Accessible only within the context of the specified project
     *
     * Archived notes (status=ARCHIVED) are still accessible but marked as such
     * for contextual information. Users without appropriate permission receive
     * a 403 error even if the note exists.
     *
     * @param noteId the unique identifier of the note
     * @return HTTP 200 OK with the complete note DTO if found and authorized, HTTP 404 Not Found otherwise
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
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "title": "How to fix null pointer exception",
                    "content": "Always check for null before dereferencing objects",
                    "contentType": "TEXT",
                    "category": "TIP",
                    "visibility": "SHARED",
                    "status": "ACTIVE",
                    "authorId": "660e8400-e29b-41d4-a716-446655440001",
                    "author": {
                        "userId": "660e8400-e29b-41d4-a716-446655440001",
                        "fullName": "John Doe"
                    },
                    "projectId": null,
                    "tagIds": [],
                    "recipients": [],
                    "comments": [],
                    "auditInfo": {
                        "createdAt": "2024-01-15T10:00:00Z",
                        "updatedAt": "2024-01-15T10:00:00Z"
                    },
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized to view this note based on visibility rules"
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
     * Deletes a note from the system (soft delete).
     *
     * Performs a logical deletion of the note by hiding it from display
     * while preserving the data for audit and traceability. The note is not
     * physically deleted from the database.
     *
     * Two categories of users can delete a note:
     * - The note author (can delete their own notes)
     * - Supervisors (can delete any note for moderation)
     *
     * Deleting a note also logically deletes all its comments, but preserves
     * the tags which remain available for other notes. Shares (recipients) are
     * also removed.
     *
     * This operation is irreversible from the user's perspective, but the
     * data remains accessible for administrative audit.
     *
     * @param noteId the unique identifier of the note to delete
     * @return HTTP 204 No Content upon successful deletion
     * @throws jakarta.ws.rs.ForbiddenException if the user is neither author nor supervisor
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - must be note author or SUPERVISOR"
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

    // ========================================
    // QUERY OPERATIONS
    // ========================================

    /**
     * Retrieves all active notes accessible to the user.
     *
     * Returns the list of all notes in ACTIVE status that the user has the
     * right to view according to visibility rules. Access filtering is applied
     * automatically at the application level.
     *
     * Returned notes respect the following rules:
     * - PRIVATE notes: Only those created by the user or shared with them
     * - SHARED notes: All notes with public visibility
     * - PROJECT notes: Only those from projects the user belongs to
     *
     * Archived notes (status=ARCHIVED) are not included in this list. Use the
     * dedicated endpoint to retrieve archived notes.
     *
     * @return HTTP 200 OK with the list of accessible active notes
     */
    @GET
    @Operation(
        summary = "Get all active notes",
        description = "Returns all active notes that the user has access to based on visibility rules. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Notes retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Note List Response",
                value = """
                [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "title": "How to fix null pointer exception",
                        "content": "Always check for null before dereferencing objects",
                        "contentType": "TEXT",
                        "category": "TIP",
                        "visibility": "SHARED",
                        "status": "ACTIVE",
                        "authorId": "660e8400-e29b-41d4-a716-446655440001",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440001",
                            "fullName": "John Doe"
                        },
                        "projectId": null,
                        "auditInfo": {
                            "createdAt": "2024-01-15T10:00:00Z",
                            "updatedAt": "2024-01-15T10:00:00Z"
                        }
                    },
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440002",
                        "title": "Understanding Java Streams",
                        "content": "Streams provide a functional approach to processing collections",
                        "contentType": "MARKDOWN",
                        "category": "DOCUMENTATION",
                        "visibility": "SHARED",
                        "status": "ACTIVE",
                        "authorId": "660e8400-e29b-41d4-a716-446655440002",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440002",
                            "fullName": "Jane Smith"
                        },
                        "projectId": null,
                        "auditInfo": {
                            "createdAt": "2024-01-16T14:30:00Z",
                            "updatedAt": "2024-01-16T14:30:00Z"
                        }
                    }
                ]
                """
            )
        )
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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
     * Retrieves all archived notes accessible to the current user.
     *
     * Returns notes with status = ARCHIVED, filtered by access rights:
     * - SUPERVISOR: Can view all archived notes from any author
     * - Regular user: Can view only their own archived notes
     *
     * Archived notes are read-only and cannot be edited or commented on.
     * They can be restored to ACTIVE status by the author or a SUPERVISOR.
     *
     * @return HTTP 200 OK with the list of accessible archived notes
     */
    @GET
    @Path("/archived")
    @Operation(
        summary = "Get archived notes",
        description = "Retrieve archived notes. SUPERVISOR sees all archived notes, regular users see only their own. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Archived notes retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                value = """
                    [
                        {
                            "id": "123e4567-e89b-12d3-a456-426614174000",
                            "title": "Old Meeting Notes",
                            "content": "Notes from Q1 planning",
                            "status": "ARCHIVED",
                            "category": "DOCUMENTATION",
                            "visibility": "SHARED",
                            "archivedAt": "2024-12-01T10:30:00Z",
                            "author": {
                                "userId": "user123",
                                "username": "john.doe"
                            }
                        }
                    ]
                    """
            )
        )
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response getArchivedNotes() {
        Log.info("Request received: GET /notes/archived - Retrieving archived notes");
        try {
            NoteUserContext userContext = userContextService.getCurrentContext();
            List<NoteDto> notes = noteQueryService.findArchivedNotes(
                    userContext.userId(),
                    userContext.isSupervisor()
            );
            Log.info("Request successful: GET /notes/archived - Retrieved " + notes.size() + " archived notes");
            return Response.ok(notes).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/archived - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all notes created by a specific author.
     *
     * Returns the list of notes whose author matches the provided identifier,
     * including only notes that the authenticated user has access to according
     * to visibility rules.
     *
     * A user can see their own PRIVATE notes, but will only see PRIVATE notes
     * from other authors if they are an explicit recipient. SHARED and PROJECT
     * notes are subject to the usual rules.
     *
     * Useful for displaying an author's profile or filtering contributions from
     * a specific team member.
     *
     * @param authorId the unique identifier of the author
     * @return HTTP 200 OK with the list of accessible notes from the author
     */
    @GET
    @Path("/author/{authorId}")
    @Operation(
        summary = "Get notes by author",
        description = "Returns all notes created by a specific author. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Notes retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Notes By Author Response",
                value = """
                [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "title": "How to fix null pointer exception",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440001",
                            "fullName": "John Doe"
                        },
                        "category": "TIP",
                        "visibility": "SHARED",
                        "status": "ACTIVE"
                    }
                ]
                """
            )
        )
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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
     * Retrieves all notes in a specific category.
     *
     * Returns the list of notes belonging to the specified category (QUESTION,
     * TIP, DOCUMENTATION), filtered according to the authenticated user's
     * access permissions.
     *
     * Categories allow classifying the type of content:
     * - QUESTION: Notes posing a question that requires an answer
     * - TIP: Tips and best practices to share
     * - DOCUMENTATION: Technical or procedural documentation
     *
     * Only active notes accessible according to visibility rules are included
     * in the results.
     *
     * @param category the category name (QUESTION, TIP, DOCUMENTATION)
     * @return HTTP 200 OK with the list of accessible notes in the category
     */
    @GET
    @Path("/category/{category}")
    @Operation(
        summary = "Get notes by category",
        description = "Returns all notes in a specific category (QUESTION, TIP, DOCUMENTATION). **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Notes retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Notes By Category Response",
                value = """
                [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "title": "How to configure JWT authentication?",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440001",
                            "fullName": "John Doe"
                        },
                        "category": "QUESTION",
                        "visibility": "SHARED",
                        "status": "ACTIVE"
                    }
                ]
                """
            )
        )
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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
     * Retrieves all notes linked to a specific project.
     *
     * Returns the list of notes with visibility=PROJECT and associated with
     * the project specified by its identifier. Access is limited to members
     * of the concerned project.
     *
     * PROJECT notes are contextual to a project and are only visible within
     * that project's context. They allow centralizing project-specific
     * documentation, questions, and tips.
     *
     * A user who is not a project member will receive an empty list or an
     * authorization error depending on the security filter implementation.
     *
     * @param projectId the unique identifier of the project
     * @return HTTP 200 OK with the list of accessible project notes
     */
    @GET
    @Path("/project/{projectId}")
    @Operation(
        summary = "Get notes by project",
        description = "Returns all notes linked to a specific project. **Required permission: Project member**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Notes retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Notes By Project Response",
                value = """
                [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "title": "Project Setup Guide",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440001",
                            "fullName": "John Doe"
                        },
                        "category": "DOCUMENTATION",
                        "visibility": "PROJECT",
                        "status": "ACTIVE",
                        "projectId": "770e8400-e29b-41d4-a716-446655440005"
                    }
                ]
                """
            )
        )
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - must be project member"
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
     * Retrieves all notes associated with a specific tag.
     *
     * Returns the list of notes that have been tagged with the specified tag,
     * filtered according to the authenticated user's access permissions.
     *
     * Tags allow flexible and multi-dimensional classification of notes (e.g.,
     * "java", "spring", "bug-fix", "performance"). A note can have multiple
     * tags, and this endpoint returns all notes having at least the specified tag.
     *
     * Only notes accessible according to visibility rules are included in the
     * results, even if they possess the tag.
     *
     * @param tagId the unique identifier of the tag
     * @return HTTP 200 OK with the list of accessible tagged notes
     */
    @GET
    @Path("/tag/{tagId}")
    @Operation(
        summary = "Get notes by tag",
        description = "Returns all notes that have been tagged with a specific tag. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Notes retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Notes By Tag Response",
                value = """
                [
                    {
                        "id": "550e8400-e29b-41d4-a716-446655440000",
                        "title": "Spring Boot configuration tips",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440001",
                            "fullName": "John Doe"
                        },
                        "category": "TIP",
                        "visibility": "SHARED",
                        "status": "ACTIVE"
                    }
                ]
                """
            )
        )
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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

    // ========================================
    // LIFECYCLE OPERATIONS
    // ========================================

    /**
     * Archives a note by removing it from active display.
     *
     * Changes a note from ACTIVE status to ARCHIVED status, removing it
     * from the main display and active note lists. The note remains
     * accessible through dedicated archive queries.
     *
     * Two categories of users can archive a note:
     * - The note author (managing their own content)
     * - Supervisors (administration and organization)
     *
     * Archiving preserves all data, comments, tags, and metadata.
     * Comments remain viewable but no new comments can be added
     * to an archived note.
     *
     * The operation is reversible via the restore endpoint.
     *
     * @param noteId the unique identifier of the note to archive
     * @return HTTP 200 OK upon successful archiving
     * @throws jakarta.ws.rs.ForbiddenException if the user is neither author nor supervisor
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - must be note author or SUPERVISOR"
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
     * Restores an archived note to active status.
     *
     * Changes a note from ARCHIVED status back to ACTIVE status, making it
     * visible again in the main display and active note lists. Reverses
     * the archiving operation.
     *
     * Two categories of users can restore a note:
     * - The note author (managing their own content)
     * - Supervisors (administration and organization)
     *
     * Restoration fully reactivates the note: it can receive comments again,
     * be modified, and appears in all active note queries.
     *
     * All comments, tags, and metadata are preserved and become
     * immediately accessible again.
     *
     * @param noteId the unique identifier of the note to restore
     * @return HTTP 200 OK upon successful restoration
     * @throws jakarta.ws.rs.ForbiddenException if the user is neither author nor supervisor
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - must be note author or SUPERVISOR"
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

    // ========================================
    // TAG MANAGEMENT
    // ========================================

    /**
     * Associates an existing tag with a note.
     *
     * Adds a tag to a note's tag list to enable its classification and
     * thematic indexing. The tag must already exist in the system
     * (created via TagResource).
     *
     * Only the note author can add tags, preserving control over their
     * content's classification. This restriction prevents tag spam and
     * maintains classification consistency.
     *
     * A note can have multiple tags for multi-dimensional classification
     * (e.g., "java", "spring", "performance"). Adding an already
     * associated tag is idempotent (no error).
     *
     * This operation automatically increments the tag's usageCount.
     *
     * @param noteId the unique identifier of the note
     * @param command validated data including the tag identifier
     * @return HTTP 200 OK upon successful association
     * @throws jakarta.ws.rs.ForbiddenException if the user is not the author
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - only note author can add tags"
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
     * Dissociates a tag from a note.
     *
     * Removes a tag from a note's associated tag list, eliminating that
     * classification dimension. The tag itself is not deleted from the
     * system and remains available for other notes.
     *
     * Only the note author can remove tags, preserving control over
     * their content's classification.
     *
     * This operation automatically decrements the tag's usageCount. If the
     * usageCount reaches 0, the tag becomes eligible for deletion via
     * TagResource (unused tags can be cleaned up).
     *
     * Removing an unassociated tag is idempotent (no error).
     *
     * @param noteId the unique identifier of the note
     * @param tagId the unique identifier of the tag to dissociate
     * @return HTTP 204 No Content upon successful dissociation
     * @throws jakarta.ws.rs.ForbiddenException if the user is not the author
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - only note author can remove tags"
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

    // ========================================
    // SHARING OPERATIONS
    // ========================================

    /**
     * Shares a note with a specific user.
     *
     * Adds a user to the note's recipients list, granting them explicit
     * access with a specified permission level (VIEWER, COMMENTER, EDITOR).
     *
     * Only the note author can share, controlling the distribution of their
     * content. This feature is particularly useful for PRIVATE notes,
     * allowing selective sharing without changing the global visibility.
     *
     * Possible access levels:
     * - VIEWER: Can read the note and its comments
     * - COMMENTER: Can read and add comments
     * - EDITOR: Can read, comment, and propose solutions
     *
     * Sharing a note already shared with the same user updates the
     * access level.
     *
     * @param noteId the unique identifier of the note
     * @param command validated data including userId and accessLevel
     * @return HTTP 200 OK upon successful sharing
     * @throws jakarta.ws.rs.ForbiddenException if the user is not the author
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - only note author can share"
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
     * Revokes a user's shared access to a note.
     *
     * Removes a user from the note's recipients list, revoking their
     * explicit access. The user will no longer be able to access the
     * note unless it has SHARED or PROJECT visibility and they have
     * access through those rules.
     *
     * Only the note author can revoke sharing, controlling the distribution
     * of their content. This operation is the inverse of the share operation.
     *
     * Revocation is immediate: the user instantly loses access and can no
     * longer view the note or its comments if it is PRIVATE.
     *
     * Revoking access for a user not present in the recipients list is
     * idempotent (no error).
     *
     * @param noteId the unique identifier of the note
     * @param userId the identifier of the user whose access is being revoked
     * @return HTTP 204 No Content upon successful revocation
     * @throws jakarta.ws.rs.ForbiddenException if the user is not the author
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - only note author can unshare"
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

    // ========================================
    // CONFIGURATION OPERATIONS
    // ========================================

    /**
     * Changes an existing note's category.
     *
     * Allows the author to change their note's classification category
     * between QUESTION, TIP, and DOCUMENTATION. This operation is separated
     * from general updates to precisely control categorization.
     *
     * Only the author can change the category as it reflects the intent
     * and content type of the note. Changing the category can affect how
     * the note is discovered and used by other users.
     *
     * Valid categories are:
     * - QUESTION: Note asking a question that needs an answer
     * - TIP: Tip or best practice to share
     * - DOCUMENTATION: Technical or procedural documentation
     *
     * The modification date (updatedAt) is updated automatically.
     *
     * @param noteId the unique identifier of the note
     * @param command validated data including the new category
     * @return HTTP 200 OK upon successful modification
     * @throws jakarta.ws.rs.ForbiddenException if the user is not the author
     */
    @PUT
    @Path("/{noteId}/category")
    @Operation(
        summary = "Change note category",
        description = "Note author can change the note's category. **Required permission: Note author**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Category changed successfully"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - note author only"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid category value"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response changeNoteCategory(
        @PathParam("noteId") UUID noteId,
        @Valid ChangeNoteCategoryCommand command) {

        Log.info("Request received: PUT /notes/" + noteId + "/category");
        try {
            if (!noteId.equals(command.noteId())) {
                throw new IllegalArgumentException("Path parameter must match command body");
            }

            changeNoteCategory.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: PUT /notes/" + noteId + "/category");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + "/category - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Changes an existing note's visibility.
     *
     * Allows the author to change their note's access rules between
     * PRIVATE, SHARED, and PROJECT. This operation controls who can
     * discover and access the note.
     *
     * Only the author can change visibility as it directly impacts
     * the confidentiality and distribution of the content.
     *
     * Possible visibility options:
     * - PRIVATE: Accessible only to the author and explicit recipients
     * - SHARED: Accessible to all authenticated users
     * - PROJECT: Accessible only within a project context (projectId required)
     *
     * Changing from PROJECT to PRIVATE/SHARED removes the project association.
     * Changing to PROJECT without providing a valid projectId generates a 400 error.
     *
     * Changing visibility immediately affects access permissions for
     * all users.
     *
     * @param noteId the unique identifier of the note
     * @param command validated data including new visibility and optional projectId
     * @return HTTP 200 OK upon successful modification
     * @throws jakarta.ws.rs.ForbiddenException if the user is not the author
     * @throws IllegalArgumentException if visibility=PROJECT without a valid projectId
     */
    @PUT
    @Path("/{noteId}/visibility")
    @Operation(
        summary = "Change note visibility",
        description = "Note author can change visibility (PRIVATE/SHARED/PROJECT). Requires projectId for PROJECT visibility. **Required permission: Note author**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Visibility changed successfully"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - note author only"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid visibility value or missing projectId for PROJECT visibility"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response changeNoteVisibility(
        @PathParam("noteId") UUID noteId,
        @Valid ChangeNoteVisibilityCommand command) {

        Log.info("Request received: PUT /notes/" + noteId + "/visibility");
        try {
            if (!noteId.equals(command.noteId())) {
                throw new IllegalArgumentException("Path parameter must match command body");
            }

            changeNoteVisibility.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: PUT /notes/" + noteId + "/visibility");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + "/visibility - " + e.getMessage());
            throw e;
        }
    }

    // ========================================
    // SOLUTION OPERATIONS
    // ========================================

    /**
     * Retrieves all comments proposed as solutions.
     *
     * Returns the list of comments that have been proposed as potential
     * solutions to the note (status=PROPOSED), awaiting validation by
     * the note author or a supervisor.
     *
     * This endpoint displays candidate solutions to facilitate the
     * validation process. The author can review all proposals and
     * choose the one that effectively solves their problem.
     *
     * Comments are filtered according to the note's access permissions.
     * Only PROPOSED comments (not REGULAR or VALIDATED) are returned.
     *
     * @param noteId the unique identifier of the note
     * @return HTTP 200 OK with the list of proposed solutions
     */
    @GET
    @Path("/{noteId}/solutions/proposed")
    @Operation(
        summary = "Get proposed solutions",
        description = "Get all comments proposed as solutions (awaiting validation). **Required permission: Based on note visibility**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Proposed solutions retrieved",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Proposed Solutions Response",
                value = """
                [
                    {
                        "id": "770e8400-e29b-41d4-a716-446655440003",
                        "authorId": "660e8400-e29b-41d4-a716-446655440001",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440001",
                            "fullName": "John Doe"
                        },
                        "content": "You can use Optional.ofNullable() to avoid NPE",
                        "parentCommentId": null,
                        "solutionStatus": "PROPOSED",
                        "moderationStatus": "VISIBLE",
                        "auditInfo": {
                            "createdAt": "2024-01-15T11:30:00Z",
                            "updatedAt": "2024-01-15T11:30:00Z"
                        }
                    }
                ]
                """
            )
        )
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized to view this note"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response getProposedSolutions(@PathParam("noteId") UUID noteId) {
        Log.info("Request received: GET /notes/" + noteId + "/solutions/proposed");
        try {
            List<CommentDto> solutions = commentQueryService.findProposedSolutions(
                noteId, userContextService.getCurrentContext());
            Log.info("Request successful: GET /notes/" + noteId + "/solutions/proposed - " + solutions.size() + " proposed solutions");
            return Response.ok(solutions).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/" + noteId + "/solutions/proposed - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves a note's validated solution.
     *
     * Returns the comment that has been validated as the official solution
     * to the note (status=VALIDATED). A note can only have one validated
     * solution at a time.
     *
     * This endpoint displays the accepted solution directly without browsing
     * through all comments. Particularly useful for users quickly seeking
     * the answer to a similar question.
     *
     * If no solution has been validated yet, the endpoint returns 404.
     * PROPOSED comments (awaiting validation) are not returned by this
     * endpoint - use /solutions/proposed to view them.
     *
     * Access is subject to the parent note's visibility rules.
     *
     * @param noteId the unique identifier of the note
     * @return HTTP 200 OK with the validated solution if it exists, HTTP 404 Not Found otherwise
     */
    @GET
    @Path("/{noteId}/solution")
    @Operation(
        summary = "Get validated solution",
        description = "Get the accepted solution for this note (if one exists). **Required permission: Based on note visibility**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Validated solution retrieved",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Validated Solution Response",
                value = """
                {
                    "id": "770e8400-e29b-41d4-a716-446655440003",
                    "authorId": "660e8400-e29b-41d4-a716-446655440001",
                    "author": {
                        "userId": "660e8400-e29b-41d4-a716-446655440001",
                        "fullName": "John Doe"
                    },
                    "content": "You can use Optional.ofNullable() to avoid NPE",
                    "parentCommentId": null,
                    "solutionStatus": "VALIDATED",
                    "moderationStatus": "VISIBLE",
                    "auditInfo": {
                        "createdAt": "2024-01-15T11:30:00Z",
                        "updatedAt": "2024-01-15T12:00:00Z"
                    }
                }
                """
            )
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found or no validated solution exists"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized to view this note"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response getValidatedSolution(@PathParam("noteId") UUID noteId) {
        Log.info("Request received: GET /notes/" + noteId + "/solution");
        try {
            Optional<CommentDto> solution = commentQueryService.findValidatedSolution(
                noteId, userContextService.getCurrentContext());

            if (solution.isEmpty()) {
                Log.info("No validated solution for note: " + noteId);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            Log.info("Request successful: GET /notes/" + noteId + "/solution");
            return Response.ok(solution.get()).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/" + noteId + "/solution - " + e.getMessage());
            throw e;
        }
    }
}
