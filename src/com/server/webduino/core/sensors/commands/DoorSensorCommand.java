package com.server.webduino.core.sensors.commands;

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


    public String status;

    public DoorSensorCommand(String command, int shieldid, int actuatorid, String status) {
        super(command, shieldid, actuatorid);
        this.status = status;
    }

    public DoorSensorCommand(JSONObject json) throws JSONException {
        super(json);
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException {

        if (json.has("actuatorid"))
            actuatorid = json.getInt("actuatorid");
        if (json.has("shieldid"))
            shieldid = json.getInt("shieldid");
        if (json.has("command"))
            command = json.getString("command");
        if (json.has("status"))
            status = json.getString("status");
    }

    @Override
    public JSONObject getJSON() {

        JSONObject json = new JSONObject();

        try {
            json.put("uuid", uuid);
            json.put("actuatorid", actuatorid);
            json.put("command", command);
            /*if (command.equals(DoorSensorCommand.Command_Test)) {

                json.put("command", DoorSensorCommand.Command_Test);
                json.put("status", status);
            }*/
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
