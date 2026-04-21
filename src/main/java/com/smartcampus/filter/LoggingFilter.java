package com.smartcampus.filter;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Logs every incoming request and every outgoing response.
 * Runs automatically on ALL endpoints — no manual logging needed.
 *
 * Example log output:
 *   [REQUEST]  POST  →  /api/v1/sensors
 *   [RESPONSE] 201   ←  /api/v1/sensors
 */
@Provider
public class LoggingFilter
        implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER =
            Logger.getLogger(LoggingFilter.class.getName());

    /** Runs BEFORE the request reaches your resource method */
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        LOGGER.info(
            "[REQUEST]  "
            + requestContext.getMethod()
            + "  →  "
            + requestContext.getUriInfo().getRequestUri()
        );
    }

    /** Runs AFTER your resource method has returned a response */
    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        LOGGER.info(
            "[RESPONSE] "
            + responseContext.getStatus()
            + "   ←  "
            + requestContext.getUriInfo().getRequestUri()
        );
    }
}