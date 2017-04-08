package com.server.webduino.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class ReleActuatorCommand extends ActuatorCommand {

    private static final Logger LOGGER = Logger.getLogger(ReleActuatorCommand.class.getName());

    public static final String Command_Program_ReleOff = "programoff"; // "programoff";
    public static final String Command_Program_ReleOn = "programon"; // "programon";
    public static final String Command_Send_Disabled = "disabled"; // "sendtemperature";
    public static final String Command_Send_Enabled = "enabled"; // "sendtemperature";
    public static final String Command_Manual_Off = "manualoff"; // "manualoff";
    public static final String Command_Manual_Auto = "manual"; // "manual";
    public static final String Command_Manual_End = "manualend"; // "endmanual";
    public static final String Command_Send_Temperature = "sendtemperature"; // "sendtemperature";

    public long duration;
    public double targetTemperature;
    public boolean remoteSensor;
    public int activeProgramID;
    public int activeTimeRangeID;
    public int activeSensorID;
    public double activeSensorTemperature;
    public boolean on;

    public static String getCommandName(String n) {

        switch (n) {
            case Command_Program_ReleOff:
                return "programoff";
            case Command_Program_ReleOn:
                return "programon";
            case Command_Send_Disabled:
                return "sendtemperature";
            case Command_Send_Enabled:
                return "sendtemperature";
            case Command_Manual_Off:
                return "manualoff";
            case Command_Manual_Auto:
                return "manualauto";
            case Command_Manual_End:
                return "endmanual";
            case Command_Send_Temperature:
                return "sendtemperature";
        }
        return "";
    }

    @Override
    public boolean fromJson(JSONObject json) {

        try {
            if (json.has("command"))
                command = json.getString("command");
            if (json.has("duration"))
                duration = json.getInt("duration");
            if (json.has("target"))
                targetTemperature = json.getDouble("target");
            if (json.has("sensorid"))
                activeSensorID = json.getInt("sensorid");
            if (json.has("remote"))
                remoteSensor = json.getBoolean("remote");

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            return false;
        }
    }

    @Override
    public JSONObject getJSONCommand(int actuatorId) {

        JSONObject json = new JSONObject();

        try {
            json.put("actuatorid", actuatorId);
            if (command.equals(ReleActuatorCommand.Command_Program_ReleOn)) {

                json.put("command", ReleActuatorCommand.Command_Program_ReleOn);
                json.put("duration", duration);
                json.put("target", targetTemperature);
                json.put("localsensor", !remoteSensor);
                json.put("sensor", activeSensorID);
                json.put("program", activeProgramID);
                json.put("timerange", activeTimeRangeID);
                json.put("temperature", activeSensorTemperature);

            } else if (command.equals(ReleActuatorCommand.Command_Program_ReleOff)) {

                json.put("command", ReleActuatorCommand.Command_Program_ReleOff);
                json.put("duration", duration);
                json.put("target", targetTemperature);
                json.put("localsensor", !remoteSensor);
                json.put("sensor", activeSensorID);
                json.put("program", activeProgramID);
                json.put("timerange", activeTimeRangeID);
                json.put("temperature", activeSensorTemperature);

            } else if (command.equals(ReleActuatorCommand.Command_Manual_Auto)) {

                json.put("command", ReleActuatorCommand.Command_Manual_Auto);
                json.put("duration", duration);
                json.put("target", targetTemperature);
                json.put("localsensor", !remoteSensor);
                json.put("sensor",activeSensorID);
                //json.put("program",activeProgramID);
                //json.put("timerange",activeTimeRangeID);
                TemperatureSensor tempSensor = (TemperatureSensor) Core.getSensorFromId(activeSensorID);
                if (tempSensor != null) {
                    activeSensorTemperature = tempSensor.getTemperature();
                }
                json.put("temperature", activeSensorTemperature);

            } else if (command.equals(ReleActuatorCommand.Command_Manual_Off)) {

                json.put("command", ReleActuatorCommand.Command_Manual_Off);
                json.put("duration", duration);
            /*json.put("target",targetTemperature);
            json.put("localsensor",!remoteSensor);
            json.put("sensor",activeSensorID);
            json.put("program",activeProgramID);
            json.put("timerange",activeTimeRangeID);
            json.put("temperature",activeSensorTemperature);*/

            } else if (command.equals(ReleActuatorCommand.Command_Manual_End)) {

                json.put("command", ReleActuatorCommand.Command_Manual_End);
                json.put("duration", duration);
                //json.put("target",targetTemperature);
                //json.put("localsensor",!remoteSensor);
                //json.put("sensor",activeSensorID);
                //json.put("program",activeProgramID);
                //json.put("timerange",activeTimeRangeID);
                json.put("temperature", activeSensorTemperature);

            } else if (command.equals(ReleActuatorCommand.Command_Send_Temperature)) {

                json.put("command", ReleActuatorCommand.Command_Send_Temperature);
                json.put("duration", duration);
                //json.put("target",targetTemperature);
                json.put("localsensor", !remoteSensor);
                json.put("sensor", activeSensorID);
                //json.put("program",activeProgramID);
                //json.put("timerange",activeTimeRangeID);
                json.put("temperature", activeSensorTemperature);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
