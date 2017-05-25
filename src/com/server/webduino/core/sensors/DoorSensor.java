package com.server.webduino.core.sensors;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.logging.Logger;

//import static com.server.webduino.core.sensors.DoorSensor.DoorSensorListener.DoorEvents;

public class DoorSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(DoorSensor.class.getName());

    private boolean open;

    /*public interface DoorSensorListener extends SensorBase.SensorListener {
        public String DoorEvents = "door event";
        void changeDoorStatus(int sensorId, boolean open);
    }**/

    /*public boolean sendEvent(String eventtype) {
        if (super.sendEvent(eventtype) || eventtype == DoorEvents)
            return true;
        return false;
    }*/

    public DoorSensor(int id, String name, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, subaddress, shieldid, pin, enabled);
        type = "doorsensor";
    }

    public void setStatus(boolean open) {
        LOGGER.info("setStatus");

        boolean oldOpen = this.open;
        this.open = open;
        if (open != oldOpen) {
            DoorSensorDataLog dl = new DoorSensorDataLog();
            dl.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (SensorListener listener : listeners) {
                listener.changeDoorStatus(id, open, oldOpen);
            }
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
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            if (json.has("open"))
                setStatus(json.getBoolean("open"));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }
    }

    @Override
    public void getJSONField() {
        try {
            json.put("status", open);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
