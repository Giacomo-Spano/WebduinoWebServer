package com.server.webduino.core.sensors;

import com.server.webduino.core.datalog.DoorSensorDataLog;
import com.server.webduino.core.webduinosystem.Status;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.logging.Logger;

//import static com.server.webduino.core.sensors.DoorSensor.DoorSensorListener.DoorEvents;

public class DoorSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(DoorSensor.class.getName());

    //private boolean open;
    public static final String STATUS_OPEN = "open";
    public static final String STATUS_CLOSED = "closed";

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

    /*public void setStatus(boolean open) {
        LOGGER.info("setStatus");

        boolean oldOpen = this.open;
        this.open = open;
        if (open != oldOpen) {
            datalog.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (SensorListener listener : listeners) {
                listener.changeDoorStatus(id, open, oldOpen);
            }
        }
    }*/

    @Override
    public void writeDataLog(String event) {
        datalog.writelog(event, this);
    }

    /*public String getDoorStatus() {
        return status;
    }*/

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {

            if (json.has("open")) {
                if (json.getBoolean("open"))
                    setStatus(STATUS_OPEN);
                else
                    setStatus(STATUS_CLOSED);
            }

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
