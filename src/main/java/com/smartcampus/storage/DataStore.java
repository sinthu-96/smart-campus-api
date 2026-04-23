package com.smartcampus.storage;

import com.smartcampus.models.Room;
import com.smartcampus.models.Sensor;
import com.smartcampus.models.SensorReading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton in-memory data store for all campus entities.
 *
 * This class holds the shared state across all resource instances.
 * Because JAX-RS creates a new resource instance per request by default,
 * a singleton store is essential to persist data between requests.
 *
 * Thread safety: For production use, ConcurrentHashMap should be used.
 * For this coursework, HashMap is sufficient as per specification requirements.
 */
public class DataStore {

    private static final DataStore INSTANCE = new DataStore();

    private final Map<String, Room> rooms = new HashMap<>();
    private final Map<String, Sensor> sensors = new HashMap<>();
    // Key: sensorId -> list of readings for that sensor
    private final Map<String, List<SensorReading>> sensorReadings = new HashMap<>();

    private DataStore() {
        seedData();
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ─── Room operations ───────────────────────────────────────────────────────

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Room getRoom(String id) {
        return rooms.get(id);
    }

    public void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public boolean deleteRoom(String id) {
        return rooms.remove(id) != null;
    }

    public boolean roomExists(String id) {
        return rooms.containsKey(id);
    }

    // ─── Sensor operations ─────────────────────────────────────────────────────

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    public Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public boolean sensorExists(String id) {
        return sensors.containsKey(id);
    }

    public boolean deleteSensor(String id) {
        return sensors.remove(id) != null;
    }

    // ─── SensorReading operations ──────────────────────────────────────────────

    public List<SensorReading> getReadingsForSensor(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public void addReading(String sensorId, SensorReading reading) {
        sensorReadings.computeIfAbsent(sensorId, k -> new ArrayList<>()).add(reading);
    }

    // ─── Seed sample data ──────────────────────────────────────────────────────

    private void seedData() {
        // Seed two rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("ENG-102", "Engineering Lab A", 30);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);

        // Seed two sensors linked to LIB-301
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE", 410.0, "LIB-301");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);

        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());

        // Seed one sensor in ENG-102
        Sensor s3 = new Sensor("OCC-001", "Occupancy", "MAINTENANCE", 15.0, "ENG-102");
        sensors.put(s3.getId(), s3);
        r2.getSensorIds().add(s3.getId());
    }
}
