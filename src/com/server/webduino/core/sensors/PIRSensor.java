package com.server.webduino.core.sensors;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class PIRSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(PIRSensor.class.getName());

    private boolean motionDetected;

    public interface PIRSensorListener {
        void changeStatus(int sensorId, boolean motionDetected);
    }

    private List<PIRSensorListener> listeners = new ArrayList<PIRSensorListener>();
    public void addListener(PIRSensorListener toAdd) {
        listeners.add(toAdd);
    }

    public PIRSensor(int id, String name, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, subaddress, shieldid, pin, enabled);
        type = "pirsensor";
    }

    public void setStatus(boolean motionDetected) {

        LOGGER.info("setStatus");

        boolean oldMotionDetected = this.motionDetected;
        this.motionDetected = motionDetected;

        if (motionDetected != oldMotionDetected) {
            CurrentSensorDataLog dl = new CurrentSensorDataLog();
            dl.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (PIRSensorListener hl : listeners)
                hl.changeStatus(id, motionDetected);
        }
    }

    @Override
    public void writeDataLog(String event) {
        PIRSensorDataLog dl = new PIRSensorDataLog();
        dl.writelog(event, this);
    }

    public boolean getStatus() {
        return motionDetected;
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            if (json.has("motionDetected"))
                setStatus(json.getBoolean("motionDetected"));

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
            json.put("current", motionDetected);
            json.put("name", getName());
            json.put("lastupdate", getStrLastUpdate());
            json.put("type", type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
