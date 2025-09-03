package org.access.exposition;

import org.access.application.dto.command.user.CreateUserCommand;
import org.access.application.dto.command.user.DeleteUserCommand;
import org.access.application.dto.command.role.CreateRoleCommand;
import org.access.application.dto.command.role.DeleteRoleCommand;
import org.access.application.dto.command.role.UpdateRoleCommand;
import org.access.application.dto.command.permission.CreatePermissionCommand;
import org.access.application.dto.command.permission.UpdatePermissionCommand;
import org.access.application.usecase.command.user.*;
import org.access.application.usecase.command.role.*;
import org.access.application.usecase.command.permission.*;
import org.access.application.usecase.query.*;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import java.util.logging.Logger;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource for access management operations.
 * 
 * This resource provides HTTP endpoints for managing users, roles, and permissions
 * in the access control system. It follows RESTful principles and uses JAX-RS
 * annotations for endpoint configuration.
 */
@Path("/api/v1/access")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Access Management", description = "Endpoints for managing users, roles, and permissions")
@ApplicationScoped
public class AccessRessource {

    @Inject
    Logger logger;

    // User command use cases
    @Inject
    CreateUser createUser;
    @Inject
    ChangeFullName changeFullName;
    @Inject
    ChangeMail changeMail;
    @Inject
    AssignNewRole assignNewRole;
    @Inject
    ExtendContract extendContract;
    @Inject
    LockUser lockUser;
    @Inject
    UnlockUser unlockUser;
    @Inject
    RecordActivity recordActivity;
    @Inject
    TerminateContract terminateContract;
    @Inject
    DeleteUser deleteUser;
    
    // Role command use cases
    @Inject
    CreateRole createRole;
    @Inject
    UpdateRole updateRole;
    @Inject
    DeleteRole deleteRole;
    @Inject
    GrantPermission grantPermission;
    @Inject
    RevokePermissionFromRole revokePermissionFromRole;
    
    // Permission command use cases
    @Inject
    CreatePermission createPermission;
    @Inject
    UpdatePermission updatePermission;
    @Inject
    DeletePermission deletePermission;
    
    // Query services for read operations
    @Inject
    UserQueryService userQueryService;
    @Inject
    RoleQueryService roleQueryService;
    @Inject
    PermissionQueryService permissionQueryService;

    // ========== USER ENDPOINTS ==========

