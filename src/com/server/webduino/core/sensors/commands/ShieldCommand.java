package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.datalog.CommandDataLog;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class ShieldCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(ShieldCommand.class.getName());
    //public String command;
    //public int shieldId;
    public JSONArray sensors;

    public ShieldCommand(JSONObject json) throws JSONException {
        super(json);
        commandDataLog = new CommandDataLog("heatercommanddatalog");
    }

    public void fromJson(JSONObject json) throws JSONException {
        LOGGER.info("updateFromJson json=" + json.toString());
        if (json.has("command"))
            command = json.getString("command");
        if (json.has("shieldid"))
            shieldid = json.getInt("shieldid");
        if (json.has("sensors"))
            sensors = json.getJSONArray("sensors");

    }

    public JSONObject getJSON() {

        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("command", command);
            json.put("shieldid", shieldid);
            json.put("sensors", sensors);
            return json;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}


