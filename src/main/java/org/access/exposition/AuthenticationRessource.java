package org.access.exposition;

import org.access.application.dto.command.auth.CompletePasswordResetCommand;
import org.access.application.dto.command.auth.LoginCommand;
import org.access.application.dto.command.auth.RequestPasswordResetCommand;
import org.access.application.dto.response.AuthenticationResponse;
import org.access.application.usecase.command.auth.AuthenticateUser;
import org.access.application.usecase.command.auth.CompletePasswordReset;
import org.access.application.usecase.command.auth.RequestPasswordReset;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import io.quarkus.logging.Log;

import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.ExampleObject;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

/**
 * REST resource for authentication operations.
 *
 * This resource provides HTTP endpoints for user authentication,
 * password reset, and token management in the system.
 */
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Endpoints for user authentication and token management")
@ApplicationScoped
public class AuthenticationRessource {

    @Inject
    AuthenticateUser authenticateUser;

    @Inject
    RequestPasswordReset requestPasswordReset;

    @Inject
    CompletePasswordReset completePasswordReset;

    /**
     * Authenticates a user and returns a JWT token.
     *
     * Validates the user's email and password, checks account status,
     * and generates a JWT token on successful authentication.
     *
     * @param command validated login credentials (email and password)
     * @return HTTP 200 OK with JWT token on successful authentication
     * @throws NotAuthorizedException if credentials are invalid or account is locked/expired
     */
    @POST
    @Path("/login")
    @Operation(
        summary = "Authenticate user",
        description = "Authenticates a user with email and password, returning a JWT token on success"
    )
    @APIResponse(
        responseCode = "200",
        description = "Authentication successful",
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Successful Authentication",
                value = """
                {
                    "token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
                    "tokenType": "Bearer",
                    "expiresIn": 3600
                }
                """
            )
        )
    )
    @APIResponse(
        responseCode = "401",
        description = "Authentication failed - invalid credentials, locked account, or expired contract"
    )
    @RequestBody(
        description = "User login credentials",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Login Request",
                value = """
                {
                    "email": "john.doe@example.com",
                    "password": "SecurePassword123"
                }
                """
            )
        )
    )
    public Response login(@Valid LoginCommand command) {
        Log.info("Request received: POST /auth/login - Authenticating user: " + command.email());
        try {
            AuthenticationResponse response = authenticateUser.execute(command);
            Log.info("Request successful: POST /auth/login - User authenticated successfully: " + command.email());
            return Response.ok(response).build();
        } catch (NotAuthorizedException e) {
            Log.warn("Request failed: POST /auth/login - Authentication failed for user: " + command.email() + " - " + e.getMessage());
            throw e;
        } catch (Exception e) {
            Log.error("Request failed: POST /auth/login - Unexpected error during authentication: " + command.email() + " - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Requests a password reset for a user.
     *
     * Sends an email with a time-limited reset token to the specified email address.
     * For security, always returns success even if the email doesn't exist.
     *
     * @param command validated password reset request (email)
     * @return HTTP 200 OK if request is processed
     * @throws ClientErrorException if rate limit is exceeded (HTTP 429)
     */
    @POST
    @Path("/password-reset/request")
    @Operation(
        summary = "Request password reset",
        description = "Sends a password reset email with a time-limited token (15 minutes validity). Always returns success to prevent email enumeration."
    )
    @APIResponse(
        responseCode = "200",
        description = "Request processed successfully (always returns success for security)"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid input data (validation failure)"
    )
    @APIResponse(
        responseCode = "429",
        description = "Too many requests - rate limit exceeded (max 3 requests per hour)"
    )
    @RequestBody(
        description = "Password reset request",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Request Password Reset",
                value = """
                {
                    "email": "john.doe@example.com"
                }
                """
            )
        )
    )
    public Response requestPasswordReset(@Valid RequestPasswordResetCommand command) {
        Log.info("Request received: POST /auth/password-reset/request - " + command.email());
        try {
            requestPasswordReset.execute(command);
            Log.info("Request successful: POST /auth/password-reset/request");
            return Response.ok().build();
        } catch (ClientErrorException e) {
            Log.warn("Request failed: POST /auth/password-reset/request - " + e.getMessage());
            throw e;
        } catch (Exception e) {
            Log.error("Request failed: POST /auth/password-reset/request - " + e.getMessage());
            throw e;
        }
    }

    /**
     * Completes a password reset using a token.
     *
     * Validates the reset token and updates the user's password.
     *
     * @param command validated password reset completion (token and new password)
     * @return HTTP 200 OK if password is reset successfully
     * @throws BadRequestException if token is invalid, expired, or password validation fails
     */
    @POST
    @Path("/password-reset/complete")
    @Operation(
        summary = "Complete password reset",
        description = "Resets the user's password using the token from the reset email"
    )
    @APIResponse(
        responseCode = "200",
        description = "Password reset successful"
    )
    @APIResponse(
        responseCode = "400",
        description = "Invalid token, expired token, or password validation failed"
    )
    @RequestBody(
        description = "Password reset completion",
        required = true,
        content = @Content(
            mediaType = "application/json",
            examples = @ExampleObject(
                name = "Complete Password Reset",
                value = """
                {
                    "token": "a1b2c3d4e5f6789012345678901234567890123456789012345678901234",
                    "newPassword": "MyNewSecurePassword123!@#$2024Pass"
                }
                """
            )
        )
    )
    public Response completePasswordReset(@Valid CompletePasswordResetCommand command) {
        Log.info("Request received: POST /auth/password-reset/complete");
        try {
            completePasswordReset.execute(command);
            Log.info("Request successful: POST /auth/password-reset/complete");
            return Response.ok().build();
        } catch (IllegalArgumentException e) {
            Log.warn("Request failed: POST /auth/password-reset/complete - " + e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (IllegalStateException e) {
            Log.error("Request failed: POST /auth/password-reset/complete - " + e.getMessage());
            throw new BadRequestException(e.getMessage());
        } catch (Exception e) {
            Log.error("Request failed: POST /auth/password-reset/complete - " + e.getMessage());
            throw e;
        }
    }
}
