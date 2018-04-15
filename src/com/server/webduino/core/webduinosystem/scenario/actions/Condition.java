package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.scenario.ScenarioProgramTimeRange;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by giaco on 17/05/2017.
 */
public class Condition {

    public int id = 0;
    public int programactionid = 0;
    public int zoneid;
    public int zonesensorid;
    public int triggerid = 0;
    public String status = "";
    public String type;
    public String valueoperator;
    public double value;
    public String[] valueoperators = {">", "<", "="};

    public Condition(JSONObject json) throws Exception {
        fromJson(json);
    }

    public Condition(Connection conn, ResultSet resultSet) throws Exception {
        fromResultSet(conn, resultSet);
    }

    public void fromJson(JSONObject json) throws Exception {
        if (json.has("id")) id = json.getInt("id");
        if (json.has("programactionid")) programactionid = json.getInt("programactionid");
        if (json.has("zoneid")) zoneid = json.getInt("zoneid");
        if (json.has("zonesensorid")) zonesensorid = json.getInt("zonesensorid");
        if (json.has("triggerid")) triggerid = json.getInt("triggerid");
        if (json.has("status")) status = json.getString("status");
        if (json.has("type")) type = json.getString("type");
        if (json.has("value")) value = json.getInt("value");
        if (json.has("valueoperator")) valueoperator = json.getString("valueoperator");
    }

    public void fromResultSet(Connection conn, ResultSet resultSet) throws Exception {
        id = resultSet.getInt("id");
        zoneid = resultSet.getInt("zoneid");
        zonesensorid = resultSet.getInt("zonesensorid");
        triggerid = resultSet.getInt("triggerid");
        status = resultSet.getString("status");
        type = resultSet.getString("type");
        value = resultSet.getDouble("value");
        valueoperator = resultSet.getString("valueoperator");
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("zoneid", zoneid);
            json.put("zonesensorid", zonesensorid);
            json.put("triggerid", triggerid);
            json.put("status", status);
            json.put("type", type);
            json.put("value", value);
            json.put("valueoperator", valueoperator);
            JSONArray jsonArray = new JSONArray();
            for(int  i = 0; i < valueoperators.length; i++) {
                jsonArray.put(valueoperators[i]);
            }
            json.put("valueoperators", jsonArray);
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
        String zoneidstr ="null";
        if (zoneid>0)
            zoneidstr = "" + zoneid;
        String zonesensoridstr = "null";
        if (zonesensorid>0)
            zonesensoridstr = "" + zonesensorid;
        String triggerstr = "null";
        if (triggerid>0)
            triggerstr = "" + triggerid;

        String sql;
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        sql = "INSERT INTO scenarios_conditions (id, programactionid, zoneid, zonesensorid, triggerid, status,type,value,valueoperator)" +
                " VALUES ("
                + id + ","
                + programactionid + ","
                + zoneidstr + ","
                + zonesensoridstr + ","
                + triggerstr + ","
                + "\"" + status + "\","
                + "\"" + type + "\","
                + value + ","
                + "\"" + valueoperator + "\""
                + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "id=" + id + ","
                + "programactionid=" + programactionid + ","
                + "zoneid=" + zoneidstr + ","
                + "zonesensorid=" + zonesensoridstr + ","
                + "triggerid=" + triggerstr + ","
                + "status=\"" + status + "\","
                + "type=\"" + type + "\","
                + "value=" + value + ","
                + "valueoperator=\"" + valueoperator + "\""
                + ";";
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }
    }

    public void delete(Statement stmt) throws SQLException {

        String sql = "DELETE FROM scenarios_conditions WHERE id=" + id;
        stmt.executeUpdate(sql);

    }
}
