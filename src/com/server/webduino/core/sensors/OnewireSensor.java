package com.server.webduino.core.sensors;

import org.json.JSONException;
import org.json.JSONObject;

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

    public OnewireSensor(int id, String name, String subaddress, int shieldid) {
        //statusUpdatePath = "/temperaturesensorstatus";
        super(id, name, subaddress, shieldid);
        type = "onewiresensor";
    }

    @Override
    public void writeDataLog(String event) {
        TemperatureSensorDataLog dl = new TemperatureSensorDataLog();
        dl.writelog(event, this);
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            lastUpdate = date;
            online = true;
            super.setData(shieldid, subaddress, name, date);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }
    }

    @Override
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
    }
}
