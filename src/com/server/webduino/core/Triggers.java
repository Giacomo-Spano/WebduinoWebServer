package com.server.webduino.core;

import com.server.webduino.DBObject;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by giaco on 17/05/2017.
 */
public class Triggers extends DBObject {

    public List<Trigger> list = new ArrayList<>();

    public Triggers(/*List<Trigger> list*/) {
        /*for (Trigger trigger: list)
            this.list.add(trigger);*/
    }

    public Triggers(JSONObject json) throws Exception {
        fromJson(json);
    }

    @Override
    public void fromJson(JSONObject json) throws Exception {

        if (json.has("triggers")) {
            JSONArray jarray = json.getJSONArray("triggers");
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject jtrigger = jarray.optJSONObject(i);
                Trigger trigger = new Trigger(jtrigger);
                list.add(trigger);
            }
        }
    }

    public String getStatus() {
        return "--";
    }

    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();

        JSONArray jsonArray = new JSONArray();
        for (Trigger trigger : list) {
            jsonArray.put(trigger.toJson());
        }
        json.put("triggers", jsonArray);
        return json;
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM triggers";
        stmt.executeUpdate(sql);
    }


    public void write(Connection conn) throws SQLException {

        for (Trigger trigger : list) {
            trigger.write(conn);
        }
    }

    public void clear() {

        list.clear();
    }

    public void add(Trigger trigger) {
        list.add(trigger);
    }

    public Trigger getFromId(int id) {

        for (Trigger trigger : list) {
            if (trigger.id == id)
                return trigger;
        }
        return null;
    }
}
