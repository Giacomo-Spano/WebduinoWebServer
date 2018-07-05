package com.server.webduino.core.sensors;

import com.server.webduino.core.datalog.HornSensorDataLog;
import com.server.webduino.core.datalog.RFIDSensorDataLog;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import com.server.webduino.core.sensors.commands.HornActuatorCommand;
import com.server.webduino.core.sensors.commands.RFIDSensorCommand;
import com.server.webduino.core.webduinosystem.Status;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.logging.Logger;

//import static com.server.webduino.core.sensors.DoorSensor.DoorSensorListener.DoorEvents;

public class RFIDSensor extends Actuator {

    private static Logger LOGGER = Logger.getLogger(RFIDSensor.class.getName());

    private String card;
    Status activeStatus, notActiveStatus;


    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_NOTACTIVE = "notactive";

    public static final String STATUS_DESCRIPTION_ACTIVE = "Attivo";
    public static final String STATUS_DESCRIPTION_NOTACTIVE = "Non attivo";

    public RFIDSensor(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "rfidsensor";

        /*ActionCommand cmd = new ActionCommand("on","Attiva sirena");
        cmd.addDuration("Durata");
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    setStatus(activeStatus.status);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            @Override
            public void end() {

            }
        });
        actionCommandList.add(cmd);

        cmd = new ActionCommand("off","Disattiva sirena");
        cmd.addDuration("Durata");
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    setStatus(notActiveStatus.status);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            @Override
            public void end() {

            }
        });
        actionCommandList.add(cmd);*/

        command = new RFIDSensorCommand(shieldid,id);
        datalog = new RFIDSensorDataLog(id);
    }

    @Override
    protected void createStatusList() {
        super.createStatusList();

        activeStatus = new Status(STATUS_NOTACTIVE,STATUS_DESCRIPTION_NOTACTIVE);
        statusList.add(activeStatus);
        notActiveStatus = new Status(STATUS_ACTIVE,STATUS_DESCRIPTION_ACTIVE);
        statusList.add(notActiveStatus);
    }


    @Override
    public ActuatorCommand getCommandFromJson(JSONObject json) {
        return null;
    }

    /*public void setStatus(boolean open) {
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
    }*/

    @Override
    public void writeDataLog(String event) {
        datalog.writelog(event, this);
    }


    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            if (json.has("card")) {
                card = json.getString("card");
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
        try {
            json.put("card", card);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
