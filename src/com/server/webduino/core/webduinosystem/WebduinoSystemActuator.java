package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystemActuator {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private int id;
    private String name;
    private SensorBase actuator;

    public WebduinoSystemActuator(int id, String name, SensorBase actuator) {
        this.id = id;
        this.name = name;
        this.actuator = actuator;
    }
    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("actuatorid", actuator.getId());
        return json;
    }
}
