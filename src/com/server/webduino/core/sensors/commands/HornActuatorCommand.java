package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.datalog.HeaterCommandDataLog;
import com.server.webduino.core.datalog.HornCommandDataLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class HornActuatorCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(HornActuatorCommand.class.getName());
    public Command command;

    public static final String Command_Off = "off";
    public static final String Command_On = "on"; // "sendtemperature";

    public int actuatorid;
    public int duration;

    public HornActuatorCommand(int shieldid, int actuatorid){
        super(shieldid,actuatorid);
        commandDataLog = new HornCommandDataLog();
    }

    public HornActuatorCommand(JSONObject json) throws Exception {
        super(json);
        commandDataLog = new HornCommandDataLog();
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException {

            if (json.has("actuatorid"))
                actuatorid = json.getInt("actuatorid");
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();

        try {
            json.put("actuatorid", actuatorid);
            json.put("uuid",uuid);
            if (command.equals(HornActuatorCommand.Command_On)) {

                json.put("command", HornActuatorCommand.Command_On);
                json.put("duration", duration);
            } else if (command.equals(HornActuatorCommand.Command_Off)) {

                json.put("command", HornActuatorCommand.Command_Off);
                json.put("duration", duration);

            }
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
