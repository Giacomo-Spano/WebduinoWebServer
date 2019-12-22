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

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {


            JSONObject jsonstatus = new JSONObject();
            try {
                jsonstatus.put("sensorid", id);
                jsonstatus.put("shieldid", shieldid);
                jsonstatus.put("name", name);
                jsonstatus.put("description", description);
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                jsonstatus.put("date", df.format(Core.getDate()));
                jsonstatus.put("status", getStatus());
                //jsonstatus.put("valuetextxx", valuetext);
                //jsonstatus.put("valuetype", valuetype);
                //jsonstatus.put("valueunit", valueunit);
                jsonstatus.put("lastUpdate", lastUpdate);
                jsonstatus.put("type", type);
                //json.put("zoneid", zoneId);
                //json.put("zonesensorid", remoteSensorId);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }

            String message = jsonstatus.toString();
            updateHomeAssistant("homeassistant/sensor/"+ id , message);

            updateHomeAssistant("homeassistant/sensor/"+ id , "/availability/online");

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
