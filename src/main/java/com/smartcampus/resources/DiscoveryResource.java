package com.smartcampus.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Discovery endpoint — GET /api/v1
 *
 * Returns API metadata and a map of primary resource collection URIs.
 * This implements HATEOAS (Hypermedia As The Engine Of Application State):
 * clients can navigate the entire API starting from this single well-known URL,
 * without relying on out-of-band static documentation.
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover() {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("api",         "Smart Campus Sensor & Room Management API");
        response.put("version",     "1.0.0");
        response.put("description", "RESTful API for managing campus rooms, sensors, and sensor readings.");
        response.put("contact",     "admin@smartcampus.ac.uk");
        response.put("status",      "operational");

        // HATEOAS links — clients can discover all primary resource endpoints here
        Map<String, String> resources = new LinkedHashMap<>();
        resources.put("rooms",   "/api/v1/rooms");
        resources.put("sensors", "/api/v1/sensors");
        response.put("resources", resources);

        return Response.ok(response).build();
    }
}
