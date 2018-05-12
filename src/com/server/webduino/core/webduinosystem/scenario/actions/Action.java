package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.scenario.Conflict;
import com.server.webduino.core.webduinosystem.zones.Zone;
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
public class Action {

    public final String ACTION_ACTUATOR = "actuator";
    public final String ACTION_SERVICE = "service";
    public final String ACTION_TRIGGER = "trigger";

    public int id = 0;
    public int timerangeid = 0;
    public String type = "";
    public String actioncommand = "";
    public double targetvalue = 0;
    public int seconds = 0;
    public int serviceid = 0;
    public int actuatorid = 0;
    public int zoneid = 0;
    public int zonesensorid = 0;
    public int triggerid = 0;
    public String param = "";

    private boolean active = false;

    protected List<ActionListener> listeners = new ArrayList<>();

    public Action(Connection conn, ResultSet resultSet) throws Exception {
        fromResultSet(conn, resultSet);
    }

    public Action(JSONObject json) throws Exception {
        fromJson(json);
    }

    public interface ActionListener {
        void onStart(Action action);
        void onStop(Action action);
    }

    public void addListener(ActionListener toAdd) {
        listeners.add(toAdd);
    }

    public void deleteListener(ActionListener toRemove) {
        listeners.remove(toRemove);
    }

    List<Conflict> conflictList = new ArrayList<>();

    public boolean hasConflict(Action action) {

        if (action ==  null)
            return false;

        if (type.equals(ACTION_ACTUATOR)) {
            if (action.actuatorid == this.actuatorid) {
                return true;
            }
        } else if (type.equals(ACTION_SERVICE)) {
            return false;
        } else if (type.equals(ACTION_TRIGGER)) {
            if (action.triggerid == this.triggerid) {
                return true;
            }
        }

        return false;
    }

    public void addConflict(Conflict newconflict) {

        // controlla che non ci sia già nella lista altrimenti
        for (Conflict conflict : conflictList) {
            if (conflict.action.id == newconflict.action.id) {
                return;
            }
        }

        // DA RIFARE in base al tipo di action decide se aggiungere un conflito
        // se la action ha lo stesso actuator aggiunge il conflitto
        //if (newconflict.action.actuatorid == this.actuatorid) {

        conflictList.add(newconflict);
            /*if ((newconflict.action instanceof KeepTemperatureScenarioProgramInstruction || newconflict.action instanceof KeepOffScenarioProgramInstruction) &&
                    (this instanceof KeepTemperatureScenarioProgramInstruction || this instanceof KeepOffScenarioProgramInstruction)) {
                conflictList.add(newconflict);
            }*/
        //}
    }

    public void removeConflict(Action action) {
        for (Conflict conflict : conflictList) {
            if (conflict.action.id == action.id) {
                conflictList.remove(conflict);
                break;
            }
        }
    }

