package com.server.webduino.core.webduinosystem.services;

import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.Shield;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.TemperatureSensor;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class Service {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    public int id;
    private String name;
    private String type;
    private String status = "idle";

    protected List<String> statusList = new ArrayList<String>();
    protected List<ActionCommand> actionCommandList = new ArrayList<ActionCommand>();

    public Service(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void init() {

    }

    public JSONArray getStatusListJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (String status: statusList) {
            jsonArray.put(status);
        }
        return jsonArray;
    }

    public JSONArray getActionCommandListJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (ActionCommand command: actionCommandList) {
            jsonArray.put(command.toJson());
        }
        return jsonArray;
    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("status", status);
            json.put("statuslist", getStatusListJSONArray());
            json.put("actioncommandlist", getActionCommandListJSONArray());

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        // get custom json field
        getJSONField(json);

        return json;
    }
    public void getJSONField(JSONObject json) {

    }
}
