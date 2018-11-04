package com.server.webduino.core.webduinosystem.scenario.actions;

import com.quartz.QuartzListener;
import com.server.webduino.core.Core;
import com.server.webduino.core.Trigger;
import com.server.webduino.core.datalog.ActionDataLog;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.WebduinoSystem;
import com.server.webduino.core.webduinosystem.scenario.Conflict;
import com.server.webduino.core.webduinosystem.services.Service;
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
    public final String ACTION_WEBDUINOSYSTEM = "webduinosystem";

    public int id = 0;
    public String description = "";
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
    public int webduinosystemid = 0;
    public String param = "";
    public int deviceid = 0;

    private ActionDataLog dataLog = new ActionDataLog();

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

        if (action == null)
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
        } else if (type.equals(ACTION_WEBDUINOSYSTEM)) {
            if (action.webduinosystemid == this.webduinosystemid) {
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
        //if (newconflict.action.sensorid == this.sensorid) {

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

        //Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);

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
                    sensor.sendCommand(actioncommand, json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (type.equals(ACTION_SERVICE)) {
            Service service = Core.getServiceFromId(serviceid);
            if (service != null) {
                JSONObject json = new JSONObject();
                try {
                    json.put("actionid", id);
                    json.put("webduinosystemid", webduinosystemid);
                    json.put("deviceid", deviceid);
                    json.put("param", param);
                    json.put("date",Core.getDate().toString());
                    json.put("type", "alarm");
                    service.sendCommand(actioncommand, json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else if (type.equals(ACTION_TRIGGER)) {
            Trigger trigger = Core.getTriggerFromId(triggerid);
            if (trigger != null) {
                JSONObject json = new JSONObject();
                trigger.sendCommand(actioncommand, json);
            }

        } else if (type.equals(ACTION_WEBDUINOSYSTEM)) {
            WebduinoSystem webduinoSystem = Core.getWebduinoSystemFromId(triggerid);
            if (webduinoSystem != null) {
                JSONObject json = new JSONObject();
                try {
                    json.put("command", actioncommand);
                    webduinoSystem.sendCommand(json);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
        dataLog.writelog("start",this);
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
            Service service = Core.getServiceFromId(serviceid);
            if (service != null) {
                service.endCommand();
            }
        } else if (type.equals(ACTION_TRIGGER)) {
            Trigger trigger = Core.getTriggerFromId(triggerid);
            if (trigger != null) {
                trigger.endCommand();
            }

        } else if (type.equals(ACTION_WEBDUINOSYSTEM)) {
            WebduinoSystem webduinoSystem = Core.getWebduinoSystemFromId(triggerid);
            if (webduinoSystem != null) {
                webduinoSystem.endCommand();
            }
        }
        dataLog.writelog("stop",this);
    }

    public String getStatus() {

        String statustext = "";
        /*if (type.equals(ACTION_ACTUATOR)) {
            SensorBase sensor = Core.getSensorFromId(actuatorid);
            if (sensor != null) {
                statustext += "Attuatore:" + sensor.getName();
                statustext += " targetvalue:" + targetvalue;
                statustext += " seconds:" + seconds;
                statustext += " zoneid:" + zoneid;
                statustext += " zonesensorid:" + zonesensorid;
                statustext += " actioncommand:" + actioncommand;
            }
        } else if (type.equals(ACTION_SERVICE)) {
            Service service = Core.getServiceFromId(serviceid);
            if (service != null) {
                statustext += "Servizio:" + service.getName();
                statustext += " actioncommand:" + actioncommand;
            }

        } else if (type.equals(ACTION_TRIGGER)) {
            Trigger trigger = Core.getTriggerFromId(triggerid);
            if (trigger != null) {
                statustext += "Trigger:" + trigger.getName();
                statustext += " actioncommand:" + actioncommand;
            }

        } else if (type.equals(ACTION_WEBDUINOSYSTEM)) {
            WebduinoSystem webduinoSystem = Core.getWebduinoSystemFromId(triggerid);
            if (webduinoSystem != null) {
                statustext += "System:" + webduinoSystem.getName();
                statustext += " actioncommand:" + actioncommand;
            }
        }*/

        if (active)
            return statustext += "Status: active";
        return statustext += "Status: not active";
    }


    public void fromResultSet(Connection conn, ResultSet resultSet) throws Exception {
        id = resultSet.getInt("id");
        description = resultSet.getString("description");
        timerangeid = resultSet.getInt("timerangeid");
        type = resultSet.getString("type");
        actioncommand = resultSet.getString("actioncommand");
        targetvalue = resultSet.getDouble("targetvalue");
        seconds = resultSet.getInt("seconds");
        serviceid = resultSet.getInt("serviceid");
        actuatorid = resultSet.getInt("sensorid");
        zoneid = resultSet.getInt("zoneid");
        zonesensorid = resultSet.getInt("zonesensorid");
        param = resultSet.getString("param");
        deviceid = resultSet.getInt("deviceid");
        triggerid = resultSet.getInt("triggerid");
        webduinosystemid = resultSet.getInt("webduinosystemid");
    }

    public void fromJson(JSONObject json) throws Exception {
        if (json.has("id")) id = json.getInt("id");
        if (json.has("description")) description = json.getString("description");
        if (json.has("timerangeid")) timerangeid = json.getInt("timerangeid");
        if (json.has("type")) type = json.getString("type");
        if (json.has("actioncommand")) actioncommand = json.getString("actioncommand");
        if (json.has("targetvalue")) targetvalue = json.getDouble("targetvalue");
        if (json.has("seconds")) seconds = json.getInt("seconds");
        if (json.has("serviceid")) serviceid = json.getInt("serviceid");
        if (json.has("sensorid")) actuatorid = json.getInt("sensorid");
        if (json.has("zoneid")) zoneid = json.getInt("zoneid");
        if (json.has("zonesensorid")) zonesensorid = json.getInt("zonesensorid");
        if (json.has("param")) param = json.getString("param");
        if (json.has("deviceid")) deviceid = json.getInt("deviceid");
        if (json.has("triggerid")) triggerid = json.getInt("triggerid");
        if (json.has("webduinosystemid")) webduinosystemid = json.getInt("webduinosystemid");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("description", description);
            json.put("timerangeid", timerangeid);
            json.put("type", type);
            json.put("actioncommand", actioncommand);
            json.put("targetvalue", targetvalue);
            json.put("seconds", seconds);
            json.put("serviceid", serviceid);
            json.put("sensorid", actuatorid);
            json.put("zoneid", zoneid);
            json.put("zonesensorid", zonesensorid);
            json.put("param", param);
            json.put("deviceid", deviceid);
            json.put("triggerid", triggerid);
            json.put("webduinosystemid", webduinosystemid);

            json.put("status", getStatus());
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

        String serviceidstr = "null";
        if (serviceid > 0)
            serviceidstr = "" + serviceid;
        String actuatoridstr = "null";
        if (actuatorid > 0)
            actuatoridstr = "" + actuatorid;
        String triggeridstr = "null";
        if (triggerid > 0)
            triggeridstr = "" + triggerid;
        String webduinosystemidstr = "null";
        if (webduinosystemid > 0)
            webduinosystemidstr = "" + webduinosystemid;
        String zoneidstr = "null";
        if (zoneid > 0)
            zoneidstr = "" + zoneid;
        String zonesensoridstr = "null";
        if (zonesensorid > 0)
            zonesensoridstr = "" + zonesensorid;
        String deviceidstr = "null";
        if (deviceid > 0)
            deviceidstr = "" + deviceid;

        String sql;
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        sql = "INSERT INTO scenarios_actions (id, description, timerangeid, type, actioncommand, targetvalue, seconds, sensorid, serviceid, zoneid,zonesensorid, triggerid, deviceid, webduinosystemid, param)" +
                " VALUES ("
                + id + ","
                + "\"" + description + "\","
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
                + deviceidstr + ","
                + webduinosystemidstr + ","
                + "\"" + param + "\""
                + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "id=" + id + ","
                + "description=\"" + description + "\","
                + "timerangeid=" + timerangeid + ","
                + "type=\"" + type + "\","
                + "actioncommand=\"" + actioncommand + "\","
                + "targetvalue=" + targetvalue + ","
                + "seconds=" + seconds + ","
                + "sensorid=" + actuatoridstr + ","
                + "serviceid=" + serviceidstr + ","
                + "zoneid=" + zoneidstr + ","
                + "zonesensorid=" + zonesensoridstr + ","
                + "triggerid=" + triggeridstr + ","
                + "deviceid=" + deviceidstr + ","
                + "webduinosystemid=" + webduinosystemidstr + ","
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
