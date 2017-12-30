package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.Core;
import com.server.webduino.core.Trigger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//import java.Time;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class ScenarioTrigger implements Trigger.TriggerListener {

    public int id;
    public int scenarioid;
    public int triggerid;
    public String type;
    public String name;
    public String description;
    public boolean enabled;
    //public boolean status;
    public int priority;

    public boolean active = false;

    @Override
    public void onChangeStatus(boolean status) {
        for (ScenarioTriggerListener listener: listeners) {
            listener.onChangeStatus(status);
        }
    }

    public boolean getStatus() {
        Trigger trigger = Core.getTriggerFromId(triggerid);
        if (trigger != null)
            return trigger.status;
        return false;
    }

    public interface ScenarioTriggerListener {
        void onChangeStatus(boolean active);
    }

    protected List<ScenarioTriggerListener> listeners = new ArrayList<ScenarioTriggerListener>();

    public void addListener(ScenarioTriggerListener toAdd) {
        listeners.add(toAdd);
    }

    public void deleteListener(ScenarioTriggerListener toRemove) {
        listeners.remove(toRemove);
    }

    public ScenarioTrigger() {
    }

    public ScenarioTrigger(ResultSet resultset) throws SQLException {
        fromResultSet(resultset);
    }

    public ScenarioTrigger(JSONObject json) throws JSONException {
        fromJson(json);
    }

    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");

        json.put("id", id);
        json.put("triggerid", triggerid);
        json.put("scenarioid", scenarioid);
        //json.put("name", name);
        Trigger trigger = Core.getTriggerFromId(triggerid);
        if (trigger != null)
            json.put("name", trigger.name);

        json.put("description", description);
        json.put("type", type);
        json.put("enabled", enabled);
        //json.put("status", status);
        json.put("status", trigger.status);
        json.put("priority", priority);

        return json;
    }

    public void fromJson(JSONObject json) throws JSONException {

        if (json.has("id")) id = json.getInt("id");
        if (json.has("scenarioid")) scenarioid = json.getInt("scenarioid");
        if (json.has("triggerid")) triggerid = json.getInt("triggerid");
        if (json.has("name")) name = json.getString("name");
        if (json.has("description")) description = json.getString("description");
        if (json.has("type")) type = json.getString("type");
        if (json.has("enabled")) enabled = json.getBoolean("enabled");
        //if (json.has("status")) status = json.getBoolean("status");
        if (json.has("priority")) priority = json.getInt("priority");
    }

    public void save() throws Exception {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            delete(stmt);
            write(conn);
            stmt.close();
            conn.commit();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception(e.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e.toString());
        }
    }

    public void remove() throws Exception {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            delete(stmt);
            stmt.close();
            conn.commit();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new Exception(e.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            throw new Exception(e.toString());
        }
    }

    public void write(Connection conn) throws SQLException {

        String sql = "INSERT INTO scenarios_triggers (id, scenarioid, triggerid, name, description, type, enabled/*, status*/, priority)" +
                " VALUES ("
                + id + ","
                + scenarioid + ","
                + triggerid + ","
                + "\"" + name + "\","
                + "\"" + description + "\","
                + "\"" + type + "\","
                + enabled + ","
                //+ status + ","
                + priority
                + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "scenarioid=" + scenarioid + ","
                + "triggerid=" + triggerid + ","
                + "name=\"" + name + "\","
                + "description=\"" + description + "\","
                + "type=\"" + type + "\","
                + "enabled=" + enabled + ","
                //+ "status=" + status + ","
                + "priority=" + priority + ";";
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }
    }

    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM scenarios_triggers WHERE id=" + id;
        stmt.executeUpdate(sql);
    }

    public static JSONArray getTriggerTypesJSONArray() {
        JSONArray jsonArray = new JSONArray();
        JSONObject json;
        try {
            json = new JSONObject();
            json.put("name", "athome");
            json.put("description", "In casa");
            //json.put("values", "In casa");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("name", "season");
            json.put("description", "Stagione");
            jsonArray.put(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    private void fromResultSet(ResultSet timeintervalsResultSet) throws SQLException {
        id = timeintervalsResultSet.getInt("id");
        scenarioid = timeintervalsResultSet.getInt("scenarioid");

        triggerid = timeintervalsResultSet.getInt("triggerid");
        Trigger trigger = Core.triggerFromId(triggerid);
        if (trigger != null)
            trigger.addListener(this);
        name = timeintervalsResultSet.getString("name");
        description = timeintervalsResultSet.getString("description");
        type = timeintervalsResultSet.getString("type");
        //status = timeintervalsResultSet.getBoolean("status");
        enabled = timeintervalsResultSet.getBoolean("enabled");
        priority = timeintervalsResultSet.getInt("priority");
    }
}
