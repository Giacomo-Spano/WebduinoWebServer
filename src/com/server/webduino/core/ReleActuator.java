package com.server.webduino.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Logger;

public class ReleActuator extends Actuator /*implements TemperatureSensor.TemperatureSensorListener*/ {
    static final String STATUS_ON = "on";
    static final String STATUS_OFF = "off";

    private static final Logger LOGGER = Logger.getLogger(ReleActuator.class.getName());

    protected boolean on;

    public ReleActuator() {
        super();
        type = "releactuator";
        statusUpdatePath = "/relestatus";
    }

    @Override
    public void addListener(ActuatorListener toAdd) {
        listeners.add((HeaterActuatorListener) toAdd);
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
                hl.changeReleStatus(on, oldReleStatus);
        }*/
    }

    @Override
    public void writeDataLog(String event) {
        HeaterDataLog dl = new HeaterDataLog();
        dl.writelog(event, this);
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

        boolean oldOn = this.on;

        lastUpdate = date;
        online = true;
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
            for (ActuatorListener hl : listeners) {
                ((HeaterActuatorListener) hl).changeReleStatus(on, oldOn);
            }
        }

        writeDataLog("update");
        LOGGER.info("updateFromJson HeaterActuator old=" + oldOn + "new " + on);

    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("on", on);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            if (lastUpdate != null)
                json.put("lastupdate", df.format(lastUpdate));
            json.put("shieldid", shieldid);
            json.put("online", online);
            json.put("type", type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    interface HeaterActuatorListener extends ActuatorListener {
        void changeStatus(String newStatus, String oldStatus);

        void changeReleStatus(boolean newReleStatus, boolean oldReleStatus);
    }

}
