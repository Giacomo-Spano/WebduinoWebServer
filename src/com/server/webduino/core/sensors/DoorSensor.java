package com.server.webduino.core.sensors;

import com.server.webduino.core.Core;
import com.server.webduino.core.datalog.DoorSensorDataLog;
import com.server.webduino.core.webduinosystem.Status;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

//import static com.server.webduino.core.sensors.DoorSensor.DoorSensorListener.DoorEvents;

public class DoorSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(DoorSensor.class.getName());

    //private boolean open;
    public static final String STATUS_OPEN = "dooropen";
    public static final String STATUS_CLOSED = "doorclosed";

    public static final String STATUS_DESCRIPTION_OPEN = "Porta aperta";
    public static final String STATUS_DESCRIPTION_CLOSED = "Porta chiusa";

    public DoorSensor(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "doorsensor";
        datalog = new DoorSensorDataLog(id);
    }

    @Override
    protected void createStatusList() {
        super.createStatusList();

        Status status = new Status(STATUS_OPEN,STATUS_DESCRIPTION_OPEN);
        statusList.add(status);
        status = new Status(STATUS_CLOSED,STATUS_DESCRIPTION_CLOSED);
        statusList.add(status);
    }

    @Override
    public void writeDataLog(String event) {
        datalog.writelog(event, this);
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date, json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {

            String message;
            if (getStatus().status.equals(STATUS_OPEN))
                message = "dooropen";
            else
                message = "doorclosed";
            Core.updateHomeAssistant("homeassistant/sensor/" + id + "/status", message);

            JSONObject jsonattributes = new JSONObject();
            try {
                jsonattributes.put("sensorid", id);
                jsonattributes.put("shieldid", shieldid);
                jsonattributes.put("name", name);
                jsonattributes.put("description", description);
                jsonattributes.put("date", Core.getDate());
                jsonattributes.put("status", getStatus().status);
                jsonattributes.put("lastUpdate", lastUpdate);
                jsonattributes.put("type", type);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String attr_message = "{\"Attributes\":" + jsonattributes.toString() + "}";
            Core.updateHomeAssistant("homeassistant/sensor/" + id + "/attributes", attr_message);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }
    }

    @Override
    public void getJSONField(JSONObject json) {
        /*try {
            //json.put("openstatus", open);

        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }
}
