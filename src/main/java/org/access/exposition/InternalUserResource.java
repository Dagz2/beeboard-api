package org.access.exposition;

import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.access.domain.model.Role;
import org.access.domain.model.User;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.access.exposition.dto.UserSyncResponse;
import org.shared.domain.AccountStatus;
import org.shared.domain.UserContext;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Internal API for cross-BC communication.
 *
 * This endpoint is NOT exposed publicly - only for BC-to-BC communication.
 * Should be protected by internal network rules or service mesh.
 *
 * @PermitAll - allows internal service-to-service calls without JWT
 */
@Path("/api/v1/internal/users")
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "Internal API", description = "Internal endpoints for inter-BC communication (not public)")
public class InternalUserResource {

    @Inject
    UserRepository userRepository;

    @Inject
    RoleRepository roleRepository;



    /**
     * GET /internal/users/all
     *
     * Returns all users with their full context for initial synchronization.
     * Used by Note BC to populate author_view on first deployment.
     *
     * NOTE: This endpoint is intended for ONE-TIME synchronization.
     * Regular updates should use domain events.
     *
     * @return 200 OK with List of UserSyncResponse
     */
    @GET
    @Path("/all")
    @Operation(
        summary = "Get all users for initial synchronization",
        description = "Returns all users with their context and full name. Used for initial author_view population in Note BC."
    )
    @APIResponse(
        responseCode = "200",
        description = "All users retrieved successfully",
        content = @Content(schema = @Schema(implementation = UserSyncResponse.class))
    )
    public Response getAllUsers() {
        List<UserSyncResponse> users = userRepository.findAllUsers()
            .stream()
            .map(user -> {
                Role role = roleRepository.findById(user.getRoleId())
                    .orElseThrow(() -> new IllegalStateException(
                        "Role not found for user " + user.getId().id()
                    ));

                return new UserSyncResponse(
                    user.getId().id(),
                    role.getApplicationRole(),
                    user.getAccountStatus(),
                    user.getFullName().toString()
                );
            })
            .collect(Collectors.toList());

        return Response.ok(users).build();
    }
}
