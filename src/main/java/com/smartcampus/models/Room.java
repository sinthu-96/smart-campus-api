package com.smartcampus.models;

import java.util.ArrayList;
import java.util.List;

/**
 * POJO representing a physical room on campus.
 * Encapsulates room metadata and the IDs of sensors deployed within it.
 */
public class Room {

    private String id;
    private String name;
    private int capacity;
    private List<String> sensorIds = new ArrayList<>();

    // No-arg constructor required for JSON deserialisation
    public Room() {}

    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public String getId()                     { return id; }
    public void setId(String id)              { this.id = id; }

    public String getName()                   { return name; }
    public void setName(String name)          { this.name = name; }

    public int getCapacity()                  { return capacity; }
    public void setCapacity(int capacity)     { this.capacity = capacity; }

    public List<String> getSensorIds()        { return sensorIds; }
    public void setSensorIds(List<String> s)  { this.sensorIds = s; }
}
