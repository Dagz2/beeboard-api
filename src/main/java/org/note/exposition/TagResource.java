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

    // ========================================
    // CRUD OPERATIONS
    // ========================================

    /**
     * Creates a new tag in the system.
     *
     * Validates the input data and creates a tag with the specified name.
     * Tag names must be unique, lowercase, and contain only alphanumeric
     * characters and hyphens.
     *
     * The created tag has an initial usageCount of 0 and can be immediately
     * used to classify notes.
     *
     * @param command validated creation data including the tag name
     * @return HTTP 201 Created upon successful creation
     * @throws IllegalArgumentException if the name already exists or doesn't match the format
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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
     * Renames an existing tag.
     *
     * Changes the tag's name while preserving its identifier and usage counter.
     * The new name must respect the same constraints as during creation
     * (unique, lowercase, alphanumeric + hyphens).
     *
     * This operation does not modify the tag's associations with existing
     * notes. The usageCount remains unchanged.
     *
     * @param tagId the unique identifier of the tag to rename
     * @param command validated data including the new name
     * @return HTTP 200 OK upon successful rename
     * @throws IllegalArgumentException if the new name already exists or is invalid
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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
     * Deletes a tag from the system.
     *
     * Permanently removes a tag from the database. This operation is only
     * allowed if the tag is not used by any notes (usageCount = 0).
     *
     * This restriction prevents accidental deletion of tags that are still
     * referenced, which would cause inconsistencies in note classification.
     *
     * @param tagId the unique identifier of the tag to delete
     * @return HTTP 204 No Content upon successful deletion
     * @throws IllegalStateException if the tag is still in use (usageCount > 0)
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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

    // ========================================
    // QUERY OPERATIONS
    // ========================================

    /**
     * Retrieves a tag by its identifier.
     *
     * Searches for and returns a specific tag using its UUID. Includes the
     * name, usage counter, and audit metadata (creation and modification dates).
     *
     * @param tagId the unique identifier of the tag
     * @return HTTP 200 OK with the tag DTO if found, HTTP 404 Not Found otherwise
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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
     * Retrieves all tags from the system.
     *
     * Returns the complete list of all created tags, including their name,
     * usage counter, and metadata. Tags are sorted alphabetically by name.
     *
     * This operation is optimized for UI operations where all tags need to
     * be displayed (e.g., autocomplete, selection lists).
     *
     * @return HTTP 200 OK with the list of all tags
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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
     * Retrieves a tag by its name.
     *
     * Searches for a tag using its exact name (case-sensitive). Useful for
     * checking if a tag exists before creation or for retrieving a tag
     * without knowing its UUID.
     *
     * @param name the exact name of the tag to search for
     * @return HTTP 200 OK with the tag DTO if found, HTTP 404 Not Found otherwise
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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
     * Retrieves all unused tags.
     *
     * Returns the list of tags with a usageCount of 0, meaning tags that
     * are not associated with any notes. These tags are candidates for deletion.
     *
     * This query is useful for system administration and cleanup, allowing
     * identification and removal of obsolete tags.
     *
     * @return HTTP 200 OK with the list of unused tags
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
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated - JWT token missing or invalid"
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
}
