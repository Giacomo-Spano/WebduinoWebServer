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

    private String status = "";
    protected String oldStatus = "";

    public Actuator(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id,name,description,subaddress,shieldid,pin,enabled);
    }

    public abstract ActuatorCommand getCommandFromJson(JSONObject json);

    public abstract void writeDataLog(String event);

    interface ActuatorListener {
        void changeStatus(String newStatus, String oldStatus);
    }

    protected List<ActuatorListener> listeners = new ArrayList<ActuatorListener>();

    public void addListener(ActuatorListener toAdd) {
        listeners.add(toAdd);
    }

    public void setStatus(String status) {
        oldStatus = this.status;
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public Boolean postCommand(String postParam, String path) {
            // questa per ora è usata solo dat heater actuator

        //String result = callPost(path, postParam);
        httpClient.Result result = call("POST", path, postParam);
        if (result.res == false) {
            for (int i = 0; i < 2; i++) {

                LOGGER.info("retry..." + i);
                //result = callPost(path, postParam);
                result = call("POST", path, postParam);
                if (result != null)
                    break;
            }
        }
        if (result.res == true) {
            //requestStatusUpdate();
            try {
                Date date = Core.getDate();
                JSONObject json = new JSONObject(result.response);
                //writeDataLog("command response rec");
                updateFromJson(date,json);
            } catch (JSONException e) {
                e.printStackTrace();
                LOGGER.severe("json error ");
                //writeDataLog("command response json error");
            }
            LOGGER.info("command sent");
            return true;
        } else {
            LOGGER.severe("command FAILED");
            this.online = false;
            writeDataLog("command FAILED");
            return false;
        }
    }

    public abstract Boolean sendCommand(ActuatorCommand cmd);

    @Override
    public void updateFromJson(Date date, JSONObject json) {
    }

    /*@Override
    public JSONObject toJson() {

        return null;
    }*/
}
