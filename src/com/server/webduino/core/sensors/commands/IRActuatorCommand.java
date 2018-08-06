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
    public String command;
    private String codetype, code;
    int bit;

    public static final String Command_send = "send";

    public IRActuatorCommand(int shieldid, int actuatorid) {
        super(shieldid, actuatorid);
        commandDataLog = new IRCommandDataLog();
    }

    public IRActuatorCommand(JSONObject json) throws Exception {
        super(json);
        commandDataLog = new IRCommandDataLog();
    }

    @Override
    public void fromJson(JSONObject json) throws Exception {

        if (json.has("command"))
            command = json.getString("command");
        else
            throw new Exception("command not found");
        if (json.has("actuatorid"))
            actuatorid = json.getInt("actuatorid");
        else
            throw new Exception("actuatorid not found");
        if (json.has("shieldid"))
            shieldid = json.getInt("shieldid");
        else
            throw new Exception("shieldid not found");
        if (json.has("codetype"))
            codetype = json.getString("codetype");
        if (json.has("code"))
            code = json.getString("code");
        if (json.has("bit"))
            bit = json.getInt("bit");
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();

        try {
            json.put("actuatorid", actuatorid);
            json.put("uuid", uuid);
            if (command.equals(IRActuatorCommand.Command_send)) {
                json.put("command", IRActuatorCommand.Command_send);
                json.put("codetype", codetype);
                json.put("code", code);
                json.put("bit", bit);
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
