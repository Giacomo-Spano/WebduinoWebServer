package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.datalog.CommandDataLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class SensorCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(SensorCommand.class.getName());

    public static final String Command_RequestSensorStatusUpdate = "requestsensorstatus"; // "programoff";
    public static final String Command_Off = "off";

    public SensorCommand(JSONObject json) throws JSONException {
        super(json);
        commandDataLog = new CommandDataLog("commanddatalog");
    }

    public SensorCommand(String command, int shieldid, int actuatorid) {
        super(command,shieldid,actuatorid);

        commandDataLog = new CommandDataLog("commanddatalog");
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException {

            if (json.has("actuatorid"))
                actuatorid = json.getInt("actuatorid");
            if (json.has("shieldid"))
                shieldid = json.getInt("shieldid");
            if (json.has("command"))
                command = json.getString("command");

    }

    @Override
    public JSONObject getJSON() {

        JSONObject json = new JSONObject();

        try {
            json.put("actuatorid", actuatorid);
            json.put("uuid",uuid);
            if (command.equals(SensorCommand.Command_RequestSensorStatusUpdate)) {



                json.put("command", SensorCommand.Command_RequestSensorStatusUpdate);

            } else if (command.equals(SensorCommand.Command_Off)) {

                json.put("command", SensorCommand.Command_Off);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
