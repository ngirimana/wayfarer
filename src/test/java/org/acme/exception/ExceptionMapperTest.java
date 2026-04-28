package org.acme.exception;

import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.acme.payload.response.MessageResponse;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class ExceptionMapperTest {

    @Test
    public void testGlobalExceptionMapperWithEmbeddedEntity() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();
        
        Response embedded = Response.status(400).entity(new MessageResponse("Embedded message")).build();
        WebApplicationException ex = new WebApplicationException(embedded);

        Response response = mapper.toResponse(ex);
        
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity() instanceof MessageResponse);
        assertEquals("Embedded message", ((MessageResponse) response.getEntity()).message);
    }

    @Test
    public void testGlobalExceptionMapperWithMessage() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();
        
        BadRequestException ex = new BadRequestException("Bad request error");

        Response response = mapper.toResponse(ex);
        
        assertEquals(400, response.getStatus());
        assertTrue(response.getEntity() instanceof MessageResponse);
        assertEquals("Bad request error", ((MessageResponse) response.getEntity()).message);
    }

    @Test
    public void testGlobalExceptionMapperWithNullMessage() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();
        
        WebApplicationException ex = new WebApplicationException(); // No message

        Response response = mapper.toResponse(ex);
        
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity() instanceof MessageResponse);
        assertEquals("HTTP 500 Internal Server Error", ((MessageResponse) response.getEntity()).message);
    }

    @Test
    public void testGlobalExceptionMapperWithBlankMessage() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();
        
        WebApplicationException ex = new WebApplicationException("   ", 500);

        Response response = mapper.toResponse(ex);
        
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity() instanceof MessageResponse);
        assertEquals("An error occurred", ((MessageResponse) response.getEntity()).message);
    }

    @Test
    public void testGlobalExceptionHandlerWithException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        
        Exception ex = new Exception("Generic error");
        
        Response response = handler.toResponse(ex);
        
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity() instanceof Map);
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals(500, entity.get("status"));
        assertEquals("Generic error", entity.get("error"));
    }

    @Test
    public void testGlobalExceptionHandlerWithNullMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        
        Exception ex = new Exception((String) null);
        
        Response response = handler.toResponse(ex);
        
        assertEquals(500, response.getStatus());
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals("Internal Server Error", entity.get("error"));
    }

    @Test
    public void testGlobalExceptionHandlerWithWebApplicationException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        
        WebApplicationException ex = new BadRequestException("Bad request exception");
        
        Response response = handler.toResponse(ex);
        
        assertEquals(400, response.getStatus());
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals(400, entity.get("status"));
        assertEquals("Bad request exception", entity.get("error"));
    }

    @Test
    public void testGlobalExceptionHandlerWithWebApplicationExceptionNullMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        
        // We use a custom exception to ensure getMessage() returns null
        WebApplicationException ex = new WebApplicationException(401) {
            @Override
            public String getMessage() {
                return null;
            }
        };
        
        Response response = handler.toResponse(ex);
        
        assertEquals(401, response.getStatus());
        Map<String, Object> entity = (Map<String, Object>) response.getEntity();
        assertEquals(401, entity.get("status"));
        assertEquals("Internal Server Error", entity.get("error"));
    }

    @Test
    public void testGlobalExceptionMapperWithEmbeddedResponseNoEntity() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();
        
        Response embedded = Response.status(403).build(); // No entity
        WebApplicationException ex = new WebApplicationException(embedded);

        Response response = mapper.toResponse(ex);
        
        assertEquals(403, response.getStatus());
        assertTrue(response.getEntity() instanceof MessageResponse);
        assertNotNull(((MessageResponse) response.getEntity()).message);
    }

    @Test
    public void testGlobalExceptionMapperWithNullResponse() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();
        
        WebApplicationException ex = new WebApplicationException() {
            @Override
            public Response getResponse() {
                return null;
            }
            @Override
            public String getMessage() {
                return "Null response test";
            }
        };

        Response response = mapper.toResponse(ex);
        
        assertEquals(500, response.getStatus());
        assertTrue(response.getEntity() instanceof MessageResponse);
        assertEquals("Null response test", ((MessageResponse) response.getEntity()).message);
    }

    @Test
    public void testGlobalExceptionMapperWithAbsoluteNullMessage() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();
        
        WebApplicationException ex = new WebApplicationException() {
            @Override
            public String getMessage() {
                return null;
            }
        };

        Response response = mapper.toResponse(ex);
        
        assertEquals(500, response.getStatus());
        assertEquals("An error occurred", ((MessageResponse) response.getEntity()).message);
    }

    @Test
    public void testGlobalExceptionMapperWithEmptyMessage() {
        GlobalExceptionMapper mapper = new GlobalExceptionMapper();
        
        WebApplicationException ex = new WebApplicationException("", 400);

        Response response = mapper.toResponse(ex);
        
        assertEquals(400, response.getStatus());
        assertEquals("An error occurred", ((MessageResponse) response.getEntity()).message);
    }
}
