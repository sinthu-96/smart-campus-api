package com.smartcampus.models;

import java.util.UUID;

/**
 * POJO representing a single timestamped measurement recorded by a sensor.
 */
public class SensorReading {

    private String id;
    private long timestamp;
    private double value;

    public SensorReading() {}

    public SensorReading(double value) {
        this.id = UUID.randomUUID().toString();
        this.timestamp = System.currentTimeMillis();
        this.value = value;
    }

    public String getId()                  { return id; }
    public void setId(String id)           { this.id = id; }

    public long getTimestamp()             { return timestamp; }
    public void setTimestamp(long t)       { this.timestamp = t; }

    public double getValue()               { return value; }
    public void setValue(double value)     { this.value = value; }
}
