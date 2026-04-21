package com.smartcampus.resource;

import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Part 3 — Sensor Operations & Linking
 * Base path: /api/v1/sensors
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore store = DataStore.getInstance();

    // ── GET /api/v1/sensors ───────────────────────────────────
    // ── GET /api/v1/sensors?type=CO2 ─────────────────────────
    /**
     * Returns all sensors, optionally filtered by type.
     * The 'type' query parameter is completely optional.
     * If omitted, all sensors are returned.
     */
    @GET
    public Response getSensors(@QueryParam("type") String type) {

        List<Sensor> result = new ArrayList<>(store.getSensors().values());

        // Apply filter if type was provided
        if (type != null && !type.trim().isEmpty()) {
            result = result.stream()
                    .filter(s -> s.getType().equalsIgnoreCase(type.trim()))
                    .collect(Collectors.toList());
        }

        return Response.ok(result).build();
    }

    // ── GET /api/v1/sensors/{sensorId} ───────────────────────
    /**
     * Returns a single sensor by its id.
     */
    @GET
    @Path("/{sensorId}")
    public Response getSensorById(@PathParam("sensorId") String sensorId) {

        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error",   "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(404).entity(err).build();
        }

        return Response.ok(sensor).build();
    }

    // ── POST /api/v1/sensors ──────────────────────────────────
    /**
     * Registers a new sensor.
     * VALIDATES that the roomId in the request body refers to an existing room.
     * If not, throws LinkedResourceNotFoundException → 422.
     */
    @POST
    public Response createSensor(Sensor sensor) {

        // Validate required fields
        if (sensor.getId() == null || sensor.getId().trim().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error",   "Bad Request");
            err.put("message", "Sensor 'id' is required.");
            return Response.status(400).entity(err).build();
        }

        if (sensor.getRoomId() == null || sensor.getRoomId().trim().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error",   "Bad Request");
            err.put("message", "Sensor 'roomId' is required.");
            return Response.status(400).entity(err).build();
        }

        // Check for duplicate sensor id
        if (store.getSensors().containsKey(sensor.getId())) {
            Map<String, String> err = new HashMap<>();
            err.put("error",   "Conflict");
            err.put("message", "A sensor with id '" + sensor.getId() + "' already exists.");
            return Response.status(409).entity(err).build();
        }

        // Validate that the referenced room exists — throws 422 if not
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room == null) {
            throw new LinkedResourceNotFoundException(
                "Cannot register sensor. Room with id '"
                + sensor.getRoomId()
                + "' does not exist in the system."
            );
        }

        // Default status to ACTIVE if not provided
        if (sensor.getStatus() == null || sensor.getStatus().trim().isEmpty()) {
            sensor.setStatus("ACTIVE");
        }

        // Save sensor
        store.getSensors().put(sensor.getId(), sensor);

        // Link sensor ID to the room
        room.getSensorIds().add(sensor.getId());

        // Return 201 Created
        Map<String, Object> success = new HashMap<>();
        success.put("message", "Sensor registered successfully.");
        success.put("sensor",  sensor);
        return Response.status(201).entity(success).build();
    }

    // ── DELETE /api/v1/sensors/{sensorId} ────────────────────
    /**
     * Deletes a sensor and removes it from its room's sensorIds list.
     */
    @DELETE
    @Path("/{sensorId}")
    public Response deleteSensor(@PathParam("sensorId") String sensorId) {

        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error",   "Not Found");
            err.put("message", "Sensor '" + sensorId + "' does not exist.");
            return Response.status(404).entity(err).build();
        }

        // Remove sensor from its room's list
        Room room = store.getRooms().get(sensor.getRoomId());
        if (room != null) {
            room.getSensorIds().remove(sensorId);
        }

        store.getSensors().remove(sensorId);

        Map<String, String> success = new HashMap<>();
        success.put("message", "Sensor '" + sensorId + "' deleted successfully.");
        return Response.ok(success).build();
    }

    // ── SUB-RESOURCE LOCATOR ──────────────────────────────────
    /**
     * Part 4 — Sub-resource locator pattern.
     * Delegates all /sensors/{sensorId}/readings requests
     * to a dedicated SensorReadingResource class.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(
            @PathParam("sensorId") String sensorId) {

        Sensor sensor = store.getSensors().get(sensorId);

        if (sensor == null) {
            throw new NotFoundException(
                "Sensor '" + sensorId + "' does not exist."
            );
        }

        return new SensorReadingResource(sensor);
    }
}