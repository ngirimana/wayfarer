package org.acme.exception;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import java.util.HashMap;
import java.util.Map;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception exception) {
        int status = 500;
        String message = exception.getMessage();

        if (exception instanceof WebApplicationException) {
            status = ((WebApplicationException) exception).getResponse().getStatus();
        }

        Map<String, Object> error = new HashMap<>();
        error.put("status", status);
        error.put("error", message != null ? message : "Internal Server Error");

        return Response.status(status).entity(error).build();
    }
}
