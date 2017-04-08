package com.server.webduino.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class DoorSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(DoorSensor.class.getName());

    private boolean open;

    public interface CurrentSensorListener {
        void changeStatus(int sensorId, boolean open);
    }

    private List<CurrentSensorListener> listeners = new ArrayList<CurrentSensorListener>();
    public void addListener(CurrentSensorListener toAdd) {
        listeners.add(toAdd);
    }

    public DoorSensor() {
    }

    public void setStatus(boolean open) {

        LOGGER.info("setStatus");

        boolean oldOpen = this.open;
        this.open = open;

        if (open != oldOpen) {
            CurrentSensorDataLog dl = new CurrentSensorDataLog();
            dl.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (CurrentSensorListener hl : listeners)
                hl.changeStatus(id, open);
        }
    }

    @Override
    public void writeDataLog(String event) {
        DoorSensorDataLog dl = new DoorSensorDataLog();
        dl.writelog(event, this);
    }

    public boolean getStatus() {
        return open;
    }

    @Override
    void updateFromJson(Date date, JSONObject json) {

        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            lastUpdate = date;
            online = true;
            if (json.has("open"))
                setStatus(json.getBoolean("open"));
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
            json.put("current", open);
            json.put("name", getName());
            json.put("lastupdate", getStrLastUpdate());
            json.put("type", type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
