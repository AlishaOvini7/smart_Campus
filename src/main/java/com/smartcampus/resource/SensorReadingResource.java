package com.smartcampus.resource;

import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 4 — Historical Data Management
 * Handles all requests under /api/v1/sensors/{sensorId}/readings
 *
 * This class is NOT registered with @Path at the top — it is returned
 * by the sub-resource locator method in SensorResource, which is how
 * JAX-RS knows to route requests here.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final Sensor    sensor;
    private final DataStore store = DataStore.getInstance();

    // Receives the specific sensor context from the locator
    public SensorReadingResource(Sensor sensor) {
        this.sensor = sensor;
    }

    // ── GET /api/v1/sensors/{sensorId}/readings ───────────────
    /**
     * Returns the full historical log of readings for this sensor.
     */
    @GET
    public Response getReadings() {
        List<SensorReading> history = store.getReadingsForSensor(sensor.getId());
        return Response.ok(history).build();
    }

    // ── POST /api/v1/sensors/{sensorId}/readings ──────────────
    /**
     * Appends a new reading to this sensor's history.
     *
     * BLOCKED if sensor status is MAINTENANCE — throws SensorUnavailableException (403).
     *
     * SIDE EFFECT: Also updates the sensor's currentValue field
     * to keep data consistent across the API.
     */
    @POST
    public Response addReading(SensorReading reading) {

        // Block if sensor is under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensor.getId()
                + "' is currently under MAINTENANCE and cannot accept new readings."
            );
        }

        // Block if sensor is offline
        if ("OFFLINE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                "Sensor '" + sensor.getId()
                + "' is OFFLINE and cannot accept new readings."
            );
        }

        // Create a proper reading with auto-generated id and timestamp
        SensorReading newReading = new SensorReading(reading.getValue());

        // Save to history
        store.addReading(sensor.getId(), newReading);

        // *** SIDE EFFECT: update parent sensor's currentValue ***
        sensor.setCurrentValue(reading.getValue());

        Map<String, Object> success = new HashMap<>();
        success.put("message", "Reading recorded successfully.");
        success.put("reading", newReading);
        success.put("sensorCurrentValue", sensor.getCurrentValue());

        return Response.status(201).entity(success).build();
    }
}