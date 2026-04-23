package com.smartcampus.resources;

import com.smartcampus.exceptions.RoomNotEmptyException;
import com.smartcampus.models.Room;
import com.smartcampus.storage.DataStore;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;

/**
 * Room resource — manages /api/v1/rooms
 *
 * Implements full CRUD for Room entities stored in the in-memory DataStore.
 * Business rule: a room cannot be deleted while it still has sensors assigned
 * (prevents orphan sensor records). Violation throws RoomNotEmptyException → HTTP 409.
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore store = DataStore.getInstance();

    // ── GET /api/v1/rooms ─────────────────────────────────────────────────────
    /**
     * Returns the full list of all registered rooms.
     * Returning complete objects (rather than IDs only) saves the client from
     * making N additional round-trips; trade-off is higher bandwidth per call.
     */
    @GET
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(store.getRooms().values());
        return Response.ok(rooms).build();
    }

    // ── POST /api/v1/rooms ────────────────────────────────────────────────────
    /**
     * Creates a new room.
     * Returns 201 Created with a Location header pointing at the new resource.
     */
    @POST
    public Response createRoom(Room room) {
        if (room == null || room.getId() == null || room.getId().isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorBody("Room 'id' field is required.", 400))
                    .build();
        }
        if (store.roomExists(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(errorBody("A room with id '" + room.getId() + "' already exists.", 409))
                    .build();
        }
        store.addRoom(room);
        return Response.status(Response.Status.CREATED)
                .entity(room)
                .build();
    }

    // ── GET /api/v1/rooms/{roomId} ────────────────────────────────────────────
    /**
     * Returns detailed metadata for a single room.
     */
    @GET
    @Path("/{roomId}")
    public Response getRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Room not found: " + roomId, 404))
                    .build();
        }
        return Response.ok(room).build();
    }

    // ── DELETE /api/v1/rooms/{roomId} ─────────────────────────────────────────
    /**
     * Deletes a room.
     *
     * Business constraint: deletion is blocked if the room still has sensors
     * assigned to it. This prevents sensor orphans and maintains referential
     * integrity without a database foreign-key constraint.
     *
     * Idempotency: The DELETE verb is idempotent by HTTP specification — repeated
     * calls produce the same server state. First call: room is removed (204).
     * Subsequent calls: room is already gone, returns 404. The observable server
     * state (room absent) is identical after both the 1st and Nth call; only the
     * response code differs, which is acceptable per RFC 7231.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = store.getRoom(roomId);
        if (room == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorBody("Room not found: " + roomId, 404))
                    .build();
        }

        // Safety check: block deletion if any sensors are still assigned
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            throw new RoomNotEmptyException(
                "Cannot delete room '" + roomId + "': it still has " +
                room.getSensorIds().size() + " active sensor(s) assigned. " +
                "Remove all sensors before decommissioning the room.");
        }

        store.deleteRoom(roomId);
        return Response.noContent().build();  // 204 No Content
    }

    // ── Helper ────────────────────────────────────────────────────────────────
    private Map<String, Object> errorBody(String message, int status) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", message);
        body.put("status", status);
        return body;
    }
}
