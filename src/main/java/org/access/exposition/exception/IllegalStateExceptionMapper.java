package org.access.exposition.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.access.exposition.dto.ErrorResponse;

/**
 * Exception mapper that converts IllegalStateException to HTTP 400 Bad Request.
 *
 * This mapper handles business rule violations represented by IllegalStateException,
 * such as attempting to delete a non-locked user.
 */
@Provider
public class IllegalStateExceptionMapper implements ExceptionMapper<IllegalStateException> {

    @Override
    public Response toResponse(IllegalStateException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                "INVALID_STATE",
                exception.getMessage()
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}
