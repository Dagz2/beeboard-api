package org.note.exposition;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.note.application.dto.command.comment.*;
import org.note.application.dto.query.CommentDto;
import org.note.application.usecase.command.comment.*;
import org.note.application.usecase.query.CommentQueryService;
import org.note.exposition.service.NoteUserContextService;

import java.util.List;
import java.util.Optional;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import io.quarkus.logging.Log;

import java.util.UUID;

/**
 * JAX-RS resource for Comment management endpoints.
 * Comments are accessed as sub-resources of notes: /api/v1/notes/{noteId}/comments
 */
@Path("/api/v1/notes/{noteId}/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Comment Management", description = "Endpoints for managing comments and solutions on notes")
@SecurityRequirement(name = "JWT")
@ApplicationScoped
public class CommentResource {

    @Inject
    NoteUserContextService userContextService;

    // Command use cases
    @Inject
    AddComment addComment;

    @Inject
    ValidateSolution validateSolution;

    @Inject
    EditComment editComment;

    @Inject
    DeleteComment deleteComment;

    @Inject
    ProposeCommentAsSolution proposeCommentAsSolution;

    @Inject
    RejectSolution rejectSolution;

    @Inject
    CensorComment censorComment;

    @Inject
    RestoreComment restoreComment;

    // Query service
    @Inject
    CommentQueryService commentQueryService;

    // ========================================
    // BASIC OPERATIONS
    // ========================================

