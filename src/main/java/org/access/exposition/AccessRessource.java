package org.access.exposition;

import org.access.application.dto.command.user.AssignNewRoleCommand;
import org.access.application.dto.command.user.ChangeFullNameCommand;
import org.access.application.dto.command.user.ChangeMailCommand;
import org.access.application.dto.command.user.ChangePasswordCommand;
import org.access.application.dto.command.user.CreateUserCommand;
import org.access.application.dto.command.user.DeleteUserCommand;
import org.access.application.dto.command.user.ExtendContractCommand;
import org.access.application.dto.command.user.LockUserCommand;
import org.access.application.dto.command.user.TerminateContractCommand;
import org.access.application.dto.command.user.UnlockUserCommand;
import org.access.application.dto.command.role.CreateRoleCommand;
import org.access.application.dto.command.role.DeleteRoleCommand;
import org.access.application.dto.command.role.UpdateRoleCommand;
import org.access.application.dto.command.permission.CreatePermissionCommand;
import org.access.application.dto.command.permission.DeletePermissionCommand;
import org.access.application.dto.command.permission.UpdatePermissionCommand;
import org.access.application.usecase.command.user.*;
import org.access.application.usecase.command.role.*;
import org.access.application.usecase.command.permission.*;
import org.access.application.usecase.query.UserQueryService;
import org.access.application.usecase.query.RoleQueryService;
import org.access.application.usecase.query.PermissionQueryService;
import org.access.application.dto.response.UserProfileResponse;
import org.access.domain.model.PermissionId;
import org.access.domain.model.RoleId;

import org.eclipse.microprofile.jwt.JsonWebToken;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.UUID;
import io.quarkus.logging.Log;

import org.access.application.service.SecurityContextService;
import org.access.exposition.security.RequiresPermission;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

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
@SecurityRequirement(name = "JWT")
@ApplicationScoped
public class AccessRessource {

    // User command use cases
    @Inject
    CreateUser createUser;
    @Inject
    ChangeFullName changeFullName;
    @Inject
    ChangeMail changeMail;
    @Inject
    ChangePassword changePassword;
    @Inject
    AssignNewRole assignNewRole;
    @Inject
    ExtendContract extendContract;
    @Inject
    LockUser lockUser;
    @Inject
    UnlockUser unlockUser;
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

    // JWT for extracting current user info
    @Inject
    JsonWebToken jwt;

    // Security context service for permission-based authorization
    @Inject
    SecurityContextService securityContextService;

    // ========== CURRENT USER ENDPOINT ==========

