package org.access.exposition.filter;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.UserRepository;
import org.eclipse.microprofile.jwt.JsonWebToken;

import io.quarkus.logging.Log;

import java.util.Optional;
import java.util.UUID;

/**
 * JAX-RS filter that verifies the authenticated user's status on every request.
 *
 * This filter ensures that users who have been locked or whose contract has expired
 * are immediately denied access, even if their JWT token is still valid.
 *
 * The filter runs after authentication (Priority.AUTHENTICATION + 1) to ensure
 * the JWT has been validated before accessing its claims.
 */
@Provider
@Priority(Priorities.AUTHENTICATION + 1)
public class UserStatusFilter implements ContainerRequestFilter {

    @Inject
    JsonWebToken jwt;

    @Inject
    UserRepository userRepository;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        // Skip if no authenticated user (public endpoints)
        if (jwt == null || jwt.getSubject() == null) {
            return;
        }

        try {
            UUID userId = UUID.fromString(jwt.getSubject());
            Optional<User> userOpt = userRepository.findById(new UserId(userId));

            if (userOpt.isEmpty()) {
                Log.warn("User not found for JWT subject: " + userId);
                abortWithForbidden(requestContext, "User not found");
                return;
            }

            User user = userOpt.get();

            if (user.isLocked()) {
                Log.info("Access denied: User " + userId + " is locked");
                abortWithForbidden(requestContext, "Account is locked. Please contact support.");
                return;
            }

            if (user.isContractExpired()) {
                Log.info("Access denied: User " + userId + " contract has expired");
                abortWithForbidden(requestContext, "Account contract has expired. Please contact support.");
                return;
            }

        } catch (IllegalArgumentException e) {
            Log.error("Invalid UUID in JWT subject: " + jwt.getSubject());
            abortWithForbidden(requestContext, "Invalid token");
        }
    }

    private void abortWithForbidden(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
            Response.status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse(message))
                .build()
        );
    }

    /**
     * Simple error response record for JSON serialization.
     */
    public record ErrorResponse(String message) {}
}
