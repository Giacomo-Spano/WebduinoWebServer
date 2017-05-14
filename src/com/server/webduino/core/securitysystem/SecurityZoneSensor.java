package com.server.webduino.core.securitysystem;

/**
 * Created by giaco on 12/05/2017.
 */
public class SecurityZoneSensor {
    int id;
    private int sensorId;

    int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    int getSensorId() {
        return sensorId;
    }

    void setSensorId(int id) {
        this.sensorId = id;
    }
}
