package org.access.exposition.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;

import org.eclipse.microprofile.jwt.JsonWebToken;

import org.access.domain.model.PermissionCode;
import org.access.domain.model.Role;
import org.access.domain.model.RoleCode;
import org.access.domain.model.RoleId;
import org.access.domain.model.User;
import org.access.domain.model.UserId;
import org.access.domain.repository.PermissionRepository;
import org.access.domain.repository.RoleRepository;
import org.access.domain.repository.UserRepository;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * CDI Interceptor that enforces permission-based access control.
 *
 * This interceptor is triggered by the {@link RequiresPermission} annotation
 * and validates that the authenticated user has the required permissions.
 */
@Interceptor
@RequiresPermission({})
@Priority(Interceptor.Priority.APPLICATION)
public class PermissionInterceptor {

    private final JsonWebToken jwt;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Inject
    public PermissionInterceptor(JsonWebToken jwt, UserRepository userRepository,
                                  RoleRepository roleRepository, PermissionRepository permissionRepository) {
        this.jwt = jwt;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @AroundInvoke
    public Object checkPermission(InvocationContext context) throws Exception {
        RequiresPermission annotation = getAnnotation(context);

        if (annotation == null || annotation.value().length == 0) {
            return context.proceed();
        }

        // Get user ID from JWT
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

        // Load user and their permissions
        Set<String> userPermissions = loadUserPermissions(userId);

        // Check permissions based on operator
        String[] requiredPermissions = annotation.value();
        LogicalOperator operator = annotation.operator();

        boolean hasAccess = checkAccess(userPermissions, requiredPermissions, operator);

        if (!hasAccess) {
            throw new ForbiddenException(Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"Insufficient permissions. Required: " +
                        String.join(", ", requiredPermissions) + "\"}")
                .build());
        }

        return context.proceed();
    }

    /**
     * Gets the RequiresPermission annotation from method or class level.
     * Method-level annotation takes precedence over class-level.
     */
    private RequiresPermission getAnnotation(InvocationContext context) {
        Method method = context.getMethod();

        // Check method-level first
        RequiresPermission methodAnnotation = method.getAnnotation(RequiresPermission.class);
        if (methodAnnotation != null) {
            return methodAnnotation;
        }

        // Fall back to class-level
        return context.getTarget().getClass().getAnnotation(RequiresPermission.class);
    }

    /**
     * Loads all permission codes for the given user through their role.
     */
    private Set<String> loadUserPermissions(UUID userId) {
        User user = userRepository.findById(new UserId(userId))
            .orElseThrow(() -> new ForbiddenException(Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"User not found\"}")
                .build()));

        Role role = roleRepository.findById(user.getRoleId())
            .orElseThrow(() -> new ForbiddenException(Response.status(Response.Status.FORBIDDEN)
                .entity("{\"error\": \"Role not found\"}")
                .build()));

        return role.permissions().stream()
            .map(permissionId -> permissionRepository.findById(permissionId))
            .filter(opt -> opt.isPresent())
            .map(opt -> opt.get().code().value())
            .collect(Collectors.toSet());
    }

    /**
     * Checks if the user has access based on their permissions and the operator.
     */
    private boolean checkAccess(Set<String> userPermissions, String[] requiredPermissions, LogicalOperator operator) {
        if (operator == LogicalOperator.AND) {
            // User must have ALL permissions
            return Arrays.stream(requiredPermissions)
                .allMatch(userPermissions::contains);
        } else {
            // User must have at least ONE permission (OR - default)
            return Arrays.stream(requiredPermissions)
                .anyMatch(userPermissions::contains);
        }
    }
}
