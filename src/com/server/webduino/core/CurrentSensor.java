package com.server.webduino.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class CurrentSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(CurrentSensor.class.getName());

    private double current;

    public interface CurrentSensorListener {
        void changeCurrent(int sensorId, double current);
    }

    private List<CurrentSensorListener> listeners = new ArrayList<CurrentSensorListener>();
    public void addListener(CurrentSensorListener toAdd) {
        listeners.add(toAdd);
    }

    public CurrentSensor() {
    }

    public void setCurrent(double current) {

        LOGGER.info("setCurrent");

        double oldCurrent = this.current;
        this.current = current;

        if (current != oldCurrent) {
            CurrentSensorDataLog dl = new CurrentSensorDataLog();
            dl.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (CurrentSensorListener hl : listeners)
                hl.changeCurrent(id, current);
        }
    }

    @Override
    public void writeDataLog(String event) {
        CurrentSensorDataLog dl = new CurrentSensorDataLog();
        dl.writelog(event, this);
    }

    public double getCurrent() {
        return current;
    }

    @Override
    void updateFromJson(Date date, JSONObject json) {

        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            lastUpdate = date;
            online = true;
            if (json.has("current"))
                setCurrent(json.getDouble("current"));
            if (json.has("name"))
                name = json.getString("name");
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
            json.put("current", getCurrent());
            json.put("name", getName());
            json.put("lastupdate", getStrLastUpdate());
            json.put("type", type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
