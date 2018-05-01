package com.server.webduino.core.sensors;

import com.server.webduino.core.datalog.DoorSensorDataLog;
import com.server.webduino.core.datalog.HornSensorDataLog;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.sensors.commands.HornActuatorCommand;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.logging.Logger;

//import static com.server.webduino.core.sensors.DoorSensor.DoorSensorListener.DoorEvents;

public class HornSensor extends Actuator {

    private static Logger LOGGER = Logger.getLogger(HornSensor.class.getName());

    private boolean alarmActive;

    public HornSensor(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "hornsensor";

        ActionCommand cmd = new ActionCommand("on","Attiva sirena");
        cmd.addDuration("Durata");
        actionCommandList.add(cmd);

        cmd = new ActionCommand("off","Disattiva sirena");
        cmd.addDuration("Durata");
        actionCommandList.add(cmd);

        command = new HornActuatorCommand(shieldid,id);
        datalog = new HornSensorDataLog(id);
    }

    @Override
    public ActuatorCommand getCommandFromJson(JSONObject json) {
        return null;
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

    /*@Override
    public Boolean sendCommand(ActuatorCommand cmd) {
        return null;
    }*/

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
