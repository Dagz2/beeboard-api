package org.note.exposition.exception;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.note.domain.exception.ParentCommentNotFoundException;
import org.note.exposition.dto.ErrorResponse;

/**
 * Exception mapper that converts ParentCommentNotFoundException to HTTP 400 Bad Request.
 *
 * This mapper is automatically discovered and registered by Quarkus/JAX-RS runtime
 * via the @Provider annotation.
 */
@Provider
public class ParentCommentNotFoundExceptionMapper
        implements ExceptionMapper<ParentCommentNotFoundException> {

    @Override
    public Response toResponse(ParentCommentNotFoundException exception) {
        ErrorResponse errorResponse = new ErrorResponse(
                "PARENT_COMMENT_NOT_FOUND",
                exception.getMessage()
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(errorResponse)
                .build();
    }
}
