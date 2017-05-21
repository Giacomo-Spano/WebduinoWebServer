package com.server.webduino.core.webduinosystem.security;

import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.ZoneSensor;
import com.server.webduino.core.webduinosystem.ZoneProgram;
import com.server.webduino.core.webduinosystem.WebduinoZone;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class SecurityZone extends WebduinoZone implements SensorBase.SensorListener {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());


    public SecurityZone(int id, String name) {
        super(id, name);
    }

    /*@Override
    public void changeTemperature(int sensorId, double temperature) {

    }

    @Override
    public void changeAvTemperature(int sensorId, double avTemperature) {

    }

    @Override
    public void changeOnlineStatus(boolean online) {

    }

    @Override
    public void changeOnlineStatus(int sensorId, boolean online) {

    }*/

    @Override
    public void changeDoorStatus(int sensorId, boolean open) {

        ZoneProgram program = getActiveProgram();

        if (program != null)
            program.triggerAlarm();
    }
}
