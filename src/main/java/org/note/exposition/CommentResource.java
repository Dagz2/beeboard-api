package org.note.exposition;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.note.application.dto.command.comment.*;
import org.note.application.usecase.command.comment.AddComment;
import org.note.application.usecase.command.comment.ValidateSolution;
import org.note.exposition.service.NoteUserContextService;

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

}
