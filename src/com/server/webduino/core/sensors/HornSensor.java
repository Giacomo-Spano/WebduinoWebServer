package com.server.webduino.core.sensors;

import com.server.webduino.core.datalog.DoorSensorDataLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.logging.Logger;

//import static com.server.webduino.core.sensors.DoorSensor.DoorSensorListener.DoorEvents;

public class HornSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(HornSensor.class.getName());

    private boolean alarmActive;

    public HornSensor(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "hornsensor";
        datalog = new DoorSensorDataLog(id);
    }

    public void setStatus(boolean open) {
        LOGGER.info("setStatus");

        boolean oldAlarmActive = this.alarmActive;
        this.alarmActive = open;
        if (open != oldAlarmActive) {
            datalog.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (SensorListener listener : listeners) {
                listener.changeDoorStatus(id, open, oldAlarmActive);
            }
        }
    }

    @Override
    public void writeDataLog(String event) {
        datalog.writelog(event, this);
    }

    public boolean getAlarmActiveStatus() {
        return alarmActive;
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            if (json.has("alarmActive"))
                setStatus(json.getBoolean("alarmActive"));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }
    }

    @Override
    public void getJSONField(JSONObject json) {
        try {
            json.put("openstatus", alarmActive);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
