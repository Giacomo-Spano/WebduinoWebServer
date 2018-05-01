package com.server.webduino.core.sensors;

import com.server.webduino.core.Core;
import com.server.webduino.core.datalog.CommandDataLog;
import com.server.webduino.core.httpClient;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import com.server.webduino.core.sensors.commands.Command;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public abstract class Actuator extends SensorBase {

    private static final Logger LOGGER = Logger.getLogger(Actuator.class.getName());
    public Command command;

    public Actuator(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id,name,description,subaddress,shieldid,pin,enabled);
    }

    public abstract ActuatorCommand getCommandFromJson(JSONObject json);

    public abstract void writeDataLog(String event);

    @Override
    public void updateFromJson(Date date, JSONObject json) {
        super.updateFromJson(date,json);
    }
}
