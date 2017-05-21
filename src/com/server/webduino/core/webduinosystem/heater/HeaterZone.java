package com.server.webduino.core.webduinosystem.heater;

import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.WebduinoZone;
import com.server.webduino.core.webduinosystem.ZoneProgram;

import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class HeaterZone extends WebduinoZone /*implements SensorBase.SensorListener*/ {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());


    public HeaterZone(int id, String name) {
        super(id, name);
    }

    /*@Override
    public void onChangeTemperature(int sensorId, double temperature) {

    }*/

    /*@Override
    public void changeAvTemperature(int sensorId, double avTemperature) {

    }

    @Override
    public void changeOnlineStatus(boolean online) {

    }

    @Override
    public void changeOnlineStatus(int sensorId, boolean online) {

    }

    @Override
    public void changeDoorStatus(int sensorId, boolean open) {

        ZoneProgram program = getActiveProgram();

        if (program != null)
            program.triggerAlarm();
    }*/
}
