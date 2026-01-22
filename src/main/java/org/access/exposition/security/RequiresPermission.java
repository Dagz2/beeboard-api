package org.access.exposition.security;

import jakarta.enterprise.util.Nonbinding;
import jakarta.interceptor.InterceptorBinding;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Security annotation for permission-based access control.
 *
 * This annotation replaces {@code @RolesAllowed} with a more granular,
 * permission-based authorization model. It can be applied at class or method level.
 *
 * <p>Usage examples:</p>
 * <pre>
 * // Require a single permission
 * &#64;RequiresPermission("USER_CREATE")
 * public Response createUser(...) { }
 *
 * // Require any of multiple permissions (OR logic - default)
 * &#64;RequiresPermission(value = {"USER_VIEW_ALL", "USER_VIEW_TEAM"})
 * public Response getUsers(...) { }
 *
 * // Require all permissions (AND logic)
 * &#64;RequiresPermission(value = {"USER_VIEW_ALL", "REPORT_GENERATE"}, operator = LogicalOperator.AND)
 * public Response generateUserReport(...) { }
 * </pre>
 */
@Inherited
@InterceptorBinding
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface RequiresPermission {

    /**
     * The permission codes required to access the annotated resource.
     * By default, the user must have at least one of the specified permissions (OR logic).
     *
     * @return array of permission codes
     */
    @Nonbinding
    String[] value();

    /**
     * The logical operator to apply when multiple permissions are specified.
     * <ul>
     *   <li>{@link LogicalOperator#OR} (default): User must have at least one permission</li>
     *   <li>{@link LogicalOperator#AND}: User must have all specified permissions</li>
     * </ul>
     *
     * @return the logical operator
     */
    @Nonbinding
    LogicalOperator operator() default LogicalOperator.OR;
}
