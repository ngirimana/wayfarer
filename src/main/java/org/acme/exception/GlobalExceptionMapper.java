package org.acme.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.acme.payload.response.MessageResponse;
import org.jboss.logging.Logger;

/**
 * Maps every JAX-RS WebApplicationException (BadRequestException,
 * NotFoundException, ConflictException, …) to a consistent JSON body.
 *
 * When the exception was built with a pre-constructed Response that already
 * carries an entity (our service pattern), that embedded response is returned
 * as-is so the message is preserved exactly.
 *
 * When only a plain string message is present, it is wrapped in MessageResponse.
 *
 * Using ExceptionMapper<WebApplicationException> (specific type) is correct
 * for RESTEasy Reactive — the generic ExceptionMapper<Exception> is not
 * intercepted by the reactive runtime.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<WebApplicationException> {

    private static final Logger LOG = Logger.getLogger(GlobalExceptionMapper.class);

    @Override
    public Response toResponse(WebApplicationException exception) {
        Response embedded = exception.getResponse();

        // If the exception was created with a pre-built Response (entity already set),
        // pass it straight through — the JSON body is already correct.
        if (embedded != null && embedded.getEntity() != null) {
            LOG.debugf("WebApplicationException with embedded entity (status %d), passing through",
                    embedded.getStatus());
            return embedded;
        }

        // Otherwise wrap the raw string message in a JSON envelope.
        String message = exception.getMessage();
        int status = embedded != null ? embedded.getStatus()
                : Response.Status.INTERNAL_SERVER_ERROR.getStatusCode();
        LOG.warnf("WebApplicationException (status %d): %s", status, message);

        if (message == null || message.isBlank()) {
            message = "An error occurred";
        }

        return Response.status(status)
                .entity(new MessageResponse(message))
                .build();
    }
}
