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

    // ==================== Comment Operations ====================

    /**
     * POST /notes/{noteId}/comments
     * Adds a comment to a note.
     *
     * @param noteId the note ID
     * @param command the add comment command
     * @return 201 Created
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

    /**
     * PUT /notes/{noteId}/comments/{commentId}/validate-solution
     * Validates a proposed solution.
     *
     * @param noteId the note ID
     * @param commentId the comment ID
     * @return 200 OK
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

    // ==================== Query Operations ====================

    /**
     * GET /notes/{noteId}/comments
     * Get all comments for a note.
     *
     * @param noteId the note ID
     * @return 200 OK with list of comments
     */
    @GET
    @Operation(
        summary = "Get all comments for note",
        description = "Retrieve all comments for a note. Censored comments are hidden from regular users but visible to moderators/supervisors."
    )
    @APIResponse(
        responseCode = "200",
        description = "Comments retrieved successfully"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized to view this note"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note not found"
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
     * GET /notes/{noteId}/comments/{commentId}
     * Get a single comment by ID.
     *
     * @param noteId the note ID
     * @param commentId the comment ID
     * @return 200 OK with comment data
     */
    @GET
    @Path("/{commentId}")
    @Operation(
        summary = "Get comment by ID",
        description = "Retrieve a single comment by its ID"
    )
    @APIResponse(
        responseCode = "200",
        description = "Comment retrieved successfully"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or comment not found"
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
     * GET /notes/{noteId}/comments/{commentId}/thread
     * Get comment thread (parent + replies).
     *
     * @param noteId the note ID
     * @param commentId the parent comment ID
     * @return 200 OK with thread comments
     */
    @GET
    @Path("/{commentId}/thread")
    @Operation(
        summary = "Get comment thread",
        description = "Get parent comment and all its replies"
    )
    @APIResponse(
        responseCode = "200",
        description = "Thread retrieved successfully"
    )
    @APIResponse(
        responseCode = "403",
        description = "Not authorized"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or comment not found"
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

    // ==================== Comment Editing Operations ====================

    /**
     * PUT /notes/{noteId}/comments/{commentId}
     * Edit comment content.
     *
     * @param noteId the note ID
     * @param commentId the comment ID
     * @param command the edit command
     * @return 200 OK
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
     * DELETE /notes/{noteId}/comments/{commentId}
     * Delete comment (soft delete).
     *
     * @param noteId the note ID
     * @param commentId the comment ID
     * @return 204 No Content
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
        description = "Not authorized"
    )
    @APIResponse(
        responseCode = "404",
        description = "Note or comment not found"
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

    // ==================== Solution Operations ====================

    /**
     * PUT /notes/{noteId}/comments/{commentId}/propose-solution
     * Propose comment as solution.
     *
     * @param noteId the note ID
     * @param commentId the comment ID
     * @return 200 OK
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
     * PUT /notes/{noteId}/comments/{commentId}/reject-solution
     * Reject proposed solution.
     *
     * @param noteId the note ID
     * @param commentId the comment ID
     * @return 200 OK
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

    // ==================== Moderation Operations ====================

    /**
     * PUT /notes/{noteId}/comments/{commentId}/censor
     * Censor comment (moderation).
     *
     * @param noteId the note ID
     * @param commentId the comment ID
     * @return 200 OK
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
     * PUT /notes/{noteId}/comments/{commentId}/restore
     * Restore censored comment.
     *
     * @param noteId the note ID
     * @param commentId the comment ID
     * @return 200 OK
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
