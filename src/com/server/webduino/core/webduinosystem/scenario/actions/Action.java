package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by giaco on 17/05/2017.
 */
public class Action {

    public int id = 0;
    public int programactionid = 0;
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

    public Action(Connection conn, ResultSet resultSet) throws Exception {
        fromResultSet(conn, resultSet);
    }

    public Action(JSONObject json) throws Exception {
        fromJson(json);
    }

    public void fromResultSet(Connection conn, ResultSet resultSet) throws Exception {
        id = resultSet.getInt("id");
        programactionid = resultSet.getInt("programactionid");
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
        if (json.has("programactionid")) programactionid = json.getInt("programactionid");
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
            json.put("programactionid", programactionid);
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
        if (actuatorid>0)
            actuatoridstr = "" + actuatorid;
        String zoneidstr ="null";
        if (actuatorid>0)
            actuatoridstr = "" + actuatorid;
        String zonesensoridstr ="null";
        if (actuatorid>0)
            actuatoridstr = "" + actuatorid;

        String sql;
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        sql = "INSERT INTO scenarios_actions (id, programactionid, type, actioncommand, targetvalue, seconds, actuatorid, serviceid, zoneid,zonesensorid, triggerid, param)" +
                " VALUES ("
                + id + ","
                + programactionid + ","
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
                + "programactionid=" + programactionid + ","
                + "type=\"" + type + "\","
                + "type=\"" + actioncommand + "\","
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