    /**
     * Creates a new user in the system.
     * 
     * Validates the input data and creates a new user with the specified
     * personal information, role assignment, and contract details.
     * 
     * @param command validated user creation data including name, email, role, and contract end date
     * @return HTTP 201 Created on successful user creation
     */
    @POST
    @Path("/users")
    @Operation(
        summary = "Create a new user",
        description = "Creates a new user with personal information, role assignment, and contract details"
    )
    @APIResponse(
        responseCode = "201",
        description = "User created successfully"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid input data"
    )
    @RequestBody(
        description = "User creation data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Create User Example",
                value = """
                {
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john.doe@example.com",
                    "roleId": "550e8400-e29b-41d4-a716-446655440000",
                    "contractEndDate": "2025-12-31"
                }
                """
            )
        )
    )
    public Response createUser(@Valid CreateUserCommand command) {
        logger.info("Request received: POST /users - Creating user: " + command.firstName() + " " + command.lastName());
        try {
            createUser.execute(command);
            logger.info("Request successful: POST /users - User created successfully: " + command.firstName() + " " + command.lastName());
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            logger.severe("Request failed: POST /users - Failed to create user: " + command.firstName() + " " + command.lastName() + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves a specific user by their unique identifier.
     * 
     * Performs a lookup for the user and returns their complete information
     * if found, or a 404 status if the user does not exist.
     * 
     * @param userId the unique identifier of the user to retrieve
     * @return HTTP 200 OK with user data, or HTTP 404 Not Found if user doesn't exist
     */
    @GET
    @Path("/users/{userId}")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a specific user by their unique identifier"
    )
    @APIResponse(
        responseCode = "200",
        description = "User found successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    public Response getUserById(@PathParam("userId") UUID userId) {
        logger.info("Request received: GET /users/" + userId + " - Retrieving user by ID");
        try {
            var user = userQueryService.findById(userId);
            if (user != null) {
                logger.info("Request successful: GET /users/" + userId + " - User found");
                return Response.ok(user).build();
            }
            logger.info("Request completed: GET /users/" + userId + " - User not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            logger.severe("Request failed: GET /users/" + userId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all users in the system.
     * 
     * Returns a complete list of all registered users with their
     * basic information and current status.
     * 
     * @return HTTP 200 OK with a list of all users
     */
    @GET
    @Path("/users")
    @Operation(
        summary = "Get all users",
        description = "Returns a complete list of all registered users"
    )
    @APIResponse(
        responseCode = "200",
        description = "Users retrieved successfully"
    )
    public Response getAllUsers() {
        logger.info("Request received: GET /users - Retrieving all users");
        try {
            var users = userQueryService.findAllUsers();
            logger.info("Request successful: GET /users - Retrieved " + users.size() + " users");
            return Response.ok(users).build();
        } catch (Exception e) {
            logger.severe("Request failed: GET /users - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all users assigned to a specific role.
     * 
     * Returns a list of users who have been assigned the specified role,
     * allowing for role-based user management and reporting.
     * 
     * @param roleId the unique identifier of the role to filter by
     * @return HTTP 200 OK with a list of users having the specified role
     */
    @GET
    @Path("/users/role/{roleId}")
    @Operation(
        summary = "Get users by role",
        description = "Returns all users assigned to a specific role"
    )
    @APIResponse(
        responseCode = "200",
        description = "Users retrieved successfully"
    )
    public Response getUsersByRole(@PathParam("roleId") UUID roleId) {
        logger.info("Request received: GET /users/role/" + roleId + " - Retrieving users by role");
        try {
            var users = userQueryService.findUsersByRole(roleId);
            logger.info("Request successful: GET /users/role/" + roleId + " - Retrieved " + users.size() + " users");
            return Response.ok(users).build();
        } catch (Exception e) {
            logger.severe("Request failed: GET /users/role/" + roleId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Locks a user account, preventing access to the system.
     * 
     * This operation is typically used for security purposes or
     * administrative actions. A locked user cannot authenticate.
     * 
     * @param userId the unique identifier of the user to lock
     * @return HTTP 200 OK when the user is successfully locked
     */
    @PUT
    @Path("/users/{userId}/lock")
    @Operation(
        summary = "Lock user account",
        description = "Locks a user account to prevent system access"
    )
    @APIResponse(
        responseCode = "200",
        description = "User locked successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    public Response lockUser(@PathParam("userId") UUID userId) {
        logger.info("Request received: PUT /users/" + userId + "/lock - Locking user");
        try {
            lockUser.execute(userId);
            logger.info("Request successful: PUT /users/" + userId + "/lock - User locked successfully");
            return Response.ok().build();
        } catch (Exception e) {
            logger.severe("Request failed: PUT /users/" + userId + "/lock - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Unlocks a previously locked user account.
     * 
     * Restores access to a user account that was previously locked,
     * allowing the user to authenticate and use the system again.
     * 
     * @param userId the unique identifier of the user to unlock
     * @return HTTP 200 OK when the user is successfully unlocked
     */
    @PUT
    @Path("/users/{userId}/unlock")
    @Operation(
        summary = "Unlock user account",
        description = "Unlocks a previously locked user account to restore system access"
    )
    @APIResponse(
        responseCode = "200",
        description = "User unlocked successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    public Response unlockUser(@PathParam("userId") UUID userId) {
        logger.info("Request received: PUT /users/" + userId + "/unlock - Unlocking user");
        try {
            unlockUser.execute(userId);
            logger.info("Request successful: PUT /users/" + userId + "/unlock - User unlocked successfully");
            return Response.ok().build();
        } catch (Exception e) {
            logger.severe("Request failed: PUT /users/" + userId + "/unlock - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Permanently deletes a user from the system.
     * 
     * Performs a hard delete operation that completely removes the user
     * and all their data from the database. The user must be locked before
     * deletion as a safety measure to prevent accidental removal.
     * 
     * @param userId the unique identifier of the user to delete
     * @return HTTP 204 No Content when the user is successfully deleted
     * @throws IllegalStateException if the user is not locked before deletion
     */
    @DELETE
    @Path("/users/{userId}")
    @Operation(
        summary = "Delete user",
        description = "Permanently deletes a user from the system. User must be locked before deletion."
    )
    @APIResponse(
        responseCode = "204",
        description = "User deleted successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "User must be locked before deletion"
    )
    public Response deleteUser(@PathParam("userId") UUID userId) {
        logger.info("Request received: DELETE /users/" + userId + " - Deleting user");
        try {
            deleteUser.execute(new DeleteUserCommand(userId));
            logger.info("Request successful: DELETE /users/" + userId + " - User deleted successfully");
            return Response.noContent().build();
        } catch (Exception e) {
            logger.severe("Request failed: DELETE /users/" + userId + " - " + e.getMessage());
            throw e;
        }
    }

    // ========== ROLE ENDPOINTS ==========

    /**
     * Creates a new role in the system.
     * 
     * Establishes a new role with a unique code and descriptive name
     * that can later be assigned to users and granted permissions.
     * 
     * @param command validated role creation data including code and name
     * @return HTTP 201 Created on successful role creation
     */
    @POST
    @Path("/roles")
    @Operation(
        summary = "Create a new role",
        description = "Creates a new role with a unique code and descriptive name"
    )
    @APIResponse(
        responseCode = "201",
        description = "Role created successfully"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid input data"
    )
    @RequestBody(
        description = "Role creation data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Create Role Example",
                value = """
                {
                    "code": "MANAGER",
                    "name": "Project Manager"
                }
                """
            )
        )
    )
    public Response createRole(@Valid CreateRoleCommand command) {
        logger.info("Request received: POST /roles - Creating role: " + command.code());
        try {
            createRole.execute(command);
            logger.info("Request successful: POST /roles - Role created successfully: " + command.code());
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            logger.severe("Request failed: POST /roles - Failed to create role: " + command.code() + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves a specific role by its unique identifier.
     * 
     * Returns complete role information including associated permissions
     * if the role exists, or a 404 status if not found.
     * 
     * @param roleId the unique identifier of the role to retrieve
     * @return HTTP 200 OK with role data, or HTTP 404 Not Found if role doesn't exist
     */
    @GET
    @Path("/roles/{roleId}")
    @Operation(
        summary = "Get role by ID",
        description = "Retrieves a specific role by its unique identifier"
    )
    @APIResponse(
        responseCode = "200",
        description = "Role found successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Role not found"
    )
    public Response getRoleById(@PathParam("roleId") UUID roleId) {
        logger.info("Request received: GET /roles/" + roleId + " - Retrieving role by ID");
        try {
            var role = roleQueryService.findById(roleId);
            if (role != null) {
                logger.info("Request successful: GET /roles/" + roleId + " - Role found");
                return Response.ok(role).build();
            }
            logger.info("Request completed: GET /roles/" + roleId + " - Role not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            logger.severe("Request failed: GET /roles/" + roleId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all roles in the system.
     * 
     * Returns a complete list of all defined roles with their
     * basic information and associated permissions.
     * 
     * @return HTTP 200 OK with a list of all roles
     */
    @GET
    @Path("/roles")
    @Operation(
        summary = "Get all roles",
        description = "Returns a complete list of all defined roles"
    )
    @APIResponse(
        responseCode = "200",
        description = "Roles retrieved successfully"
    )
    public Response getAllRoles() {
        logger.info("Request received: GET /roles - Retrieving all roles");
        try {
            var roles = roleQueryService.findAllRoles();
            logger.info("Request successful: GET /roles - Retrieved " + roles.size() + " roles");
            return Response.ok(roles).build();
        } catch (Exception e) {
            logger.severe("Request failed: GET /roles - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Updates an existing role's information.
     * 
     * Modifies the role's properties such as name or description
     * while preserving its existing permission assignments.
     * 
     * @param roleId the unique identifier of the role to update
     * @param command validated role update data
     * @return HTTP 200 OK when the role is successfully updated
     */
    @PUT
    @Path("/roles/{roleId}")
    @Operation(
        summary = "Update role",
        description = "Updates an existing role's information"
    )
    @APIResponse(
        responseCode = "200",
        description = "Role updated successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Role not found"
    )
    @RequestBody(
        description = "Role update data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Update Role Example",
                value = """
                {
                    "roleId": "550e8400-e29b-41d4-a716-446655440000",
                    "code": "SENIOR_MANAGER",
                    "name": "Senior Project Manager"
                }
                """
            )
        )
    )
    public Response updateRole(@PathParam("roleId") UUID roleId, @Valid UpdateRoleCommand command) {
        logger.info("Request received: PUT /roles/" + roleId + " - Updating role to code: " + command.code());
        try {
            updateRole.execute(command);
            logger.info("Request successful: PUT /roles/" + roleId + " - Role updated successfully to code: " + command.code());
            return Response.ok().build();
        } catch (Exception e) {
            logger.severe("Request failed: PUT /roles/" + roleId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes a role from the system.
     * 
     * Permanently removes a role and all its permission associations.
     * This operation should be used carefully as it affects all users
     * currently assigned to this role.
     * 
     * @param roleId the unique identifier of the role to delete
     * @return HTTP 204 No Content when the role is successfully deleted
     */
    @DELETE
    @Path("/roles/{roleId}")
    @Operation(
        summary = "Delete role",
        description = "Permanently deletes a role from the system"
    )
    @APIResponse(
        responseCode = "204",
        description = "Role deleted successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Role not found"
    )
    public Response deleteRole(@PathParam("roleId") UUID roleId) {
        logger.info("Request received: DELETE /roles/" + roleId + " - Deleting role");
        try {
            deleteRole.execute(new DeleteRoleCommand(roleId));
            logger.info("Request successful: DELETE /roles/" + roleId + " - Role deleted successfully");
            return Response.noContent().build();
        } catch (Exception e) {
            logger.severe("Request failed: DELETE /roles/" + roleId + " - " + e.getMessage());
            throw e;
        }
    }

    // ========== PERMISSION ENDPOINTS ==========

    /**
     * Creates a new permission in the system.
     * 
     * Defines a new permission that can be granted to roles to
     * control access to specific system functionality or resources.
     * 
     * @param command validated permission creation data
     * @return HTTP 201 Created on successful permission creation
     */
    @POST
    @Path("/permissions")
    @Operation(
        summary = "Create a new permission",
        description = "Creates a new permission that can be granted to roles"
    )
    @APIResponse(
        responseCode = "201",
        description = "Permission created successfully"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid input data"
    )
    @RequestBody(
        description = "Permission creation data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Create Permission Example",
                value = """
                {
                    "code": "VIEW_REPORTS",
                    "name": "View Reports",
                    "description": "Allows viewing of system reports"
                }
                """
            )
        )
    )
    public Response createPermission(@Valid CreatePermissionCommand command) {
        logger.info("Request received: POST /permissions - Creating permission: " + command.code());
        try {
            createPermission.execute(command);
            logger.info("Request successful: POST /permissions - Permission created successfully: " + command.code());
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            logger.severe("Request failed: POST /permissions - Failed to create permission: " + command.code() + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves a specific permission by its unique identifier.
     * 
     * Returns complete permission information if found,
     * or a 404 status if the permission does not exist.
     * 
     * @param permissionId the unique identifier of the permission to retrieve
     * @return HTTP 200 OK with permission data, or HTTP 404 Not Found if permission doesn't exist
     */
    @GET
    @Path("/permissions/{permissionId}")
    @Operation(
        summary = "Get permission by ID",
        description = "Retrieves a specific permission by its unique identifier"
    )
    @APIResponse(
        responseCode = "200",
        description = "Permission found successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Permission not found"
    )
    public Response getPermissionById(@PathParam("permissionId") UUID permissionId) {
        logger.info("Request received: GET /permissions/" + permissionId + " - Retrieving permission by ID");
        try {
            var permission = permissionQueryService.findById(permissionId);
            if (permission != null) {
                logger.info("Request successful: GET /permissions/" + permissionId + " - Permission found");
                return Response.ok(permission).build();
            }
            logger.info("Request completed: GET /permissions/" + permissionId + " - Permission not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            logger.severe("Request failed: GET /permissions/" + permissionId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Retrieves all permissions in the system.
     * 
     * Returns a complete list of all defined permissions
     * that can be granted to roles for access control.
     * 
     * @return HTTP 200 OK with a list of all permissions
     */
    @GET
    @Path("/permissions")
    @Operation(
        summary = "Get all permissions",
        description = "Returns a complete list of all defined permissions"
    )
    @APIResponse(
        responseCode = "200",
        description = "Permissions retrieved successfully"
    )
    public Response getAllPermissions() {
        logger.info("Request received: GET /permissions - Retrieving all permissions");
        try {
            var permissions = permissionQueryService.findAllPermissions();
            logger.info("Request successful: GET /permissions - Retrieved " + permissions.size() + " permissions");
            return Response.ok(permissions).build();
        } catch (Exception e) {
            logger.severe("Request failed: GET /permissions - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Updates an existing permission's information.
     * 
     * Modifies the permission's properties such as code, name, or description
     * while preserving its unique identifier and existing role associations.
     * 
     * @param permissionId the unique identifier of the permission to update
     * @param command validated permission update data
     * @return HTTP 200 OK when the permission is successfully updated
     */
    @PUT
    @Path("/permissions/{permissionId}")
    @Operation(
        summary = "Update permission",
        description = "Updates an existing permission's information"
    )
    @APIResponse(
        responseCode = "200",
        description = "Permission updated successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Permission not found"
    )
    @RequestBody(
        description = "Permission update data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Update Permission Example",
                value = """
                {
                    "permissionId": "550e8400-e29b-41d4-a716-446655440000",
                    "code": "VIEW_DETAILED_REPORTS",
                    "name": "View Detailed Reports",
                    "description": "Allows viewing of detailed system reports and analytics"
                }
                """
            )
        )
    )
    public Response updatePermission(@PathParam("permissionId") UUID permissionId, @Valid UpdatePermissionCommand command) {
        logger.info("Request received: PUT /permissions/" + permissionId + " - Updating permission to code: " + command.code());
        try {
            updatePermission.execute(command);
            logger.info("Request successful: PUT /permissions/" + permissionId + " - Permission updated successfully to code: " + command.code());
            return Response.ok().build();
        } catch (Exception e) {
            logger.severe("Request failed: PUT /permissions/" + permissionId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Deletes a permission from the system.
     * 
     * Permanently removes a permission and all its associations with roles.
     * This operation should be used carefully as it affects access control
     * for all roles that currently have this permission.
     * 
     * @param permissionId the unique identifier of the permission to delete
     * @return HTTP 204 No Content when the permission is successfully deleted
     */
    @DELETE
    @Path("/permissions/{permissionId}")
    @Operation(
        summary = "Delete permission",
        description = "Permanently deletes a permission from the system"
    )
    @APIResponse(
        responseCode = "204",
        description = "Permission deleted successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Permission not found"
    )
    public Response deletePermission(@PathParam("permissionId") UUID permissionId) {
        logger.info("Request received: DELETE /permissions/" + permissionId + " - Deleting permission");
        try {
            deletePermission.execute(permissionId);
            logger.info("Request successful: DELETE /permissions/" + permissionId + " - Permission deleted successfully");
            return Response.noContent().build();
        } catch (Exception e) {
            logger.severe("Request failed: DELETE /permissions/" + permissionId + " - " + e.getMessage());
            throw e;
        }
    }
}