    public void start() {
        active = true;

        for (ActionListener listener : listeners) {
            listener.onStart(this);
        }

        if (type.equals(ACTION_ACTUATOR)) {
            SensorBase sensor = Core.getSensorFromId(actuatorid);
            if (sensor != null) {
                JSONObject json = new JSONObject();
                try {
                    json.put("targetvalue", targetvalue);
                    json.put("seconds", seconds);
                    json.put("zoneid", zoneid);
                    json.put("zonesensorid", zonesensorid);
                    sensor.sendCommand(actioncommand,json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        } else if (type.equals(ACTION_SERVICE)) {

        } else if (type.equals(ACTION_TRIGGER)) {

        }
    }

    public void stop() {
        active = false;

        for (ActionListener listener : listeners) {
            listener.onStop(this);
        }

        if (type.equals(ACTION_ACTUATOR)) {
            SensorBase sensor = Core.getSensorFromId(actuatorid);
            if (sensor != null) {
                sensor.endCommand();
            }
        } else if (type.equals(ACTION_SERVICE)) {

        } else if (type.equals(ACTION_TRIGGER)) {

        }
    }

    public String getStatus() {
        if (active)
            return "active";
        return "not active";
    }


    public void fromResultSet(Connection conn, ResultSet resultSet) throws Exception {
        id = resultSet.getInt("id");
        timerangeid = resultSet.getInt("timerangeid");
        type = resultSet.getString("type");
        actioncommand = resultSet.getString("actioncommand");
        targetvalue = resultSet.getDouble("targetvalue");
        seconds = resultSet.getInt("seconds");
        serviceid = resultSet.getInt("serviceid");
        actuatorid = resultSet.getInt("actuatorid");
        zoneid = resultSet.getInt("zoneid");
        zonesensorid = resultSet.getInt("zonesensorid");
        param = resultSet.getString("param");
        triggerid = resultSet.getInt("triggerid");

    }

    public void fromJson(JSONObject json) throws Exception {
        if (json.has("id")) id = json.getInt("id");
        if (json.has("timerangeid")) timerangeid = json.getInt("timerangeid");
        if (json.has("type")) type = json.getString("type");
        if (json.has("actioncommand")) actioncommand = json.getString("actioncommand");
        if (json.has("targetvalue")) targetvalue = json.getDouble("targetvalue");
        if (json.has("seconds")) seconds = json.getInt("seconds");
        if (json.has("serviceid")) serviceid = json.getInt("serviceid");
        if (json.has("actuatorid")) actuatorid = json.getInt("actuatorid");
        if (json.has("zoneid")) zoneid = json.getInt("zoneid");
        if (json.has("zonesensorid")) zonesensorid = json.getInt("zonesensorid");
        if (json.has("param")) param = json.getString("param");
        if (json.has("triggerid")) triggerid = json.getInt("triggerid");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("timerangeid", timerangeid);
            json.put("type", type);
            json.put("actioncommand", actioncommand);
            json.put("targetvalue", targetvalue);
            json.put("seconds", seconds);
            json.put("serviceid", serviceid);
            json.put("actuatorid", actuatorid);
            json.put("zoneid", zoneid);
            json.put("zonesensorid", zonesensorid);
            json.put("param", param);
            json.put("triggerid", triggerid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
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

    public void delete() throws Exception {

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

        String serviceidstr ="null";
        if (serviceid>0)
            serviceidstr = "" + serviceid;
        String actuatoridstr ="null";
        if (actuatorid>0)
            actuatoridstr = "" + actuatorid;
        String triggeridstr ="null";
        if (triggerid>0)
            triggeridstr = "" + triggerid;
        String zoneidstr ="null";
        if (zoneid>0)
            zoneidstr = "" + zoneid;
        String zonesensoridstr ="null";
        if (zonesensorid>0)
            zonesensoridstr = "" + zonesensorid;

        String sql;
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        sql = "INSERT INTO scenarios_actions (id, timerangeid, type, actioncommand, targetvalue, seconds, actuatorid, serviceid, zoneid,zonesensorid, triggerid, param)" +
                " VALUES ("
                + id + ","
                + timerangeid + ","
                + "\"" + type + "\","
                + "\"" + actioncommand + "\","
                + targetvalue + ","
                + seconds + ","
                + actuatoridstr + ","
                + serviceidstr + ","
                + zoneidstr + ","
                + zonesensoridstr + ","
                + triggeridstr + ","
                + "\"" + param + "\""
                + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "id=" + id + ","
                + "timerangeid=" + timerangeid + ","
                + "type=\"" + type + "\","
                + "actioncommand=\"" + actioncommand + "\","
                + "targetvalue=" + targetvalue + ","
                + "seconds=" + seconds + ","
                + "actuatorid=" + actuatoridstr + ","
                + "serviceid=" + serviceidstr + ","
                + "zoneid=" + zoneidstr + ","
                + "zonesensorid=" + zonesensoridstr + ","
                + "triggerid=" + triggeridstr + ","
                + "param=\"" + param + "\""
                + ";";
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }
    }

    public void delete(Statement stmt) throws SQLException {

        String sql = "DELETE FROM scenarios_actions WHERE id=" + id;
        stmt.executeUpdate(sql);

    }
}
