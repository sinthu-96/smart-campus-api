package com.smartcampus.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Global catch-all exception mapper.
 *
 * Intercepts any unhandled Throwable (NullPointerException, IndexOutOfBoundsException, etc.)
 * and returns a generic HTTP 500 response WITHOUT exposing the internal stack trace.
 *
 * This is critical for security: raw stack traces reveal class names, library versions,
 * and internal paths that attackers can use to identify exploitable vulnerabilities.
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable ex) {
        // Log full detail server-side only — never expose to the client
        LOGGER.log(Level.SEVERE, "Unexpected server error: " + ex.getMessage(), ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "An unexpected internal server error occurred. Please contact support.");
        body.put("status", 500);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                       .type(MediaType.APPLICATION_JSON)
                       .entity(body)
                       .build();
    }
}
