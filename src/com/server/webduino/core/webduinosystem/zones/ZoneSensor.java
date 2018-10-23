package com.server.webduino.core.webduinosystem.zones;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by giaco on 12/05/2017.
 */
public class ZoneSensor {
    public int id;
    private int sensorid;
    public String name;


    int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSensorId() {
        return sensorid;
    }

    public void setSensorId(int id) {
        this.sensorid = id;
    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("sensorid", sensorid);
            json.put("name", name);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

}
