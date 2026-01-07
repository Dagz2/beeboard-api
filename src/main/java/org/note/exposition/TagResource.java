package org.note.exposition;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.note.application.dto.command.tag.CreateTagCommand;
import org.note.application.dto.command.tag.DeleteTagCommand;
import org.note.application.dto.command.tag.RenameTagCommand;
import org.note.application.dto.query.TagDto;
import org.note.application.usecase.command.tag.CreateTag;
import org.note.application.usecase.command.tag.DeleteTag;
import org.note.application.usecase.command.tag.RenameTag;
import org.note.application.usecase.query.TagQueryService;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.note.exposition.service.NoteUserContextService;
import io.quarkus.logging.Log;

import java.util.List;
import java.util.UUID;

/**
 * JAX-RS resource for Tag management endpoints.
 * Base path: /api/v1/tags
 */
@Path("/api/v1/tags")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Tag Management", description = "Endpoints for managing tags used to classify notes")
@SecurityRequirement(name = "JWT")
@ApplicationScoped
public class TagResource {

    @Inject
    NoteUserContextService userContextService;

    // Command use cases
    @Inject
    CreateTag createTag;

    @Inject
    RenameTag renameTag;

    @Inject
    DeleteTag deleteTag;

    // Query service
    @Inject
    TagQueryService tagQueryService;

    // ==================== Tag CRUD Operations ====================

    /**
     * POST /tags
     * Creates a new tag.
     *
     * @param command the create tag command
     * @return 201 Created
     */
    @POST
    @Operation(
        summary = "Create a new tag",
        description = "Creates a new tag that can be used to classify notes. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "201",
        description = "Tag created successfully"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid input data or tag name already exists"
    )
    @RequestBody(
        description = "Tag creation data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Create Tag Example",
                value = """
                {
                    "name": "java"
                }
                """
            )
        )
    )
    public Response createTag(@Valid CreateTagCommand command) {
        Log.info("Request received: POST /tags - Creating tag: " + command.name());
        try {
            createTag.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: POST /tags - Tag created successfully: " + command.name());
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            Log.error("Request failed: POST /tags - Failed to create tag: " + command.name() + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * GET /tags/{tagId}
     * Gets a tag by ID.
     *
     * @param tagId the tag ID
     * @return 200 OK with tag DTO, or 404 Not Found
     */
    @GET
    @Path("/{tagId}")
    @Operation(
        summary = "Get tag by ID",
        description = "Retrieves a specific tag by its unique identifier. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Tag found successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Tag Response",
                value = """
                {
                    "tagId": "770e8400-e29b-41d4-a716-446655440002",
                    "name": "java",
                    "usageCount": 5,
                    "createdAt": "2024-01-10T09:00:00Z",
                    "updatedAt": "2024-01-15T14:30:00Z"
                }
                """
            )
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Tag not found"
    )
    public Response getTagById(@PathParam("tagId") UUID tagId) {
        Log.info("Request received: GET /tags/" + tagId + " - Retrieving tag by ID");
        try {
            var tag = tagQueryService.findById(tagId);
            if (tag.isPresent()) {
                Log.info("Request successful: GET /tags/" + tagId + " - Tag found");
                return Response.ok(tag.get()).build();
            }
            Log.info("Request completed: GET /tags/" + tagId + " - Tag not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /tags/" + tagId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * GET /tags
     * Gets all tags.
     *
     * @return 200 OK with list of tags
     */
    @GET
    @Operation(
        summary = "Get all tags",
        description = "Returns a complete list of all tags. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Tags retrieved successfully"
    )
    public Response getAllTags() {
        Log.info("Request received: GET /tags - Retrieving all tags");
        try {
            List<TagDto> tags = tagQueryService.findAllTags();
            Log.info("Request successful: GET /tags - Retrieved " + tags.size() + " tags");
            return Response.ok(tags).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /tags - " + e.getMessage());
            throw e;
        }
    }

    /**
     * GET /tags/name/{name}
     * Gets a tag by name.
     *
     * @param name the tag name
     * @return 200 OK with tag DTO, or 404 Not Found
     */
    @GET
    @Path("/name/{name}")
    @Operation(
        summary = "Get tag by name",
        description = "Retrieves a tag by its name. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Tag found successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Tag not found"
    )
    public Response getTagByName(@PathParam("name") String name) {
        Log.info("Request received: GET /tags/name/" + name + " - Retrieving tag by name");
        try {
            var tag = tagQueryService.findByName(name);
            if (tag.isPresent()) {
                Log.info("Request successful: GET /tags/name/" + name + " - Tag found");
                return Response.ok(tag.get()).build();
            }
            Log.info("Request completed: GET /tags/name/" + name + " - Tag not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /tags/name/" + name + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * GET /tags/unused
     * Gets all unused tags (usageCount = 0).
     *
     * @return 200 OK with list of unused tags
     */
    @GET
    @Path("/unused")
    @Operation(
        summary = "Get unused tags",
        description = "Returns all tags that are not currently used by any notes. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Unused tags retrieved successfully"
    )
    public Response getUnusedTags() {
        Log.info("Request received: GET /tags/unused - Retrieving unused tags");
        try {
            List<TagDto> tags = tagQueryService.findUnusedTags();
            Log.info("Request successful: GET /tags/unused - Retrieved " + tags.size() + " unused tags");
            return Response.ok(tags).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /tags/unused - " + e.getMessage());
            throw e;
        }
    }

    /**
     * PUT /tags/{tagId}
     * Renames a tag.
     *
     * @param tagId the tag ID
     * @param command the rename command
     * @return 200 OK
     */
    @PUT
    @Path("/{tagId}")
    @Operation(
        summary = "Rename tag",
        description = "Renames an existing tag. **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Tag renamed successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Tag not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid tag name or name already exists"
    )
    @RequestBody(
        description = "Tag rename data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Rename Tag Example",
                value = """
                {
                    "tagId": "770e8400-e29b-41d4-a716-446655440002",
                    "newName": "java-17"
                }
                """
            )
        )
    )
    public Response renameTag(@PathParam("tagId") UUID tagId,
                             @Valid RenameTagCommand command) {
        Log.info("Request received: PUT /tags/" + tagId + " - Renaming tag to: " + command.newName());
        try {
            renameTag.execute(command, userContextService.getCurrentContext());
            Log.info("Request successful: PUT /tags/" + tagId + " - Tag renamed successfully to: " + command.newName());
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /tags/" + tagId + " - Failed to rename tag - " + e.getMessage());
            throw e;
        }
    }

    /**
     * DELETE /tags/{tagId}
     * Deletes a tag (only if unused).
     *
     * @param tagId the tag ID
     * @return 204 No Content
     */
    @DELETE
    @Path("/{tagId}")
    @Operation(
        summary = "Delete tag",
        description = "Deletes a tag. Can only delete unused tags (usageCount = 0). **Required permission: Authenticated user**"
    )
    @APIResponse(
        responseCode = "204",
        description = "Tag deleted successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Tag not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Tag is still in use and cannot be deleted"
    )
    public Response deleteTag(@PathParam("tagId") UUID tagId) {
        Log.info("Request received: DELETE /tags/" + tagId + " - Deleting tag");
        try {
            deleteTag.execute(new DeleteTagCommand(tagId));
            Log.info("Request successful: DELETE /tags/" + tagId + " - Tag deleted successfully");
            return Response.noContent().build();
        } catch (Exception e) {
            Log.error("Request failed: DELETE /tags/" + tagId + " - Failed to delete tag - " + e.getMessage());
            throw e;
        }
    }
}
