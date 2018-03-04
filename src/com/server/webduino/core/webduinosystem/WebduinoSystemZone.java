package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystemZone {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private int id;
    private String name;
    private Zone zone;

    public WebduinoSystemZone(int id, String name, Zone zone, String type) {
        this.id = id;
        this.name = name;
        this.zone = zone;
     }

    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("zoneid", zone.id);
        return json;
    }
}
