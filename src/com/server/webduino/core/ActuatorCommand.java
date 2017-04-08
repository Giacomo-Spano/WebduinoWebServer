package com.server.webduino.core;

import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class ActuatorCommand {

    private static final Logger LOGGER = Logger.getLogger(ActuatorCommand.class.getName());
    public String command;

    public boolean fromJson(JSONObject json) {
        return false;
    }

    public JSONObject getJSONCommand(int actuatorId) {
        return null;
    }

    public boolean send(Actuator actuator) {

        JSONObject json = getJSONCommand(actuator.id);
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
    }
}