    /**
     * Retrieves the current authenticated user's profile.
     *
     * Returns user identity, role information, and all permission codes
     * associated with their role. This endpoint is used by the frontend
     * to determine UI visibility and feature access.
     *
     * This endpoint is optimized to use cached security context data
     * and JWT claims, avoiding database queries entirely.
     *
     * @return HTTP 200 OK with the user profile including permissions
     */
    @GET
    @Path("/me")
    @Operation(
        summary = "Get current user profile",
        description = "Returns the authenticated user's profile with role and permissions. **Required permission: None (authenticated user only)**"
    )
    @APIResponse(
        responseCode = "200",
        description = "User profile retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "User Profile Response",
                value = """
                {
                    "id": "550e8400-e29b-41d4-a716-446655440000",
                    "email": "john.doe@example.com",
                    "fullName": "John Doe",
                    "role": {
                        "code": "MANAGER",
                        "name": "Project Manager"
                    },
                    "permissions": [
                        "PERMISSION_VIEW",
                        "ROLE_VIEW",
                        "USER_VIEW_ALL"
                    ]
                }
                """
            )
        )
    )
    @APIResponse(
        responseCode = "401",
        description = "Not authenticated"
    )
    public Response getCurrentUserProfile() {
        Log.info("Request received: GET /me - Retrieving current user profile");
        try {
            // Get cached security context (0 DB queries if already cached)
            var securityContext = securityContextService.getCurrentContext();

            // Build profile from JWT claims + cached permissions
            var profile = new UserProfileResponse(
                securityContext.actorId().id(),
                jwt.getName(), // email from upn claim
                jwt.getClaim("fullName"),
                new UserProfileResponse.RoleInfo(
                    securityContext.roleCode().value(),
                    jwt.getClaim("roleName")
                ),
                securityContext.permissions().stream()
                    .map(p -> p.value())
                    .sorted()
                    .toList()
            );

            Log.info("Request successful: GET /me - Profile retrieved for user: " + jwt.getName());
            return Response.ok(profile).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /me - " + e.getMessage());
            throw e;
        }
    }

    // ========== USER ENDPOINTS ==========

    // ===== COMMAND ENDPOINTS =====

    /**
     * Creates a new user in the system.
     * 
     * Validates the input data and creates a new user with the specified
     * personal information, role assignment, and contract details.
     * 
     * @param cmd validated user creation data including name, email, role, and contract end date
     * @return HTTP 201 Created on successful user creation
     */
    @POST
    @Path("/users")
    @RequiresPermission("USER_CREATE")
    @Operation(
        summary = "Create a new user",
        description = "Creates a new user with personal information, role assignment, and contract details. **Required permission: USER_CREATE**"
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
    public Response createUser(@Valid CreateUserCommand cmd) {
        Log.info("Request received: POST /users - Creating user: " + cmd.firstName() + " " + cmd.lastName());
        try {
            createUser.execute(cmd, securityContextService.getCurrentContext());
            Log.info("Request successful: POST /users - User created successfully: " + cmd.firstName() + " " + cmd.lastName());
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            Log.error("Request failed: POST /users - Failed to create user: " + cmd.firstName() + " " + cmd.lastName() + " - " + e.getMessage());
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
     * @param cmd the unique identifier of the user to lock
     * @return HTTP 200 OK when the user is successfully locked
     */
    @PUT
    @Path("/users/{userId}/lock")
    @RequiresPermission("USER_LOCK")
    @Operation(
        summary = "Lock user account",
        description = "Locks a user account to prevent system access. **Required permission: USER_LOCK**"
    )
    @APIResponse(
        responseCode = "200",
        description = "User locked successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    @RequestBody(
        description = "User lock data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Lock User Example",
                value = """
                {
                    "userId": "550e8400-e29b-41d4-a716-446655440000"
                }
                """
            )
        )
    )
    public Response lockUser(@PathParam("userId") UUID userId, @Valid LockUserCommand cmd) {
        Log.info("Request received: PUT /users/" + userId + "/lock - Locking user");
        try {
            lockUser.execute(cmd, securityContextService.getCurrentContext());
            Log.info("Request successful: PUT /users/" + userId + "/lock - User locked successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /users/" + userId + "/lock - " + e.getMessage());
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
     * @param cmd the unique identifier of the user to unlock
     * @return HTTP 200 OK when the user is successfully unlocked
     */
    @PUT
    @Path("/users/{userId}/unlock")
    @RequiresPermission("USER_LOCK")
    @Operation(
        summary = "Unlock user account",
        description = "Unlocks a previously locked user account to restore system access. **Required permission: USER_LOCK**"
    )
    @APIResponse(
        responseCode = "200",
        description = "User unlocked successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    @RequestBody(
        description = "User unlock data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Unlock User Example",
                value = """
                {
                    "userId": "550e8400-e29b-41d4-a716-446655440000"
                }
                """
            )
        )
    )
    public Response unlockUser(@PathParam("userId") UUID userId, @Valid UnlockUserCommand cmd) {
        Log.info("Request received: PUT /users/" + userId + "/unlock - Unlocking user");
        try {
            unlockUser.execute(cmd, securityContextService.getCurrentContext());
            Log.info("Request successful: PUT /users/" + userId + "/unlock - User unlocked successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /users/" + userId + "/unlock - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Assigns a new role to a user.
     *
     * Updates the user's role assignment, which affects their permissions
     * and access levels throughout the system.
     *
     * @param userId the unique identifier of the user
     * @param cmd the command containing the new role ID
     * @return HTTP 200 OK when the role is successfully assigned
     */
    @PUT
    @Path("/users/{userId}/role")
    @RequiresPermission({"ASSIGN_CRITICAL_ROLES", "ASSIGN_STANDARD_ROLES", "ASSIGN_BASIC_ROLES"})
    @Operation(
        summary = "Assign new role to user",
        description = "Updates the user's role assignment. **Required permission: ASSIGN_CRITICAL_ROLES OR ASSIGN_STANDARD_ROLES OR ASSIGN_BASIC_ROLES**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Role assigned successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    @RequestBody(
        description = "Role assignment data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Assign Role Example",
                value = """
                {
                    "userId": "550e8400-e29b-41d4-a716-446655440000",
                    "roleId": "660e8400-e29b-41d4-a716-446655440001"
                }
                """
            )
        )
    )
    public Response assignNewRole(@PathParam("userId") UUID userId, @Valid AssignNewRoleCommand cmd) {
        Log.info("Request received: PUT /users/" + userId + "/role - Assigning new role to user");
        try {
            assignNewRole.execute(cmd, securityContextService.getCurrentContext());
            Log.info("Request successful: PUT /users/" + userId + "/role - Role assigned successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /users/" + userId + "/role - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Changes a user's full name.
     *
     * Updates both the first name and last name of the user.
     *
     * @param userId the unique identifier of the user
     * @param cmd the command containing the new first and last names
     * @return HTTP 200 OK when the name is successfully changed
     */
    @PUT
    @Path("/users/{userId}/name")
    @RequiresPermission("USER_UPDATE")
    @Operation(
        summary = "Change user's full name",
        description = "Updates the user's first name and last name. **Required permission: USER_UPDATE**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Name changed successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    @RequestBody(
        description = "Name change data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Change Full Name Example",
                value = """
                {
                    "userId": "550e8400-e29b-41d4-a716-446655440000",
                    "firstName": "Jane",
                    "lastName": "Smith"
                }
                """
            )
        )
    )
    public Response changeFullName(@PathParam("userId") UUID userId, @Valid ChangeFullNameCommand cmd) {
        Log.info("Request received: PUT /users/" + userId + "/name - Changing user full name");
        try {
            changeFullName.execute(cmd);
            Log.info("Request successful: PUT /users/" + userId + "/name - Full name changed successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /users/" + userId + "/name - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Changes a user's email address.
     *
     * Updates the user's email address used for authentication and communication.
     *
     * @param userId the unique identifier of the user
     * @param cmd the command containing the new email address
     * @return HTTP 200 OK when the email is successfully changed
     */
    @PUT
    @Path("/users/{userId}/email")
    @RequiresPermission("USER_UPDATE")
    @Operation(
        summary = "Change user's email",
        description = "Updates the user's email address. **Required permission: USER_UPDATE**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Email changed successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    @RequestBody(
        description = "Email change data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Change Email Example",
                value = """
                {
                    "userId": "550e8400-e29b-41d4-a716-446655440000",
                    "email": "jane.smith@newcompany.com"
                }
                """
            )
        )
    )
    public Response changeMail(@PathParam("userId") UUID userId, @Valid ChangeMailCommand cmd) {
        Log.info("Request received: PUT /users/" + userId + "/email - Changing user email");
        try {
            changeMail.execute(cmd);
            Log.info("Request successful: PUT /users/" + userId + "/email - Email changed successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /users/" + userId + "/email - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Changes a user's password.
     *
     * Updates the user's password with a new secure password that meets all
     * security requirements (minimum 32 characters, at least one digit, at least one special character).
     * The password is hashed using BCrypt before being stored.
     *
     * @param userId the unique identifier of the user
     * @param cmd the command containing the new password
     * @return HTTP 200 OK when the password is successfully changed
     */
    @PUT
    @Path("/users/{userId}/password")
    @RequiresPermission("USER_UPDATE")
    @Operation(
        summary = "Change user's password",
        description = "Updates the user's password with a new secure password. **Required permission: USER_UPDATE**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Password changed successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid password format or validation failed"
    )
    @RequestBody(
        description = "Password change data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Change Password Example",
                value = """
                {
                    "userId": "550e8400-e29b-41d4-a716-446655440000",
                    "newPassword": "MySecurePassword123!@#$%2024NewPass"
                }
                """
            )
        )
    )
    public Response changePassword(@PathParam("userId") UUID userId, @Valid ChangePasswordCommand cmd) {
        Log.info("Request received: PUT /users/" + userId + "/password - Changing user password");
        try {
            changePassword.execute(cmd);
            Log.info("Request successful: PUT /users/" + userId + "/password - Password changed successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /users/" + userId + "/password - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Extends a user's contract end date.
     *
     * Updates the contract end date to a future date, extending the user's
     * access to the system.
     *
     * @param userId the unique identifier of the user
     * @param cmd the command containing the new contract end date
     * @return HTTP 200 OK when the contract is successfully extended
     */
    @PUT
    @Path("/users/{userId}/contract")
    @RequiresPermission("USER_UPDATE")
    @Operation(
        summary = "Extend user's contract",
        description = "Updates the user's contract end date to a future date. **Required permission: USER_UPDATE**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Contract extended successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    @APIResponse(
        responseCode = "400",
        description = "New end date must be after the current one"
    )
    @RequestBody(
        description = "Contract extension data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Extend Contract Example",
                value = """
                {
                    "userId": "550e8400-e29b-41d4-a716-446655440000",
                    "newEndDate": "2026-12-31"
                }
                """
            )
        )
    )
    public Response extendContract(@PathParam("userId") UUID userId, @Valid ExtendContractCommand cmd) {
        Log.info("Request received: PUT /users/" + userId + "/contract - Extending user contract");
        try {
            extendContract.execute(cmd, securityContextService.getCurrentContext());
            Log.info("Request successful: PUT /users/" + userId + "/contract - Contract extended successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /users/" + userId + "/contract - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Terminates a user's contract immediately.
     *
     * Sets the contract end date to today and locks the user's account.
     * Used for resignations or dismissals.
     *
     * @param userId the unique identifier of the user
     * @param cmd the command containing the user ID
     * @return HTTP 200 OK when the contract is successfully terminated
     */
    @PUT
    @Path("/users/{userId}/contract/terminate")
    @RequiresPermission("USER_UPDATE")
    @Operation(
        summary = "Terminate user's contract",
        description = "Immediately terminates the user's contract and locks their account. **Required permission: USER_UPDATE**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Contract terminated successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    @RequestBody(
        description = "Contract termination data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Terminate Contract Example",
                value = """
                {
                    "userId": "550e8400-e29b-41d4-a716-446655440000"
                }
                """
            )
        )
    )
    public Response terminateContract(@PathParam("userId") UUID userId, @Valid TerminateContractCommand cmd) {
        Log.info("Request received: PUT /users/" + userId + "/contract/terminate - Terminating user contract");
        try {
            terminateContract.execute(cmd, securityContextService.getCurrentContext());
            Log.info("Request successful: PUT /users/" + userId + "/contract/terminate - Contract terminated successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /users/" + userId + "/contract/terminate - " + e.getMessage());
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
     * @param cmd the unique identifier of the user to delete
     * @return HTTP 204 No Content when the user is successfully deleted
     * @throws IllegalStateException if the user is not locked before deletion
     */
    @DELETE
    @Path("/users/{userId}")
    @RequiresPermission("USER_DELETE")
    @Operation(
        summary = "Delete user",
        description = "Permanently deletes a user from the system. User must be locked before deletion. **Required permission: USER_DELETE**"
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
    @RequestBody(
        description = "User deletion data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Delete User Example",
                value = """
                {
                    "userId": "550e8400-e29b-41d4-a716-446655440000"
                }
                """
            )
        )
    )
    public Response deleteUser(@PathParam("userId") UUID userId, @Valid DeleteUserCommand cmd) {
        Log.info("Request received: DELETE /users/" + userId + " - Deleting user");
        try {
            deleteUser.execute(cmd, securityContextService.getCurrentContext());
            Log.info("Request successful: DELETE /users/" + userId + " - User deleted successfully");
            return Response.noContent().build();
        } catch (Exception e) {
            Log.error("Request failed: DELETE /users/" + userId + " - " + e.getMessage());
            throw e;
        }
    }

    // ===== QUERY ENDPOINTS =====

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
    @RequiresPermission("USER_VIEW_ALL")
    @Operation(
        summary = "Get user by ID",
        description = "Retrieves a specific user by their unique identifier. **Required permission: USER_VIEW_ALL**"
    )
    @APIResponse(
        responseCode = "200",
        description = "User found successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "User Response",
                value = """
                {
                    "userId": "550e8400-e29b-41d4-a716-446655440000",
                    "firstName": "John",
                    "lastName": "Doe",
                    "email": "john.doe@example.com",
                    "contractEndDate": "2026-12-31",
                    "isLocked": false,
                    "lastActivityDate": "2024-01-15T14:30:00Z",
                    "auditInfo": {
                        "createdAt": "2023-06-01T09:00:00Z",
                        "updatedAt": "2024-01-15T14:30:00Z"
                    },
                    "roleId": "660e8400-e29b-41d4-a716-446655440001"
                }
                """
            )
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "User not found"
    )
    public Response getUserById(@PathParam("userId") UUID userId) {
        Log.info("Request received: GET /users/" + userId + " - Retrieving user by ID");
        try {
            var user = userQueryService.findById(userId);
            if (user != null) {
                Log.info("Request successful: GET /users/" + userId + " - User found");
                return Response.ok(user).build();
            }
            Log.info("Request completed: GET /users/" + userId + " - User not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /users/" + userId + " - " + e.getMessage());
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
    @RequiresPermission("USER_VIEW_ALL")
    @Operation(
        summary = "Get all users",
        description = "Returns a complete list of all registered users. **Required permission: USER_VIEW_ALL**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Users retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Users List Response",
                value = """
                [
                    {
                        "userId": "550e8400-e29b-41d4-a716-446655440000",
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john.doe@example.com",
                        "contractEndDate": "2026-12-31",
                        "isLocked": false,
                        "lastActivityDate": "2024-01-15T14:30:00Z",
                        "auditInfo": {
                            "createdAt": "2023-06-01T09:00:00Z",
                            "updatedAt": "2024-01-15T14:30:00Z"
                        },
                        "roleId": "660e8400-e29b-41d4-a716-446655440001"
                    },
                    {
                        "userId": "550e8400-e29b-41d4-a716-446655440001",
                        "firstName": "Jane",
                        "lastName": "Smith",
                        "email": "jane.smith@example.com",
                        "contractEndDate": "2025-06-30",
                        "isLocked": false,
                        "lastActivityDate": "2024-01-14T16:45:00Z",
                        "auditInfo": {
                            "createdAt": "2023-07-15T10:00:00Z",
                            "updatedAt": "2024-01-14T16:45:00Z"
                        },
                        "roleId": "770e8400-e29b-41d4-a716-446655440002"
                    }
                ]
                """
            )
        )
    )
    public Response getAllUsers() {
        Log.info("Request received: GET /users - Retrieving all users");
        try {
            var users = userQueryService.findAllUsers();
            Log.info("Request successful: GET /users - Retrieved " + users.size() + " users");
            return Response.ok(users).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /users - " + e.getMessage());
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
    @RequiresPermission("USER_VIEW_ALL")
    @Operation(
        summary = "Get users by role",
        description = "Returns all users assigned to a specific role. **Required permission: USER_VIEW_ALL**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Users retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Users By Role Response",
                value = """
                [
                    {
                        "userId": "550e8400-e29b-41d4-a716-446655440000",
                        "firstName": "John",
                        "lastName": "Doe",
                        "email": "john.doe@example.com",
                        "contractEndDate": "2026-12-31",
                        "isLocked": false,
                        "lastActivityDate": "2024-01-15T14:30:00Z",
                        "auditInfo": {
                            "createdAt": "2023-06-01T09:00:00Z",
                            "updatedAt": "2024-01-15T14:30:00Z"
                        },
                        "roleId": "660e8400-e29b-41d4-a716-446655440001"
                    }
                ]
                """
            )
        )
    )
    public Response getUsersByRole(@PathParam("roleId") UUID roleId) {
        Log.info("Request received: GET /users/role/" + roleId + " - Retrieving users by role");
        try {
            var users = userQueryService.findUsersByRole(roleId);
            Log.info("Request successful: GET /users/role/" + roleId + " - Retrieved " + users.size() + " users");
            return Response.ok(users).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /users/role/" + roleId + " - " + e.getMessage());
            throw e;
        }
    }

    // ========== ROLE ENDPOINTS ==========

    // ===== COMMAND ENDPOINTS =====

    /**
     * Creates a new role in the system.
     * 
     * Establishes a new role with a unique code and descriptive name
     * that can later be assigned to users and granted permissions.
     * 
     * @param cmd validated role creation data including code and name
     * @return HTTP 201 Created on successful role creation
     */
    @POST
    @Path("/roles")
    @RequiresPermission("MANAGE_ROLES")
    @Operation(
        summary = "Create a new role",
        description = "Creates a new role with a unique code and descriptive name. **Required permission: MANAGE_ROLES**"
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
    public Response createRole(@Valid CreateRoleCommand cmd) {
        Log.info("Request received: POST /roles - Creating role: " + cmd.code());
        try {
            createRole.execute(cmd, securityContextService.getCurrentContext());
            Log.info("Request successful: POST /roles - Role created successfully: " + cmd.code());
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            Log.error("Request failed: POST /roles - Failed to create role: " + cmd.code() + " - " + e.getMessage());
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
     * @param cmd validated role update data
     * @return HTTP 200 OK when the role is successfully updated
     */
    @PUT
    @Path("/roles/{roleId}")
    @RequiresPermission("MANAGE_ROLES")
    @Operation(
        summary = "Update role",
        description = "Updates an existing role's information. **Required permission: MANAGE_ROLES**"
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
    public Response updateRole(@PathParam("roleId") UUID roleId, @Valid UpdateRoleCommand cmd) {
        Log.info("Request received: PUT /roles/" + roleId + " - Updating role to code: " + cmd.code());
        try {
            updateRole.execute(cmd, securityContextService.getCurrentContext());
            Log.info("Request successful: PUT /roles/" + roleId + " - Role updated successfully to code: " + cmd.code());
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /roles/" + roleId + " - " + e.getMessage());
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
     * @param cmd the unique identifier of the role to delete
     * @return HTTP 204 No Content when the role is successfully deleted
     */
    @DELETE
    @Path("/roles/{roleId}")
    @RequiresPermission("MANAGE_ROLES")
    @Operation(
        summary = "Delete role",
        description = "Permanently deletes a role from the system. **Required permission: MANAGE_ROLES**"
    )
    @APIResponse(
        responseCode = "204",
        description = "Role deleted successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Role not found"
    )
    @RequestBody(
        description = "Role deletion data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Delete Role Example",
                value = """
                {
                    "roleId": "550e8400-e29b-41d4-a716-446655440000"
                }
                """
            )
        )
    )
    public Response deleteRole(@PathParam("roleId") UUID roleId, @Valid DeleteRoleCommand cmd) {
        Log.info("Request received: DELETE /roles/" + roleId + " - Deleting role");
        try {
            deleteRole.execute(cmd);
            Log.info("Request successful: DELETE /roles/" + roleId + " - Role deleted successfully");
            return Response.noContent().build();
        } catch (Exception e) {
            Log.error("Request failed: DELETE /roles/" + roleId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Grants a permission to a role.
     *
     * Associates a permission with a role, allowing users with that role
     * to perform the associated action.
     *
     * @param roleId the unique identifier of the role
     * @param permissionId the unique identifier of the permission to grant
     * @return HTTP 200 OK when the permission is successfully granted
     */
    @PUT
    @Path("/roles/{roleId}/permissions/{permissionId}")
    @RequiresPermission("MANAGE_ROLES")
    @Operation(
        summary = "Grant permission to role",
        description = "Associates a permission with a role, granting access to the specified action. **Required permission: MANAGE_ROLES**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Permission granted successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Role or permission not found"
    )
    public Response grantPermission(@PathParam("roleId") UUID roleId, @PathParam("permissionId") UUID permissionId) {
        Log.info("Request received: PUT /roles/" + roleId + "/permissions/" + permissionId + " - Granting permission to role");
        try {
            grantPermission.execute(new RoleId(roleId), new PermissionId(permissionId), securityContextService.getCurrentContext());
            Log.info("Request successful: PUT /roles/" + roleId + "/permissions/" + permissionId + " - Permission granted successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /roles/" + roleId + "/permissions/" + permissionId + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Revokes a permission from a role.
     *
     * Removes the association between a permission and a role,
     * preventing users with that role from performing the associated action.
     *
     * @param roleId the unique identifier of the role
     * @param permissionId the unique identifier of the permission to revoke
     * @return HTTP 200 OK when the permission is successfully revoked
     */
    @DELETE
    @Path("/roles/{roleId}/permissions/{permissionId}")
    @RequiresPermission("MANAGE_ROLES")
    @Operation(
        summary = "Revoke permission from role",
        description = "Removes a permission from a role, revoking access to the specified action. **Required permission: MANAGE_ROLES**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Permission revoked successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Role or permission not found"
    )
    public Response revokePermissionFromRole(@PathParam("roleId") UUID roleId, @PathParam("permissionId") UUID permissionId) {
        Log.info("Request received: DELETE /roles/" + roleId + "/permissions/" + permissionId + " - Revoking permission from role");
        try {
            revokePermissionFromRole.execute(new RoleId(roleId), new PermissionId(permissionId));
            Log.info("Request successful: DELETE /roles/" + roleId + "/permissions/" + permissionId + " - Permission revoked successfully");
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: DELETE /roles/" + roleId + "/permissions/" + permissionId + " - " + e.getMessage());
            throw e;
        }
    }

    // ===== QUERY ENDPOINTS =====

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
    @RequiresPermission("ROLE_VIEW")
    @Operation(
        summary = "Get role by ID",
        description = "Retrieves a specific role by its unique identifier. **Required permission: ROLE_VIEW**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Role found successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Role Response",
                value = """
                {
                    "roleId": "660e8400-e29b-41d4-a716-446655440001",
                    "code": "MANAGER",
                    "name": "Project Manager",
                    "auditInfo": {
                        "createdAt": "2023-01-01T00:00:00Z",
                        "updatedAt": "2024-01-10T12:00:00Z"
                    },
                    "permissions": [
                        "880e8400-e29b-41d4-a716-446655440010",
                        "880e8400-e29b-41d4-a716-446655440011",
                        "880e8400-e29b-41d4-a716-446655440012"
                    ]
                }
                """
            )
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Role not found"
    )
    public Response getRoleById(@PathParam("roleId") UUID roleId) {
        Log.info("Request received: GET /roles/" + roleId + " - Retrieving role by ID");
        try {
            var role = roleQueryService.findById(roleId);
            if (role != null) {
                Log.info("Request successful: GET /roles/" + roleId + " - Role found");
                return Response.ok(role).build();
            }
            Log.info("Request completed: GET /roles/" + roleId + " - Role not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /roles/" + roleId + " - " + e.getMessage());
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
    @RequiresPermission("ROLE_VIEW")
    @Operation(
        summary = "Get all roles",
        description = "Returns a complete list of all defined roles. **Required permission: ROLE_VIEW**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Roles retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Roles List Response",
                value = """
                [
                    {
                        "roleId": "660e8400-e29b-41d4-a716-446655440001",
                        "code": "MANAGER",
                        "name": "Project Manager",
                        "auditInfo": {
                            "createdAt": "2023-01-01T00:00:00Z",
                            "updatedAt": "2024-01-10T12:00:00Z"
                        },
                        "permissions": [
                            "880e8400-e29b-41d4-a716-446655440010",
                            "880e8400-e29b-41d4-a716-446655440011"
                        ]
                    },
                    {
                        "roleId": "770e8400-e29b-41d4-a716-446655440002",
                        "code": "ANALYST",
                        "name": "Business Analyst",
                        "auditInfo": {
                            "createdAt": "2023-02-15T00:00:00Z",
                            "updatedAt": "2024-01-05T08:00:00Z"
                        },
                        "permissions": [
                            "880e8400-e29b-41d4-a716-446655440012"
                        ]
                    }
                ]
                """
            )
        )
    )
    public Response getAllRoles() {
        Log.info("Request received: GET /roles - Retrieving all roles");
        try {
            var roles = roleQueryService.findAllRoles();
            Log.info("Request successful: GET /roles - Retrieved " + roles.size() + " roles");
            return Response.ok(roles).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /roles - " + e.getMessage());
            throw e;
        }
    }

    // ========== PERMISSION ENDPOINTS ==========

    // ===== COMMAND ENDPOINTS =====

    /**
     * Creates a new permission in the system.
     * 
     * Defines a new permission that can be granted to roles to
     * control access to specific system functionality or resources.
     * 
     * @param cmd validated permission creation data
     * @return HTTP 201 Created on successful permission creation
     */
    @POST
    @Path("/permissions")
    @RequiresPermission("MANAGE_PERMISSIONS")
    @Operation(
        summary = "Create a new permission",
        description = "Creates a new permission that can be granted to roles. **Required permission: MANAGE_PERMISSIONS**"
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
    public Response createPermission(@Valid CreatePermissionCommand cmd) {
        Log.info("Request received: POST /permissions - Creating permission: " + cmd.code());
        try {
            createPermission.execute(cmd);
            Log.info("Request successful: POST /permissions - Permission created successfully: " + cmd.code());
            return Response.status(Response.Status.CREATED).build();
        } catch (Exception e) {
            Log.error("Request failed: POST /permissions - Failed to create permission: " + cmd.code() + " - " + e.getMessage());
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
     * @param cmd validated permission update data
     * @return HTTP 200 OK when the permission is successfully updated
     */
    @PUT
    @Path("/permissions/{permissionId}")
    @RequiresPermission("MANAGE_PERMISSIONS")
    @Operation(
        summary = "Update permission",
        description = "Updates an existing permission's information. **Required permission: MANAGE_PERMISSIONS**"
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
    public Response updatePermission(@PathParam("permissionId") UUID permissionId, @Valid UpdatePermissionCommand cmd) {
        Log.info("Request received: PUT /permissions/" + permissionId + " - Updating permission to code: " + cmd.code());
        try {
            updatePermission.execute(cmd);
            Log.info("Request successful: PUT /permissions/" + permissionId + " - Permission updated successfully to code: " + cmd.code());
            return Response.ok().build();
        } catch (Exception e) {
            Log.error("Request failed: PUT /permissions/" + permissionId + " - " + e.getMessage());
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
    @RequiresPermission("MANAGE_PERMISSIONS")
    @Operation(
        summary = "Delete permission",
        description = "Permanently deletes a permission from the system. **Required permission: MANAGE_PERMISSIONS**"
    )
    @APIResponse(
        responseCode = "204",
        description = "Permission deleted successfully"
    )
    @APIResponse(
        responseCode = "404",
        description = "Permission not found"
    )
    @RequestBody(
        description = "Permission deletion data",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Delete Permission Example",
                value = """
                {
                    "permissionId": "550e8400-e29b-41d4-a716-446655440000"
                }
                """
            )
        )
    )
    public Response deletePermission(@PathParam("permissionId") UUID permissionId, @Valid DeletePermissionCommand cmd) {
        Log.info("Request received: DELETE /permissions/" + permissionId + " - Deleting permission");
        try {
            deletePermission.execute(cmd);
            Log.info("Request successful: DELETE /permissions/" + permissionId + " - Permission deleted successfully");
            return Response.noContent().build();
        } catch (Exception e) {
            Log.error("Request failed: DELETE /permissions/" + permissionId + " - " + e.getMessage());
            throw e;
        }
    }

    // ===== QUERY ENDPOINTS =====

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
    @RequiresPermission("PERMISSION_VIEW")
    @Operation(
        summary = "Get permission by ID",
        description = "Retrieves a specific permission by its unique identifier. **Required permission: PERMISSION_VIEW**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Permission found successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Permission Response",
                value = """
                {
                    "id": "880e8400-e29b-41d4-a716-446655440010",
                    "code": "USER_CREATE",
                    "name": "Create User",
                    "description": "Allows creating new users in the system"
                }
                """
            )
        )
    )
    @APIResponse(
        responseCode = "404",
        description = "Permission not found"
    )
    public Response getPermissionById(@PathParam("permissionId") UUID permissionId) {
        Log.info("Request received: GET /permissions/" + permissionId + " - Retrieving permission by ID");
        try {
            var permission = permissionQueryService.findById(permissionId);
            if (permission != null) {
                Log.info("Request successful: GET /permissions/" + permissionId + " - Permission found");
                return Response.ok(permission).build();
            }
            Log.info("Request completed: GET /permissions/" + permissionId + " - Permission not found");
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /permissions/" + permissionId + " - " + e.getMessage());
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
    @RequiresPermission("PERMISSION_VIEW")
    @Operation(
        summary = "Get all permissions",
        description = "Returns a complete list of all defined permissions. **Required permission: PERMISSION_VIEW**"
    )
    @APIResponse(
        responseCode = "200",
        description = "Permissions retrieved successfully",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Permissions List Response",
                value = """
                [
                    {
                        "id": "880e8400-e29b-41d4-a716-446655440010",
                        "code": "USER_CREATE",
                        "name": "Create User",
                        "description": "Allows creating new users in the system"
                    },
                    {
                        "id": "880e8400-e29b-41d4-a716-446655440011",
                        "code": "USER_UPDATE",
                        "name": "Update User",
                        "description": "Allows updating existing user information"
                    },
                    {
                        "id": "880e8400-e29b-41d4-a716-446655440012",
                        "code": "USER_DELETE",
                        "name": "Delete User",
                        "description": "Allows permanently deleting users from the system"
                    },
                    {
                        "id": "880e8400-e29b-41d4-a716-446655440013",
                        "code": "USER_VIEW_ALL",
                        "name": "View All Users",
                        "description": "Allows viewing all users in the system"
                    }
                ]
                """
            )
        )
    )
    public Response getAllPermissions() {
        Log.info("Request received: GET /permissions - Retrieving all permissions");
        try {
            var permissions = permissionQueryService.findAllPermissions();
            Log.info("Request successful: GET /permissions - Retrieved " + permissions.size() + " permissions");
            return Response.ok(permissions).build();
        } catch (Exception e) {
            Log.error("Request failed: GET /permissions - " + e.getMessage());
            throw e;
        }
    }
}