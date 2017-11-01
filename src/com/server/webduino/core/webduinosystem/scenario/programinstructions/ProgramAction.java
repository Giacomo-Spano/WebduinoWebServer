package com.server.webduino.core.webduinosystem.scenario.programinstructions;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by giaco on 17/05/2017.
 */
public class ProgramAction implements Zone.WebduinoZoneListener {

    public int id = 0;
    public int timerangeid = 0;
    public String type = "";
    public String name = "";
    public String description = "";
    public int priority = 0;
    public int actuatorid = 0;
    public double targetvalue = 0;
    public double thresholdvalue = 0;
    public int zoneId = 0;
    public int seconds = 0;
    public boolean enabled = true;

    boolean active = false;

    public ProgramAction(int id, int programtimerangeid, String type, String name, String description, int priority, int actuatorid, double targevalue, double thresholdvalue,
                         int zoneId, int seconds, boolean enabled) {
        this.id = id;
        this.timerangeid = programtimerangeid;
        this.type = type;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.actuatorid = actuatorid;
        this.targetvalue = targevalue;
        this.thresholdvalue = thresholdvalue;
        this.zoneId = zoneId;
        this.seconds = seconds;
        this.enabled = enabled;
    }

    @Override
    public void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature) {
    }

    @Override
    public void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus) {
    }

    public void init() {
    }

    public void start() {
        active = true;
    }
    public void stop() {
        active = false;
    }

    public String getStatus() {
        return "--";
    }


    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("timerangeid", timerangeid);
            json.put("type", type);
            json.put("name", name);
            json.put("description", description);
            json.put("actuatorid", actuatorid);
            SensorBase actuator = Core.getSensorFromId(actuatorid);
            if (actuator != null) json.put("actuatorname", actuator.getName());
            json.put("targetvalue", targetvalue);
            json.put("thresoldvalue", thresholdvalue);
            json.put("zoneid", zoneId);
            Zone zone = Core.getZoneFromId(zoneId);
            if (zone != null) json.put("zonename", zone.getName());
            json.put("seconds", seconds);
            json.put("enabled", enabled);

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

        String sql;
        DateFormat df = new SimpleDateFormat("HH:mm:ss");

        java.util.Date time = new java.util.Date(seconds * 1000);
        sql = "INSERT INTO scenarios_programinstructions (id, timerangeid, type, name, description, priority, actuatorid, targetvalue, thresholdvalue, zoneid, time, enabled)" +
                " VALUES ("
                + id + ","
                + timerangeid + ","
                + "\"" + type + "\","
                + "\"" + name + "\","
                + "\"" + description + "\","
                + priority + ","
                + actuatorid + ","
                + targetvalue + ","
                + thresholdvalue + ","
                + zoneId + ","
                + "'" + df.format(time) + "',"
                + Core.boolToString(enabled) + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "id=" + id + ","
                + "timerangeid=" + timerangeid + ","
                + "type=\"" + type + "\","
                + "name=\"" + name + "\","
                + "name=\"" + description + "\","
                + "priority=" + priority + ","
                + "actuatorid=" + actuatorid + ","
                + "targetvalue=" + targetvalue + ","
                + "thresholdvalue=" + thresholdvalue + ","
                + "zoneId=" + zoneId + ","
                + "time='" + df.format(time) + "',"
                + "enabled=" + Core.boolToString(enabled) + ";";
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }
    }

    public void delete(Statement stmt) throws SQLException {

        String sql = "DELETE FROM scenarios_programinstructions WHERE id=" + id;
        stmt.executeUpdate(sql);

    }
}
