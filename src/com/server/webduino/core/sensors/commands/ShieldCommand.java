package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.Core;
import com.server.webduino.core.Shield;
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
    public static final String Command_CheckHealth = "checkhealth";

    public ShieldCommand(JSONObject json) throws JSONException {
        super(json);
        commandDataLog = new CommandDataLog("commanddatalog");
    }

    public void fromJson(JSONObject json) throws JSONException {
        LOGGER.info("updateFromJson json=" + json.toString());
        if (json.has("command"))
            command = json.getString("command");
        if (json.has("shieldid"))
            shieldid = json.getInt("shieldid");
    }

    @Override
    public boolean processResponseReceived(String message) {
        try {
            if (command.equals(Command_CheckHealth)) {
                JSONObject json = new JSONObject(message);
                if (json.has("MAC")) {
                    String macaddress = json.getString("MAC");
                    Shield shield = Core.getShieldFromMACAddress(macaddress);
                    if (shield != null) {
                        return shield.updateShieldStatus(json);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }

    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        try {
            json.put("uuid", uuid);
            json.put("command", command);
            json.put("shieldid", shieldid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}


