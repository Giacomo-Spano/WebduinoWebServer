package com.server.webduino.core.webduinosystem.zones;

import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.zones.Zone;

import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class SecurityZone extends Zone implements SensorBase.SensorListener {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());


    public SecurityZone(int id, String name) {
        super(id, name);
    }


}
