package org.access.application.service;

import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import org.access.domain.model.PermissionCode;
import org.access.domain.model.Role;
import org.access.domain.model.RoleId;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.PermissionRepository;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Application service for managing SecurityContext objects.
 *
 * This service loads and caches security contexts with a 15-minute TTL to reduce
 * database queries for permission checks. The cache is invalidated proactively
 * when roles or permissions are modified.
 */
@ApplicationScoped
public class SecurityContextService {

    private final JsonWebToken jwt;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Inject
    public SecurityContextService(JsonWebToken jwt, UserRepository userRepository,
                                 RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.jwt = jwt;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Gets the security context for the current authenticated user.
     * Extracts userId from JWT and delegates to the cached loader.
     *
     * @return the security context of the current user
     * @throws ForbiddenException if the user is not authenticated or not found
     */
    public SecurityContext getCurrentContext() {
        String subject = jwt.getSubject();
        if (subject == null) {
            throw new ForbiddenException(Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"Authentication required\"}")
                .build());
        }

        UUID userId;
        try {
            userId = UUID.fromString(subject);
        } catch (IllegalArgumentException e) {
            throw new ForbiddenException(Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"Invalid authentication token\"}")
                .build());
        }

        return loadContext(userId);
    }

    /**
     * Loads and caches the security context for a user.
     * The result is cached for 15 minutes (configured in application.properties).
     *
     * @param userId the user's unique identifier
     * @return the security context for the user
     * @throws ForbiddenException if the user or role is not found
     */
    @CacheResult(cacheName = "security-context")
    public SecurityContext loadContext(UUID userId) {
        User user = userRepository.findById(new UserId(userId))
            .orElseThrow(() -> new ForbiddenException(Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"User not found\"}")
                .build()));

        Role role = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new ForbiddenException(Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"Role not found\"}")
                .build()));

        Set<PermissionCode> permissions = role.permissions().stream()
            .map(permissionId -> permissionRepository.findById(permissionId))
            .filter(Optional::isPresent)
            .map(opt -> opt.get().code())
            .collect(Collectors.toSet());

        return new SecurityContext(
            user.getId(),
            user.getEmail().value(),
            role.id(),
            role.code(),
            role.tier(),
            permissions
        );
    }

    /**
     * Invalidates the cache for a specific user.
     * Called when the user's role is changed.
     *
     * @param userId the user's unique identifier
     */
    @CacheInvalidate(cacheName = "security-context")
    public void invalidate(UUID userId) {
        // Cache entry will be removed by the annotation
    }

    /**
     * Invalidates cache for all users with a specific role.
     * Called when role permissions are modified.
     *
     * @param roleId the role's unique identifier
     */
    public void invalidateByRole(RoleId roleId) {
        userRepository.findByRoleId(roleId)
            .forEach(user -> invalidate(user.getId().id()));
    }
}
