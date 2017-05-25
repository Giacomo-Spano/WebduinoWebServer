package com.server.webduino.core.webduinosystem.zones;

/**
 * Created by giaco on 12/05/2017.
 */
public class ZoneSensor {
    int id;
    private int sensorId;

    int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSensorId() {
        return sensorId;
    }

    public void setSensorId(int id) {
        this.sensorId = id;
    }
}
