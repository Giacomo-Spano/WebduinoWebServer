package com.server.webduino.core.sensors;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class PressureSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(PressureSensor.class.getName());

    private double pressure;

    public interface PressureSensorListener {
        void changePressure(int sensorId, double current);
    }

    private List<PressureSensorListener> listeners = new ArrayList<PressureSensorListener>();
    public void addListener(PressureSensorListener toAdd) {
        listeners.add(toAdd);
    }

    public PressureSensor(int id, String name, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, subaddress, shieldid, pin, enabled);
        type = "pressuresensor";
    }

    public void setPressure(double pressure) {

        LOGGER.info("setCurrent");

        double oldPressure = this.pressure;
        this.pressure = pressure;

        if (pressure != oldPressure) {
            CurrentSensorDataLog dl = new CurrentSensorDataLog();
            dl.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (PressureSensorListener hl : listeners)
                hl.changePressure(id, pressure);
        }
    }

    @Override
    public void writeDataLog(String event) {
        PressureSensorDataLog dl = new PressureSensorDataLog();
        dl.writelog(event, this);
    }

    public double getPressure() {
        return pressure;
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            if (json.has("pressure"))
                setPressure(json.getDouble("pressure"));

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
            json.put("current", pressure);
            json.put("name", getName());
            //json.put("lastupdate", getStrLastUpdate());
            json.put("type", type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
