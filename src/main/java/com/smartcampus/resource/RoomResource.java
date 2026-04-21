package com.smartcampus.resource;

import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import com.smartcampus.store.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Part 2 — Room Management
 * Base path: /api/v1/rooms
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // ── GET /api/v1/rooms ──────────────────────────────────────
    /**
     * Returns the full list of all rooms in the system.
     */
    @GET
    public Response getAllRooms() {
        List<Room> roomList = new ArrayList<>(store.getRooms().values());
        return Response.ok(roomList).build();
    }

    // ── POST /api/v1/rooms ────────────────────────────────────
    /**
     * Creates a new room.
     * Returns 201 Created with the new room in the body.
     * Returns 400 if id or name is missing.
     * Returns 409 if a room with the same id already exists.
     */
    @POST
    public Response createRoom(Room room) {

        // Validate required fields
        if (room.getId() == null || room.getId().trim().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error",   "Bad Request");
            err.put("message", "Room 'id' is required.");
            return Response.status(400).entity(err).build();
        }

        if (room.getName() == null || room.getName().trim().isEmpty()) {
            Map<String, String> err = new HashMap<>();
            err.put("error",   "Bad Request");
            err.put("message", "Room 'name' is required.");
            return Response.status(400).entity(err).build();
        }

        // Check for duplicate
        if (store.getRooms().containsKey(room.getId())) {
            Map<String, String> err = new HashMap<>();
            err.put("error",   "Conflict");
            err.put("message", "A room with id '" + room.getId() + "' already exists.");
            return Response.status(409).entity(err).build();
        }

        // Save to store
        store.getRooms().put(room.getId(), room);

        // Return 201 Created
        Map<String, Object> success = new HashMap<>();
        success.put("message", "Room created successfully.");
        success.put("room",    room);
        return Response.status(201).entity(success).build();
    }

    // ── GET /api/v1/rooms/{roomId} ────────────────────────────
    /**
     * Returns detailed metadata for one specific room.
     * Returns 404 if not found.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoomById(@PathParam("roomId") String roomId) {

        Room room = store.getRooms().get(roomId);

        if (room == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error",   "Not Found");
            err.put("message", "Room '" + roomId + "' does not exist.");
            return Response.status(404).entity(err).build();
        }

        return Response.ok(room).build();
    }

    // ── DELETE /api/v1/rooms/{roomId} ─────────────────────────
    /**
     * Deletes a room.
     * BLOCKED if the room still has sensors — throws RoomNotEmptyException (409).
     * Returns 404 if room does not exist.
     * Returns 200 OK on successful deletion.
     *
     * Idempotency note: The first DELETE succeeds (200).
     * Subsequent DELETEs on the same id return 404 — safe but not strictly idempotent.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {

        Room room = store.getRooms().get(roomId);

        // 404 if room doesn't exist
        if (room == null) {
            Map<String, String> err = new HashMap<>();
            err.put("error",   "Not Found");
            err.put("message", "Room '" + roomId + "' does not exist.");
            return Response.status(404).entity(err).build();
        }

        // 409 if room still has sensors — throw custom exception
        if (!room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Cannot delete room '" + roomId
                + "'. It still has " + room.getSensorIds().size()
                + " sensor(s) assigned: " + room.getSensorIds()
            );
        }

        // Safe to delete
        store.getRooms().remove(roomId);

        Map<String, String> success = new HashMap<>();
        success.put("message", "Room '" + roomId + "' deleted successfully.");
        return Response.ok(success).build();
    }
}