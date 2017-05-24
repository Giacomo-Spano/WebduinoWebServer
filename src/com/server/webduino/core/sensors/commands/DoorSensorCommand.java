package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.Command;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class DoorSensorCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(DoorSensorCommand.class.getName());

    public static final String Command_Test = "test"; // "programoff";
    //public static final String Command_Off = "Off";
    //public static final String Command_SendTemperature = "sendtemperature"; // "sendtemperature";

    public int actuatorid;

    public String status;

    public DoorSensorCommand(JSONObject json) {
        super(json);
    }

    @Override
    public boolean fromJson(JSONObject json) {

        try {
            if (!json.has("actuatorid"))
                return false;
            actuatorid = json.getInt("actuatorid");

            if (!json.has("shieldid"))
                return false;
            shieldid = json.getInt("shieldid");

            if (json.has("command"))
                command = json.getString("command");
            if (json.has("status"))
                status = json.getString("status");
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
            json.put("uuid",uuid);
            json.put("actuatorid", actuatorid);
            if (command.equals(DoorSensorCommand.Command_Test)) {

                json.put("command", DoorSensorCommand.Command_Test);
                json.put("status", status);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
