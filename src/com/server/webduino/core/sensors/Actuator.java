package com.server.webduino.core.sensors;

import com.server.webduino.core.Core;
import com.server.webduino.core.httpClient;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public abstract class Actuator extends SensorBase {

    private static final Logger LOGGER = Logger.getLogger(Actuator.class.getName());

    public Actuator(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id,name,description,subaddress,shieldid,pin,enabled);
    }

    public abstract ActuatorCommand getCommandFromJson(JSONObject json);

    public abstract void writeDataLog(String event);

    /*interface ActuatorListener {
        void changeStatus(String newStatus, String oldStatus);
    }

    protected List<ActuatorListener> listeners = new ArrayList<ActuatorListener>();

    public void addListener(ActuatorListener toAdd) {
        listeners.add(toAdd);
    }*/


    public abstract Boolean sendCommand(ActuatorCommand cmd);

    @Override
    public void updateFromJson(Date date, JSONObject json) {
    }
}
