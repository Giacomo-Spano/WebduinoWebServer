package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.datalog.HornCommandDataLog;
import com.server.webduino.core.datalog.IRCommandDataLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class IRActuatorCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(IRActuatorCommand.class.getName());
    public Command command;
    private String code;

    public static final String Command_send = "send";

    public IRActuatorCommand(int shieldid, int actuatorid){
        super(shieldid,actuatorid);
        commandDataLog = new IRCommandDataLog();
    }

    public IRActuatorCommand(JSONObject json) throws JSONException {
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
            if (command.equals(IRActuatorCommand.Command_send)) {

                json.put("command", IRActuatorCommand.Command_send);
                json.put("code", code);
            } /*else if (command.equals(IRActuatorCommand.Command_Off)) {

                json.put("command", IRActuatorCommand.Command_Off);
                json.put("duration", duration);

            }*/
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
