package com.server.webduino.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public abstract class Actuator extends SensorBase {

    private static final Logger LOGGER = Logger.getLogger(Actuator.class.getName());



    /*static final int relestatus_off = 0;
    static final int relestatus_on = 1;
    static final int relestatus_disabled = 2;
    static final int relestatus_enabled = 3;*/

    private String status = "";

    public Actuator() {
    }

    public void SetData(int id, int shieldid, String subaddress, String name, Date lastupdate) {
        super.setData(shieldid, subaddress, name, lastUpdate);

        this.id = id;
        //this.name = name;
        listeners = new ArrayList<ActuatorListener>();
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

        String oldStatus = this.status;
        this.status = status;

    }

    public String getStatus() {
        return status;
    }

    protected Boolean postCommand(String postParam, String path) {
            // questa per ora è usata solo dat heater actuator

        //String result = callPost(path, postParam);
        Result result = call("POST", path, postParam);
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
    void updateFromJson(Date date, JSONObject json) {
    }

    @Override
    public JSONObject getJson() {

        return null;
    }
}
