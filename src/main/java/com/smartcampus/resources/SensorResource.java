package com.smartcampus.resources;

import com.smartcampus.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.Room;
import com.smartcampus.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Sensor resource — manages /api/v1/sensors
 *
 * Handles sensor registration, retrieval (with optional type-filter), and
 * delegates reading history to SensorReadingResource via a sub-resource locator.
 *
 * Foreign-key integrity: POST validates that the supplied roomId actually exists.
 * A missing roomId throws LinkedResourceNotFoundException → HTTP 422.
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // ── GET /api/v1/sensors[?type=...] ────────────────────────────────────────
    /**
     * Returns all sensors, optionally filtered by type using @QueryParam.
     *
     * Using a query parameter (?type=CO2) is preferred over a path segment
     * (/sensors/type/CO2) for filtering because:
     * - Query params are optional by nature; the base path /sensors still makes sense.
     * - Multiple filters can be composed (?type=CO2&status=ACTIVE) without changing the URI structure.
     * - Path params encode identity (which resource), query params encode search criteria.
     */
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> sensors = new ArrayList<>(store.getSensors().values());

        if (type != null && !type.isBlank()) {
            sensors = sensors.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type))
                    .collect(Collectors.toList());
        }

        return Response.ok(sensors).build();
    }

    // ── POST /api/v1/sensors ──────────────────────────────────────────────────
    /**
     * Registers a new sensor.
     *
     * Validates that the roomId referenced in the body actually exists.
     * If the room is absent, throws LinkedResourceNotFoundException (→ 422).
     *
     * The @Consumes(APPLICATION_JSON) annotation means JAX-RS will reject
     * any request with a Content-Type other than application/json with
     * HTTP 415 Unsupported Media Type before this method is even invoked.
     */
    @POST
    public Response createSensor(Sensor sensor) {
        if (sensor == null || sensor.getId() == null || sensor.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Sensor 'id' field is required.", 400))
                    .build();
        }

        if (store.sensorExists(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody("A sensor with id '" + sensor.getId() + "' already exists.", 409))
                    .build();
        }

        // Foreign-key validation: roomId must reference an existing room
        if (sensor.getRoomId() == null || sensor.getRoomId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Sensor 'roomId' field is required.", 400))
                    .build();
        }

        if (!store.roomExists(sensor.getRoomId())) {
            throw new LinkedResourceNotFoundException(
                "Cannot register sensor: the referenced roomId '" + sensor.getRoomId() +
                "' does not exist in the system. Create the room first.");
        }

        // Default status to ACTIVE if not supplied
        if (sensor.getStatus() == null || sensor.getStatus().isBlank()) {
            sensor.setStatus("ACTIVE");
        }

        store.addSensor(sensor);

        // Maintain the sensorIds list on the parent room for bidirectional navigation
        Room room = store.getRoom(sensor.getRoomId());
        if (room != null && !room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }

        return Response.status(Response.Status.CREATED).entity(sensor).build();
    }

    // ── GET /api/v1/sensors/{sensorId} ───────────────────────────────────────
    @GET
    @Path("/{sensorId}")
    public Response getSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor not found: " + sensorId, 404))
                    .build();
        }
        return Response.ok(sensor).build();
    }

    // ── DELETE /api/v1/sensors/{sensorId} ────────────────────────────────────
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor not found: " + sensorId, 404))
                    .build();
        }

        // Remove sensor ID from the parent room's list
        if (sensor.getRoomId() != null) {
            Room room = store.getRoom(sensor.getRoomId());
            if (room != null) {
                room.getSensorIds().remove(sensorId);
            }
        }

        store.deleteSensor(sensorId);
        return Response.noContent().build();
    }

    // ── Sub-resource locator: /api/v1/sensors/{sensorId}/readings ─────────────
    /**
     * Sub-resource locator pattern.
     *
     * JAX-RS resolves paths lazily: when the path matches up to "/{sensorId}/readings",
     * it calls this method to obtain the resource object that handles the remainder.
     * The returned SensorReadingResource then handles GET and POST on /readings.
     *
     * Benefits: each resource class has a single responsibility, the codebase scales
     * cleanly, and unit-testing individual sub-resources is straightforward.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        return new SensorReadingResource(sensorId);
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Map<String, Object> errorBody(String message, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        body.put("status", status);
        return body;
    }
}