    /**
     * Adds a comment to a note.
     *
     * Creates a new comment on an existing note with the specified content.
     * The comment author is automatically set to the current authenticated user.
     *
     * The note must be in ACTIVE status to accept comments. The user must have
     * at least COMMENTER access level and have permission to view the note
     * according to its visibility rules.
     *
     * The comment can be a reply to another comment by specifying a
     * parentCommentId to create a threaded discussion.
     *
     * @param noteId the unique identifier of the note
     * @param command validated data including content and optionally the parent comment
     * @return HTTP 201 Created upon successful creation
     * @throws IllegalStateException if the note is not in ACTIVE status
     */
    @POST
    @Operation(
        summary = "Add comment to note",
        description = "Adds a comment to a note. Requires appropriate access to the note. **Required permission: Viewer or higher**"
    )
    @APIResponse(
        responseCode = "201",
        description = "Comment added successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid input data or note is not active"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized to view or comment on this note"
    )
    @RequestBody(
        description = "Add comment data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Add Comment Example",
                value = """
                {
                    "noteId": "550e8400-e29b-41d4-a716-446655440000",
                    "content": "Have you tried using Optional.ofNullable()?"
                }
                """
            )
        )
    )
    public Response addComment(@PathParam("noteId") UUID noteId,
                               @Valid AddCommentCommand command) {
        Log.info("Request received: POST /notes/" + noteId + "/comments - Adding comment to note");
        try {
            addComment.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: POST /notes/" + noteId + "/comments - Comment added successfully");
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            Log.error("Request failed: POST /notes/" + noteId + "/comments - Failed to add comment - " + e.getMessage());
            throw e;
        }
    }

    // ========================================
    // QUERY OPERATIONS
    // ========================================

    /**
     * Retrieves all comments for a note.
     *
     * Returns the complete list of comments associated with a note,  including
     * metadata (author, dates, solution status, moderation status).
     *
     * Censored comments (CENSORED) are automatically filtered for regular users
     * but remain visible to moderators and supervisors for audit purposes.
     *
     * Comments are sorted by creation date (oldest to newest) to facilitate
     * reading the discussion thread.
     *
     * @param noteId the unique identifier of the note
     * @return HTTP 200 OK with the list of comments
     * @throws jakarta.ws.rs.ForbiddenException if the user doesn't have access to the note
     */
    @GET
    @Operation(
        summary = "Get all comments for note",
        description = "Retrieve all comments for a note. Censored comments are hidden from regular users but visible to moderators/supervisors. **Required permission: Viewer or higher**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Comments retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Comments List Response",
                value = """
                [
                    {
                        "id": "770e8400-e29b-41d4-a716-446655440003",
                        "authorId": "660e8400-e29b-41d4-a716-446655440001",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440001",
                            "fullName": "John Doe"
                        },
                        "content": "Have you tried using Optional.ofNullable()?",
                        "parentCommentId": null,
                        "solutionStatus": "REGULAR",
                        "moderationStatus": "VISIBLE",
                        "auditInfo": {
                            "createdAt": "2024-01-15T11:30:00Z",
                            "updatedAt": "2024-01-15T11:30:00Z"
                        }
                    },
                    {
                        "id": "770e8400-e29b-41d4-a716-446655440004",
                        "authorId": "660e8400-e29b-41d4-a716-446655440002",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440002",
                            "fullName": "Jane Smith"
                        },
                        "content": "That works, thanks!",
                        "parentCommentId": "770e8400-e29b-41d4-a716-446655440003",
                        "solutionStatus": "REGULAR",
                        "moderationStatus": "VISIBLE",
                        "auditInfo": {
                            "createdAt": "2024-01-15T12:00:00Z",
                            "updatedAt": "2024-01-15T12:00:00Z"
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
    public Response getComments(@PathParam("noteId") UUID noteId) {
        Log.info("Request received: GET /notes/" + noteId + "/comments");
        try {
            List<CommentDto> comments = commentQueryService.findByNoteId(
                noteId, userContextService.getCurrentContext());
            Log.info("Request successful: GET /notes/" + noteId + "/comments - " + comments.size() + " comments retrieved");
            return Response.ok(comments).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/" + noteId + "/comments - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves a specific comment by its identifier.
     *
     * Returns the complete details of a comment using its UUID, including the
     * author, content, creation and modification dates, solution status
     * (REGULAR/PROPOSED/VALIDATED), and moderation status
     * (VISIBLE/CENSORED/UNDER_REVIEW).
     *
     * Access to the comment requires having read permissions on the parent
     * note according to its visibility rules.
     *
     * @param noteId the unique identifier of the parent note
     * @param commentId the unique identifier of the comment
     * @return HTTP 200 OK with the comment DTO if found, HTTP 404 Not Found otherwise
     */
    @GET
    @Path("/{commentId}")
    @Operation(
        summary = "Get comment by ID",
        description = "Retrieve a single comment by its ID. **Required permission: Viewer or higher**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Comment retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Comment Response",
                value = """
                {
                    "id": "770e8400-e29b-41d4-a716-446655440003",
                    "authorId": "660e8400-e29b-41d4-a716-446655440001",
                    "author": {
                        "userId": "660e8400-e29b-41d4-a716-446655440001",
                        "fullName": "John Doe"
                    },
                    "content": "Have you tried using Optional.ofNullable()?",
                    "parentCommentId": null,
                    "solutionStatus": "REGULAR",
                    "moderationStatus": "VISIBLE",
                    "auditInfo": {
                        "createdAt": "2024-01-15T11:30:00Z",
                        "updatedAt": "2024-01-15T11:30:00Z"
                    }
                }
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
        description = "Note or comment not found"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response getComment(
        @PathParam("noteId") UUID noteId,
        @PathParam("commentId") UUID commentId) {

        Log.info("Request received: GET /notes/" + noteId + "/comments/" + commentId);
        try {
            Optional<CommentDto> comment = commentQueryService.findById(
                noteId, commentId, userContextService.getCurrentContext());

            if (comment.isEmpty()) {
                Log.warn("Comment not found: " + commentId);
                return Response.status(Response.Status.NOT_FOUND).build();
            }

            Log.info("Request successful: GET /notes/" + noteId + "/comments/" + commentId);
            return Response.ok(comment.get()).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/" + noteId + "/comments/" + commentId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves a complete discussion thread for a comment.
     *
     * Returns the specified parent comment along with all its direct replies,
     * forming a hierarchical discussion thread. Useful for displaying a
     * complete contextual conversation.
     *
     * Comments are sorted chronologically, with the parent first followed by
     * its replies in creation order. Censored replies are filtered according
     * to the user's permissions.
     *
     * This operation is optimized for UI display of nested discussions,
     * avoiding multiple queries to construct the thread.
     *
     * @param noteId the unique identifier of the parent note
     * @param commentId the identifier of the parent comment in the thread
     * @return HTTP 200 OK with the ordered list of the thread (parent + replies)
     */
    @GET
    @Path("/{commentId}/thread")
    @Operation(
        summary = "Get comment thread",
        description = "Get parent comment and all its replies. **Required permission: Viewer or higher**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Thread retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Comment Thread Response",
                value = """
                [
                    {
                        "id": "770e8400-e29b-41d4-a716-446655440003",
                        "authorId": "660e8400-e29b-41d4-a716-446655440001",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440001",
                            "fullName": "John Doe"
                        },
                        "content": "Have you tried using Optional.ofNullable()?",
                        "parentCommentId": null,
                        "solutionStatus": "REGULAR",
                        "moderationStatus": "VISIBLE",
                        "auditInfo": {
                            "createdAt": "2024-01-15T11:30:00Z",
                            "updatedAt": "2024-01-15T11:30:00Z"
                        }
                    },
                    {
                        "id": "770e8400-e29b-41d4-a716-446655440004",
                        "authorId": "660e8400-e29b-41d4-a716-446655440002",
                        "author": {
                            "userId": "660e8400-e29b-41d4-a716-446655440002",
                            "fullName": "Jane Smith"
                        },
                        "content": "That works, thanks!",
                        "parentCommentId": "770e8400-e29b-41d4-a716-446655440003",
                        "solutionStatus": "REGULAR",
                        "moderationStatus": "VISIBLE",
                        "auditInfo": {
                            "createdAt": "2024-01-15T12:00:00Z",
                            "updatedAt": "2024-01-15T12:00:00Z"
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
        description = "Note or comment not found"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response getCommentThread(
        @PathParam("noteId") UUID noteId,
        @PathParam("commentId") UUID commentId) {

        Log.info("Request received: GET /notes/" + noteId + "/comments/" + commentId + "/thread");
        try {
            List<CommentDto> thread = commentQueryService.findThreadByCommentId(
                noteId, commentId, userContextService.getCurrentContext());
            Log.info("Request successful: GET /notes/" + noteId + "/comments/" + commentId + "/thread - " +
                    thread.size() + " comments in thread");
            return Response.ok(thread).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /notes/" + noteId + "/comments/" + commentId + "/thread - " + e.getMessage());
            throw e;
        }
    }

    // ========================================
    // CRUD OPERATIONS
    // ========================================

    /**
     * Modifies the content of an existing comment.
     *
     * Allows the comment author to modify their comment's text content at
     * any time after creation, with no time limit. This flexibility allows
     * correcting errors or improving clarity.
     *
     * Only the original comment author can perform this modification.
     * Moderators and supervisors cannot edit others' comments to preserve
     * content integrity.
     *
     * The modification date (updatedAt) is automatically updated to indicate
     * that the content has been modified.
     *
     * @param noteId the unique identifier of the parent note
     * @param commentId the unique identifier of the comment to modify
     * @param command validated data including the new content
     * @return HTTP 200 OK upon successful modification
     * @throws jakarta.ws.rs.ForbiddenException if the user is not the author
     */
    @PUT
    @Path("/{commentId}")
    @Operation(
        summary = "Edit comment content",
        description = "Allows comment author to edit their comment content. No time limit. **Required permission: Comment author only**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Comment edited successfully"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - only comment author can edit"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or comment not found"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response editComment(
        @PathParam("noteId") UUID noteId,
        @PathParam("commentId") UUID commentId,
        @Valid EditCommentCommand command) {

        Log.info("Request received: PUT /notes/" + noteId + "/comments/" + commentId);
        try {
            // Ensure path params match command body
            if (!noteId.equals(command.noteId()) || !commentId.equals(command.commentId())) {
                throw new IllegalArgumentException("Path parameters must match command body");
            }

            editComment.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: PUT /notes/" + noteId + "/comments/" + commentId);
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + "/comments/" + commentId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes a comment from the system (soft delete).
     *
     * Performs a logical deletion of the comment by hiding it from users
     * while preserving the data for audit and traceability. The comment is
     * not physically deleted from the database.
     *
     * Three categories of users can delete a comment:
     * - The comment author (can delete their own comments)
     * - Moderators (can delete any inappropriate comment)
     * - Supervisors (can delete any comment)
     *
     * This operation is irreversible from the user's perspective, but the
     * data remains accessible for administrative audit.
     *
     * @param noteId the unique identifier of the parent note
     * @param commentId the unique identifier of the comment to delete
     * @return HTTP 204 No Content upon successful deletion
     * @throws jakarta.ws.rs.ForbiddenException if the user doesn't have permissions
     */
    @DELETE
    @Path("/{commentId}")
    @Operation(
        summary = "Delete comment",
        description = "Soft delete comment - hides it from users but keeps for audit trail. Author, moderators, or supervisors can delete. **Required permission: Comment author, moderator, or supervisor**"
    )
    @APIResponse(
        responseCode = "204",
        description = "Comment deleted successfully"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - must be comment author, moderator, or supervisor"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or comment not found"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response deleteComment(
        @PathParam("noteId") UUID noteId,
        @PathParam("commentId") UUID commentId) {

        Log.info("Request received: DELETE /notes/" + noteId + "/comments/" + commentId);
        try {
            DeleteCommentCommand command = new DeleteCommentCommand(noteId, commentId);
            deleteComment.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: DELETE /notes/" + noteId + "/comments/" + commentId);
            return Response.noContent().build();
        } catch (Exception e) {
            Log.error("Request failed: DELETE /notes/" + noteId + "/comments/" + commentId + " - " + e.getMessage());
            throw e;
        }
    }

    // ========================================
    // SOLUTION WORKFLOW
    // ========================================

    /**
     * Proposes a comment as a solution to the note.
     *
     * Allows the comment author to propose their comment as a potential
     * solution to the question or problem posed in the note. The comment
     * transitions from REGULAR status to PROPOSED status.
     *
     * Only the comment author can propose their own comment as a solution.
     * This self-proposal allows contributors to flag their answers that they
     * believe solve the problem.
     *
     * The comment must be in REGULAR status to be proposed. Comments already
     * PROPOSED or VALIDATED cannot be re-proposed.
     *
     * Once proposed, the comment awaits validation by the note author or a
     * supervisor via the validate-solution endpoint.
     *
     * @param noteId the unique identifier of the parent note
     * @param commentId the unique identifier of the comment to propose
     * @return HTTP 200 OK upon successful proposal
     * @throws jakarta.ws.rs.ForbiddenException if the user is not the comment author
     * @throws IllegalStateException if the comment is not in REGULAR status
     */
    @PUT
    @Path("/{commentId}/propose-solution")
    @Operation(
        summary = "Propose comment as solution",
        description = "Comment author can propose their comment as a solution to the note. **Required permission: Comment author only**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Comment proposed as solution"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - only comment author can propose"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or comment not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Comment is not in REGULAR status"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response proposeCommentAsSolution(
        @PathParam("noteId") UUID noteId,
        @PathParam("commentId") UUID commentId) {

        Log.info("Request received: PUT /notes/" + noteId + "/comments/" + commentId + "/propose-solution");
        try {
            ProposeCommentAsSolutionCommand command = new ProposeCommentAsSolutionCommand(noteId, commentId);
            proposeCommentAsSolution.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: PUT /notes/" + noteId + "/comments/" + commentId + "/propose-solution");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + "/comments/" + commentId + "/propose-solution - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Validates a proposed comment as the official solution.
     *
     * Confirms that a comment in PROPOSED status is indeed the solution to
     * the problem posed in the note. The comment transitions to VALIDATED
     * status, marking the resolution of the question.
     *
     * Two categories of users can validate a solution:
     * - The note author (acknowledges that their question has been resolved)
     * - Supervisors (can validate for quality control)
     *
     * The comment must be in PROPOSED status to be validated. REGULAR
     * comments cannot be directly validated without going through the
     * proposal step.
     *
     * Once validated, the solution is highlighted in the interface to help
     * future users with similar questions.
     *
     * @param noteId the unique identifier of the parent note
     * @param commentId the unique identifier of the comment to validate
     * @return HTTP 200 OK upon successful validation
     * @throws jakarta.ws.rs.ForbiddenException if the user is neither author nor supervisor
     * @throws IllegalStateException if the comment is not in PROPOSED status
     */
    @PUT
    @Path("/{commentId}/validate-solution")
    @Operation(
        summary = "Validate solution",
        description = "Validates a proposed comment as the solution. Only the note author or SUPERVISOR can validate. **Required permission: Note author or SUPERVISOR**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Solution validated successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or comment not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Comment is not a proposed solution"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - must be note author or SUPERVISOR"
    )
    public Response validateSolution(@PathParam("noteId") UUID noteId,
                                     @PathParam("commentId") UUID commentId) {
        Log.info("Request received: PUT /notes/" + noteId + "/comments/" + commentId + "/validate-solution - Validating solution");
        try {
            validateSolution.execute(
                    new ValidateSolutionCommand(noteId, commentId),
                    userContextService.getCurrentContext()
            );
            Log.info("Request successful: PUT /notes/" + noteId + "/comments/" + commentId + "/validate-solution - Solution validated successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + "/comments/" + commentId + "/validate-solution - Failed to validate solution - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Rejects a proposed solution and returns it to normal status.
     *
     * Allows refusing a comment proposed as a solution (PROPOSED status) and
     * returning it to REGULAR status. Useful when the proposed solution is
     * not satisfactory or doesn't fully solve the problem.
     *
     * Two categories of users can reject a solution:
     * - The note author (considers that the proposal doesn't solve their problem)
     * - Supervisors (can reject for quality control)
     *
     * The comment must be in PROPOSED status to be rejected. An already
     * VALIDATED solution cannot be rejected via this endpoint.
     *
     * After rejection, the comment author can improve their answer and
     * re-propose it later via propose-solution.
     *
     * @param noteId the unique identifier of the parent note
     * @param commentId the unique identifier of the comment to reject
     * @return HTTP 200 OK upon successful rejection
     * @throws jakarta.ws.rs.ForbiddenException if the user is neither author nor supervisor
     * @throws IllegalStateException if the comment is not in PROPOSED status
     */
    @PUT
    @Path("/{commentId}/reject-solution")
    @Operation(
        summary = "Reject proposed solution",
        description = "Note author or supervisor can reject a proposed solution, returning it to regular status. **Required permission: Note author or supervisor**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Solution rejected"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - note author or supervisor only"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or comment not found"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response rejectSolution(
        @PathParam("noteId") UUID noteId,
        @PathParam("commentId") UUID commentId) {

        Log.info("Request received: PUT /notes/" + noteId + "/comments/" + commentId + "/reject-solution");
        try {
            RejectSolutionCommand command = new RejectSolutionCommand(noteId, commentId);
            rejectSolution.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: PUT /notes/" + noteId + "/comments/" + commentId + "/reject-solution");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + "/comments/" + commentId + "/reject-solution - " + e.getMessage());
            throw e;
        }
    }

    // ========================================
    // MODERATION OPERATIONS
    // ========================================

    /**
     * Censors an inappropriate comment (moderation).
     *
     * Marks a comment as censored, hiding it from public display while
     * preserving the data for audit and traceability. The status transitions
     * from VISIBLE to CENSORED.
     *
     * Only moderators and supervisors can censor comments, reflecting their
     * content moderation responsibilities. This action is typically used to
     * manage comments containing inappropriate, offensive content, or content
     * violating platform rules.
     *
     * Censored comments remain visible to moderators and supervisors in the
     * administration interface to allow review and quality control of
     * moderation decisions.
     *
     * Regular users do not see censored comments, thus preserving a healthy
     * discussion environment.
     *
     * @param noteId the unique identifier of the parent note
     * @param commentId the unique identifier of the comment to censor
     * @return HTTP 200 OK upon successful censorship
     * @throws jakarta.ws.rs.ForbiddenException if the user is neither moderator nor supervisor
     */
    @PUT
    @Path("/{commentId}/censor")
    @Operation(
        summary = "Censor comment",
        description = "Moderator or supervisor can censor inappropriate comment - hides from users but visible for audit. **Required permission: Moderator or supervisor**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Comment censored"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - moderator or supervisor only"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or comment not found"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response censorComment(
        @PathParam("noteId") UUID noteId,
        @PathParam("commentId") UUID commentId) {

        Log.info("Request received: PUT /notes/" + noteId + "/comments/" + commentId + "/censor");
        try {
            CensorCommentCommand command = new CensorCommentCommand(noteId, commentId);
            censorComment.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: PUT /notes/" + noteId + "/comments/" + commentId + "/censor");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + "/comments/" + commentId + "/censor - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Restores a censored comment to visible status.
     *
     * Reverses the censorship of a previously censored comment, making it
     * visible to the public again. The status transitions from CENSORED to
     * VISIBLE.
     *
     * Only moderators and supervisors can restore comments, as with the
     * censorship operation. This action is used when a review reveals that
     * the censorship was inappropriate or following a modification of the
     * comment by its author.
     *
     * This operation allows correcting excessive or erroneous moderation
     * decisions, maintaining the balance between active moderation and
     * constructive freedom of expression.
     *
     * Restoration can also be used after an initially problematic comment
     * has been edited by its author to comply with platform rules.
     *
     * @param noteId the unique identifier of the parent note
     * @param commentId the unique identifier of the comment to restore
     * @return HTTP 200 OK upon successful restoration
     * @throws jakarta.ws.rs.ForbiddenException if the user is neither moderator nor supervisor
     */
    @PUT
    @Path("/{commentId}/restore")
    @Operation(
        summary = "Restore censored comment",
        description = "Moderator or supervisor can restore a censored comment to visible status. **Required permission: Moderator or supervisor**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Comment restored"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized - moderator or supervisor only"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or comment not found"
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
    )
    public Response restoreComment(
        @PathParam("noteId") UUID noteId,
        @PathParam("commentId") UUID commentId) {

        Log.info("Request received: PUT /notes/" + noteId + "/comments/" + commentId + "/restore");
        try {
            RestoreCommentCommand command = new RestoreCommentCommand(noteId, commentId);
            restoreComment.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: PUT /notes/" + noteId + "/comments/" + commentId + "/restore");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /notes/" + noteId + "/comments/" + commentId + "/restore - " + e.getMessage());
            throw e;
        }
    }

}
