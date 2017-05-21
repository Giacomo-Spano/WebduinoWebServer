package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.Actuator;
import com.server.webduino.core.sensors.SensorBase;
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

    public JSONObject getJSONCommand(int actuatorId, String subaddress) {
        return null;
    }

    public boolean send(SensorBase actuator) {


        /*Core.postCommand(actuator.getShieldId(),)


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
        }*/
        return false;
    }
}


