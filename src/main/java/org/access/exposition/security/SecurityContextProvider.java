package org.access.exposition.security;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import org.access.domain.model.Permission;
import org.access.domain.model.PermissionCode;
import org.access.domain.model.Role;
import org.access.domain.model.SecurityContext;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.PermissionRepository;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Request-scoped provider that builds a {@link SecurityContext} from the current JWT.
 *
 * This provider extracts the user ID from the JWT token and loads the full security
 * context including the user's role and permissions.
 */
@RequestScoped
public class SecurityContextProvider {

    private final JsonWebToken jwt;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    private SecurityContext cachedContext;

    @Inject
    public SecurityContextProvider(JsonWebToken jwt, UserRepository userRepository,
                                    RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.jwt = jwt;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    /**
     * Gets the security context for the current authenticated user.
     * The context is cached for the duration of the request.
     *
     * @return the security context of the current user
     * @throws ForbiddenException if the user is not authenticated or not found
     */
    public SecurityContext getSecurityContext() {
        if (cachedContext != null) {
            return cachedContext;
        }

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
            .filter(opt -> opt.isPresent())
            .map(opt -> opt.get().code())
            .collect(Collectors.toSet());

        cachedContext = new SecurityContext(
            user.getId(),
            user.getEmail().value(),
            role.id(),
            role.code(),
            role.tier(),
            permissions
        );

        return cachedContext;
    }
}
