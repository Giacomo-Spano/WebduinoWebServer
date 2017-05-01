package com.server.webduino.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class ShieldCommand extends Command {

    private static final Logger LOGGER = Logger.getLogger(ShieldCommand.class.getName());
    public String command;
    public int shieldId;
    public JSONArray sensors;

    public ShieldCommand(JSONObject json) {
        super(json);
    }

    public boolean fromJson(JSONObject json) {
        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            if (json.has("command"))
                command = json.getString("command");
            if (json.has("shieldid"))
                shieldId = json.getInt("shieldid");
            if (json.has("sensors"))
                sensors = json.getJSONArray("sensors");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            return false;
        }
        return true;
    }

    public JSONObject getJSON() {

        JSONObject json = new JSONObject();
        try {
            json.put("command", command);
            json.put("shieldid", shieldId);
            json.put("sensors", sensors);
            return json;

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}


