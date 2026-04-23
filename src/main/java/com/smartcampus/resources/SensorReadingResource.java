package com.smartcampus.resources;

import com.smartcampus.exceptions.SensorUnavailableException;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;
import com.smartcampus.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Sub-resource for sensor readings — /api/v1/sensors/{sensorId}/readings
 *
 * Manages the historical reading log for a specific sensor.
 * Instantiated per-request by the sub-resource locator in SensorResource.
 *
 * Side effect: a successful POST updates the parent Sensor's currentValue,
 * ensuring data consistency across the API without a separate PATCH call.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore store = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    // ── GET /api/v1/sensors/{sensorId}/readings ───────────────────────────────
    /**
     * Returns the full reading history for the sensor.
     */
    @GET
    public Response getReadings() {
        Sensor sensor = store.getSensor(sensorId);
        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor not found: " + sensorId, 404))
                    .build();
        }
        List<SensorReading> readings = store.getReadingsForSensor(sensorId);
        return Response.ok(readings).build();
    }

    // ── POST /api/v1/sensors/{sensorId}/readings ──────────────────────────────
    /**
     * Appends a new reading to the sensor's history.
     *
     * Business rules enforced:
     *  1. Sensor must exist (404 if not).
     *  2. Sensor status must NOT be "MAINTENANCE" or "OFFLINE" → 403 Forbidden.
     *  3. On success, parent sensor.currentValue is updated to the new reading's value.
     */
    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = store.getSensor(sensorId);

        if (sensor == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Sensor not found: " + sensorId, 404))
                    .build();
        }

        // State constraint: sensors under maintenance or offline cannot accept readings
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is currently under MAINTENANCE and cannot accept new readings.");
        }
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensorId + "' is OFFLINE and cannot accept new readings.");
        }

        if (reading == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Request body with a 'value' field is required.", 400))
                    .build();
        }

        // Create and persist the reading (auto-assigns UUID and timestamp)
        SensorReading newReading = new SensorReading(reading.getValue());
        store.addReading(sensorId, newReading);

        // Side effect: keep parent sensor's currentValue in sync with latest reading
        sensor.setCurrentValue(newReading.getValue());

        return Response.status(Response.Status.CREATED).entity(newReading).build();
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Map<String, Object> errorBody(String message, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        body.put("status", status);
        return body;
    }
}
