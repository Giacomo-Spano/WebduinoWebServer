package com.server.webduino.core.sensors;

import com.server.webduino.core.Core;
import com.server.webduino.core.datalog.HornSensorDataLog;
import com.server.webduino.core.datalog.IRSensorDataLog;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import com.server.webduino.core.sensors.commands.HornActuatorCommand;
import com.server.webduino.core.sensors.commands.IRActuatorCommand;
import com.server.webduino.core.sensors.commands.SensorCommand;
import com.server.webduino.core.webduinosystem.Status;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.servlet.SendPushMessages;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.logging.Logger;

import static com.server.webduino.core.webduinosystem.Status.STATUS_OFFLINE;

//import static com.server.webduino.core.sensors.DoorSensor.DoorSensorListener.DoorEvents;

public class IRSensor extends Actuator {

    private static Logger LOGGER = Logger.getLogger(IRSensor.class.getName());

    //public static final String STATUS_ACTIVE = "active";
    //public static final String STATUS_NOTACTIVE = "notactive";

    //public static final String STATUS_DESCRIPTION_ACTIVE = "Attivo";
    //public static final String STATUS_DESCRIPTION_NOTACTIVE = "Non attivo";

    public IRSensor(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "irsensor";

        ActionCommand cmd = new ActionCommand("send","Invia codice IR");
        cmd.addParam("Codice",10);
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {

                boolean res = false;
                try {
                    IRActuatorCommand command = new IRActuatorCommand(json);
                    res = command.send();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!res && !getStatus().status.equals(STATUS_OFFLINE)) {
                    setStatus(STATUS_OFFLINE);
                    String description = "Sensor " + name + " offline";
                    //Core.sendPushNotification(SendPushMessages.notification_offline, "Offline", description, "0", 0);
                }
                return res;
            }
            @Override
            public void end() {

            }

            @Override
            public JSONObject getResult() {
                return null;
            }
        });
        actionCommandList.add(cmd);


        //command = new IRActuatorCommand(shieldid,id);
        datalog = new IRSensorDataLog(id);
    }

    @Override
    protected void createStatusList() {
        super.createStatusList();

        /*activeStatus = new Status(STATUS_NOTACTIVE,STATUS_DESCRIPTION_NOTACTIVE);
        statusList.add(activeStatus);
        notActiveStatus = new Status(STATUS_ACTIVE,STATUS_DESCRIPTION_ACTIVE);
        statusList.add(notActiveStatus);*/
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
        /*try {
            if (json.has("alarmActive")) {
                if (json.getBoolean("alarmActive"))
                    setStatus(STATUS_ACTIVE);
                else
                    setStatus(STATUS_NOTACTIVE);
            }

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }*/
    }

    @Override
    public void getJSONField(JSONObject json) {
        /*try {
            json.put("openstatus", alarmActive);

        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }
}
