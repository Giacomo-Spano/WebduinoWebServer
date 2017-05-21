package com.server.webduino.core;

import com.server.webduino.core.sensors.commands.ActuatorCommand;
import com.server.webduino.core.sensors.TemperatureSensor;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class HeaterActuatorCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(HeaterActuatorCommand.class.getName());

    public static final String Command_KeepTemperature = "keepTemperature"; // "programoff";
    public static final String Command_Off = "Off";
    public static final String Command_SendTemperature = "sendtemperature"; // "sendtemperature";

    public int actuatorid;
    public long duration;
    public double targetTemperature;
    public int scenario;
    public int timeInterval;
    public int zone;
    public double temperature;

    public HeaterActuatorCommand(JSONObject json) {
        super(json);
    }

    @Override
    public boolean fromJson(JSONObject json) {

        try {
            if (!json.has("actuatorid"))
                return false;
            actuatorid = json.getInt("actuatorid");

            if (json.has("command"))
                command = json.getString("command");
            if (json.has("duration"))
                duration = json.getInt("duration");
            if (json.has("target"))
                targetTemperature = json.getDouble("target");
            if (json.has("scenario"))
                scenario = json.getInt("scenario");
            if (json.has("timeinterval"))
                timeInterval = json.getInt("timeinterval");
            if (json.has("temperature"))
                temperature = json.getDouble("temperature");

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            return false;
        }
    }

    @Override
    public JSONObject getJSON() {

        JSONObject json = new JSONObject();

        try {
            json.put("actuatorid", actuatorid);
            //json.put("actuatorid", actuatorId);
            if (command.equals(HeaterActuatorCommand.Command_KeepTemperature)) {

                json.put("command", HeaterActuatorCommand.Command_KeepTemperature);
                json.put("duration", duration);
                json.put("target", targetTemperature);
                json.put("scenario", scenario);
                json.put("timeinterval", timeInterval);
                json.put("zone", zone);
                json.put("temperature", temperature);

            } else if (command.equals(HeaterActuatorCommand.Command_Off)) {

                json.put("command", HeaterActuatorCommand.Command_Off);
                json.put("duration", duration);
                json.put("scenario", scenario);
                json.put("timeinterval", timeInterval);
                json.put("zone", zone);

            } else if (command.equals(HeaterActuatorCommand.Command_SendTemperature)) {

                json.put("command", HeaterActuatorCommand.Command_SendTemperature);
                json.put("temperature", temperature);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
