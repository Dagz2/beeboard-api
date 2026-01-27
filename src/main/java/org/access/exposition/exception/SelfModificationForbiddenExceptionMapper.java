package org.access.exposition.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.access.domain.exception.SelfModificationForbiddenException;
import org.access.exposition.dto.ErrorResponse;

/**
 * Exception mapper that converts SelfModificationForbiddenException to HTTP 403 Forbidden.
 *
 * This mapper handles cases where a user attempts to modify their own account
 * in ways that are not permitted (e.g., self-lock, self-role-assignment).
 */
@Provider
public class SelfModificationForbiddenExceptionMapper
        implements ExceptionMapper<SelfModificationForbiddenException> {

    @Override
    public Response toResponse(SelfModificationForbiddenException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                "SELF_MODIFICATION_FORBIDDEN",
                exception.getMessage()
        );

        return Response.status(Response.Status.FORBIDDEN)
                .entity(errorResponse)
                .build();
    }
}
