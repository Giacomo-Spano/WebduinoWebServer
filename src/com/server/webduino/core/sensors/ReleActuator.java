package com.server.webduino.core.sensors;

import com.server.webduino.core.ReleActuatorCommand;
import com.server.webduino.core.datalog.CurrentSensorDataLog;
import com.server.webduino.core.datalog.ReleDataLog;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

public class ReleActuator extends Actuator /*implements TemperatureSensor.TemperatureSensorListener*/ {
    static final String STATUS_ON = "on";
    static final String STATUS_OFF = "off";

    private static final Logger LOGGER = Logger.getLogger(ReleActuator.class.getName());

    protected boolean on;

    public ReleActuator(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "releactuator";
        datalog = new ReleDataLog(id);
    }

    public boolean getReleStatus() {
        return on;
    }

    protected void setReleStatus(boolean on) {

        boolean oldReleStatus = this.on;
        this.on = on;

        /*if (on != oldReleStatus) {
            // Notify everybody that may be interested.
            for (ActuatorListener hl : listeners)
                hl.changedReleStatus(on, oldReleStatus);
        }*/
    }

    @Override
    public void writeDataLog(String event) {
        datalog.writelog(event, this);
    }

    public Boolean sendCommand(String command, boolean status) {

        ReleActuatorCommand cmd = new ReleActuatorCommand();
        cmd.command = command;
        cmd.on = on;
        return sendCommand(cmd);
    }

    @Override
    public ActuatorCommand getCommandFromJson(JSONObject json) {
        ReleActuatorCommand command = new ReleActuatorCommand();
        if (command.fromJson(json))
            return command;
        else
            return null;
    }

    @Override
    public Boolean sendCommand(ActuatorCommand cmd) {
        // sendcommand è usata anche da actuatorservlet per mandare i command dalle app
        ReleActuatorCommand releActuatorCommand = (ReleActuatorCommand) cmd;

        LOGGER.info("sendCommand " +
                "\ncommand = " + releActuatorCommand.command +
                "\non = " + releActuatorCommand.on);

        writeDataLog("Sending command" + releActuatorCommand.command);

        String postParam = "";
        String path = "";
        String strEvent = "event";

        LOGGER.info("sendCommand command=" + releActuatorCommand.command + ",on=" + releActuatorCommand.on);

        return releActuatorCommand.send(this);
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);

        boolean oldOn = this.on;

        try {
            LOGGER.info("received jsonResultSring=" + json.toString());

            if (json.has("on"))
                setReleStatus(json.getBoolean("on"));

        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("error");
        }

        if (on != oldOn) {
            // Notify everybody that may be interested.
            for (SensorListener hl : listeners) {
                ((ReleListener) hl).changeReleStatus(on, oldOn);
            }
        }

        writeDataLog("update");
        LOGGER.info("updateFromJson HeaterActuator old=" + oldOn + "new " + on);

    }


    @Override
    public void getJSONField(JSONObject json) {
        try {
            json.put("on", on);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    interface ReleListener extends SensorListener {
        void changeReleStatus(boolean newReleStatus, boolean oldReleStatus);
    }

}
