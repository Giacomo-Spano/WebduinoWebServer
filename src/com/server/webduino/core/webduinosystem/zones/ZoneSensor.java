package com.server.webduino.core.webduinosystem.zones;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.management.Sensor;

import java.util.Date;

/**
 * Created by giaco on 12/05/2017.
 */
public class ZoneSensor {
    public int id;
    private int sensorid;

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

    public String getStatus() {
        SensorBase sensor = Core.getSensorFromId(sensorid);
        if (sensor != null) {
            return sensor.getStatus().description;
        }
        return "error";
    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("sensorid", sensorid);
            //json.put("name", name);
            SensorBase sensor = Core.getSensorFromId(sensorid);
            if (sensor != null) {
                json.put("sensorname", sensor.getName());
                json.put("sensortype", sensor.getType());
                json.put("status", sensor.getStatus().toJson());
                json.put("value", sensor.getValue());
                json.put("valueunit", sensor.getValueUnit());
                json.put("valuetype", sensor.getValueType());
            }

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

}
