package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.IRCode;
import com.server.webduino.core.IRCodeSequence;
import com.server.webduino.core.datalog.HornCommandDataLog;
import com.server.webduino.core.datalog.IRCommandDataLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class IRActuatorCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(IRActuatorCommand.class.getName());
    public String command;
    JSONArray codes;
    private String codetype, code;
    int bit;

    public static final String Command_send = "send";

    public IRActuatorCommand(String command, int shieldid, int actuatorid, IRCodeSequence sequence) {
        super(command, shieldid, actuatorid);
        commandDataLog = new IRCommandDataLog();
        this.command = command;
        codes = new JSONArray();
        for (int i = 0; i < sequence.sequence.size(); i++) {
            codes.put(sequence.sequence.get(i).toJson());
        }

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
        if (json.has("codes"))
            codes = json.getJSONArray("codes");
        else
            throw new Exception("codes not found");
    }

    @Override
    public JSONObject toJSON() {

        JSONObject json = new JSONObject();

        try {
            json.put("actuatorid", actuatorid);
            json.put("uuid", uuid);
            if (command.equals(IRActuatorCommand.Command_send)) {
                json.put("command", IRActuatorCommand.Command_send);
                json.put("codes",codes);
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
