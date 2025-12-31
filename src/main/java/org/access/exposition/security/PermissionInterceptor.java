package org.access.exposition.security;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.core.Response;

import org.access.domain.model.PermissionCode;
import org.access.domain.model.SecurityContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * CDI Interceptor that enforces permission-based access control.
 *
 * This interceptor is triggered by the {@link RequiresPermission} annotation
 * and validates that the authenticated user has the required permissions.
 *
 * Uses {@link SecurityContextCache} for efficient permission loading with
 * Caffeine caching (15-minute TTL).
 */
@Interceptor
@RequiresPermission({})
@Priority(Interceptor.Priority.APPLICATION)
public class PermissionInterceptor {

    private final SecurityContextCache securityContextCache;

    @Inject
    public PermissionInterceptor(SecurityContextCache securityContextCache) {
        this.securityContextCache = securityContextCache;
    }

    @AroundInvoke
    public Object checkPermission(InvocationContext context) throws Exception {
        RequiresPermission annotation = getAnnotation(context);

        if (annotation == null || annotation.value().length == 0) {
            return context.proceed();
        }

        // Get security context from cache
        SecurityContext ctx = securityContextCache.getCurrentContext();

        // Extract permission codes as strings
        Set<String> userPermissions = ctx.permissions().stream()
            .map(PermissionCode::value)
            .collect(Collectors.toSet());

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
