package com.server.webduino.core.sensors;

import org.json.JSONException;
import org.json.JSONObject;
import sun.management.Sensor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class OnewireSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(OnewireSensor.class.getName());

    public interface TemperatureSensorListener {
        void changeTemperature(int sensorId, double temperature);
        void changeAvTemperature(int sensorId, double avTemperature);
    }

    private List<TemperatureSensorListener> listeners = new ArrayList<TemperatureSensorListener>();

    public void addListener(TemperatureSensorListener toAdd) {
        listeners.add(toAdd);
    }

    public OnewireSensor(int id, String name, String subaddress, int shieldid, String pin, boolean enabled) {
        //statusUpdatePath = "/temperaturesensorstatus";
        super(id, name, subaddress, shieldid, pin, enabled);
        type = "onewiresensor";
    }

    @Override
    public void writeDataLog(String event) {
        TemperatureSensorDataLog dl = new TemperatureSensorDataLog();
        dl.writelog(event, this);
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        /*try {

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }*/
    }

    public void getJSONField() {

    }

    /*@Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", getId());
            json.put("shieldid", shieldid);
            json.put("online", online);
            json.put("subaddress", subaddress);
            json.put("name", getName());
            json.put("lastupdate", getStrLastUpdate());
            json.put("type", type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }*/
}
