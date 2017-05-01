package com.server.webduino.core;

import com.server.webduino.core.sensors.Actuator;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class Command {

    private static final Logger LOGGER = Logger.getLogger(Command.class.getName());
    public String command;
    public int shieldId;

    public Command(JSONObject json) {
        fromJson(json);
    }

    public boolean fromJson(JSONObject json) {
        return false;
    }

    public JSONObject getJSON() {
        return null;
    }

    /*public boolean send(Actuator actuator) {

        JSONObject json = getJSONCommand(actuator.getId(), actuator.getSubaddress());
        if (json != null) {

            String path = "/command";
            boolean res = actuator.postCommand(json.toString(), path);

            if (res) {
                //writeDataLog(strEvent + " sent");
                LOGGER.info("Command=" + command + " sent");
            } else {
                //writeDataLog(strEvent + " FAILED");
                LOGGER.info("Command=" + command + " failed");
            }
            return res;
        }
        return false;
    }*/
}


