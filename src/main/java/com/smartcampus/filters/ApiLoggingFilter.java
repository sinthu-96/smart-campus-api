package com.smartcampus.filters;

import javax.ws.rs.container.*;
import javax.ws.rs.ext.Provider;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * API observability filter implementing both request and response logging.
 *
 * Using JAX-RS filters for cross-cutting concerns (logging, auth, CORS) is
 * preferable to embedding Logger calls inside every resource method because:
 * - It follows the Single Responsibility Principle.
 * - It guarantees consistent logging across all endpoints without risk of omission.
 * - It keeps resource classes clean and focused on business logic.
 * - It can be enabled/disabled centrally without touching resource code.
 */
@Provider
public class ApiLoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = Logger.getLogger(ApiLoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(String.format(
            "[REQUEST]  Method=%-6s  URI=%s",
            requestContext.getMethod(),
            requestContext.getUriInfo().getRequestUri()
        ));
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(String.format(
            "[RESPONSE] Method=%-6s  URI=%-50s  Status=%d",
            requestContext.getMethod(),
            requestContext.getUriInfo().getRequestUri(),
            responseContext.getStatus()
        ));
    }
}
