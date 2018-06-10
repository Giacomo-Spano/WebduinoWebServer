package com.server.webduino.core.webduinosystem.zones;

import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.zones.Zone;

import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class HeaterZone extends Zone implements SensorBase.SensorListener {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());


    public HeaterZone(int id, String name, String type) {
        super(id, name);
    }


}
