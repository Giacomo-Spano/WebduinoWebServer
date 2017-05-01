package com.server.webduino.core.sensors;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class HumiditySensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(HumiditySensor.class.getName());

    private int humidity;

    public interface CurrentSensorListener {
        void changeHumidity(int sensorId, double humidity);
    }

    private List<CurrentSensorListener> listeners = new ArrayList<CurrentSensorListener>();
    public void addListener(CurrentSensorListener toAdd) {
        listeners.add(toAdd);
    }

    public HumiditySensor(int id, String name, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, subaddress, shieldid, pin, enabled);
        type = "huniditysensor";
    }

    public void setCurrent(int humidity) {

        LOGGER.info("setHumidity");

        double oldHumidity = this.humidity;
        this.humidity = humidity;

        if (humidity != oldHumidity) {
            CurrentSensorDataLog dl = new CurrentSensorDataLog();
            dl.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (CurrentSensorListener hl : listeners)
                hl.changeHumidity(id, humidity);
        }
    }

    @Override
    public void writeDataLog(String event) {
        HumiditySensorDataLog dl = new HumiditySensorDataLog();
        dl.writelog(event, this);
    }

    public int getHumidity() {
        return humidity;
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            if (json.has("humidity"))
                setCurrent(json.getInt("humidity"));

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
            json.put("current", humidity);
            json.put("name", getName());
            json.put("lastupdate", getStrLastUpdate());
            json.put("type", type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
