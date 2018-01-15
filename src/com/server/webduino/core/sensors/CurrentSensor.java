package com.server.webduino.core.sensors;

import com.server.webduino.core.datalog.CurrentSensorDataLog;
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

    public CurrentSensor(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "currentsensor";
        datalog = new CurrentSensorDataLog(id);
    }

    public void setCurrent(double current) {

        LOGGER.info("setCurrent");

        double oldCurrent = this.current;
        this.current = current;

        if (current != oldCurrent) {
            datalog.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (CurrentSensorListener hl : listeners)
                hl.changeCurrent(id, current);
        }
    }

    @Override
    public void writeDataLog(String event) {
        datalog.writelog(event, this);
    }

    public double getCurrent() {
        return current;
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            if (json.has("current"))
                setCurrent(json.getDouble("current"));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }
    }


    @Override
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", getId());
            json.put("shieldid", shieldid);
            json.put("online", online);
            json.put("subaddress", subaddress);
            json.put("current", getCurrent());
            json.put("name", getName());
            //json.put("lastupdate", getStrLastUpdate());
            json.put("type", type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public void getJSONField(JSONObject json) {

    }
}
