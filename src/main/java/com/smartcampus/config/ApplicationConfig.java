package com.smartcampus.config;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

/**
 * JAX-RS Application bootstrap class.
 *
 * The @ApplicationPath annotation establishes the versioned root of the API.
 * All resource paths declared with @Path are relative to this base URI.
 *
 * Lifecycle note: By default, JAX-RS creates a new resource class instance
 * for every incoming HTTP request (request-scoped). This means resource
 * classes must NOT hold mutable state directly. Instead, shared state is
 * delegated to the DataStore singleton, which survives across requests.
 */
@ApplicationPath("/api/v1")
public class ApplicationConfig extends Application {
}
