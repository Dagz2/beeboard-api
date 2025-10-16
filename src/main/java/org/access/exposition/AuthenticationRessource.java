package org.access.exposition;

import org.access.application.dto.command.auth.LoginCommand;
import org.access.application.dto.response.AuthenticationResponse;
import org.access.application.usecase.command.auth.AuthenticateUser;

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
 * This resource provides HTTP endpoints for user authentication
 * and token management in the system.
 */
@Path("/api/v1/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Authentication", description = "Endpoints for user authentication and token management")
@ApplicationScoped
public class AuthenticationRessource {

    @Inject
    AuthenticateUser authenticateUser;

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
}
