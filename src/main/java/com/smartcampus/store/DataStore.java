package com.smartcampus.store;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton in-memory data store.
 * Uses ConcurrentHashMap to handle multiple simultaneous requests safely.
 * No database is used — all data lives in memory.
 */
public class DataStore {

    // ── Singleton ──────────────────────────────────────────────
    private static final DataStore INSTANCE = new DataStore();

    public static DataStore getInstance() {
        return INSTANCE;
    }

    // ── In-memory "tables" ────────────────────────────────────
    private final Map<String, Room>          rooms    = new ConcurrentHashMap<>();
    private final Map<String, Sensor>        sensors  = new ConcurrentHashMap<>();
    private final Map<String, List<SensorReading>> readings = new ConcurrentHashMap<>();

    // ── Pre-load sample data ──────────────────────────────────
    private DataStore() {

        // Sample rooms
        Room r1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room r2 = new Room("LAB-101", "Computer Lab A", 30);
        Room r3 = new Room("HALL-01", "Main Hall", 200);
        rooms.put(r1.getId(), r1);
        rooms.put(r2.getId(), r2);
        rooms.put(r3.getId(), r3);

        // Sample sensors
        Sensor s1 = new Sensor("TEMP-001", "Temperature", "ACTIVE",   22.5, "LIB-301");
        Sensor s2 = new Sensor("CO2-001",  "CO2",         "ACTIVE",  412.0, "LIB-301");
        Sensor s3 = new Sensor("OCC-001",  "Occupancy",   "MAINTENANCE", 0, "LAB-101");
        sensors.put(s1.getId(), s1);
        sensors.put(s2.getId(), s2);
        sensors.put(s3.getId(), s3);

        // Link sensors to their rooms
        r1.getSensorIds().add(s1.getId());
        r1.getSensorIds().add(s2.getId());
        r2.getSensorIds().add(s3.getId());

        // Initialise empty reading lists for each sensor
        readings.put(s1.getId(), new ArrayList<>());
        readings.put(s2.getId(), new ArrayList<>());
        readings.put(s3.getId(), new ArrayList<>());
    }

    // ── Accessors ─────────────────────────────────────────────

    public Map<String, Room> getRooms() {
        return rooms;
    }

    public Map<String, Sensor> getSensors() {
        return sensors;
    }

    /**
     * Returns the readings list for a sensor.
     * Creates an empty list if none exists yet.
     */
    public List<SensorReading> getReadingsForSensor(String sensorId) {
        readings.putIfAbsent(sensorId, new ArrayList<>());
        return readings.get(sensorId);
    }

    public void addReading(String sensorId, SensorReading reading) {
        getReadingsForSensor(sensorId).add(reading);
    }
}