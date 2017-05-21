package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Devices;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public abstract class ZoneProgram {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private int id;
    private String name;
    private String type;
    private int seconds;
    private List<ZoneSensor> securityZoneSensors = new ArrayList<>();

    public ZoneProgram(int id, String name, String type, int seconds) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.seconds = seconds;
    }

    void setId(int id) {
        this.id = id;
    }

    int getId() {
        return id;
    }

    void setName(String name) {
        this.id = id;
    }

    String getName() {
        return name;
    }

    void setType(String type) {
        this.type = type;
    }

    String getType() {
        return type;
    }


    public abstract void changeDoorStatus(int sensorId, boolean open);

    public abstract void triggerAlarm();
}
