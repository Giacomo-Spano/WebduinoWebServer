package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramAction;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramActionFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.ejb.Local;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by giaco on 17/05/2017.
 */
public class ScenarioProgramTimeRange extends DBObject {

    boolean active = false;

    public int id;
    public int programid;
    public String name;
    public String description;
    public LocalTime startTime;
    public LocalTime endTime;
    public boolean enabled;
    public int index;


    public List<ProgramAction> programActionList = new ArrayList<>();

    public ScenarioProgramTimeRange(Connection conn, int programid, ResultSet resultSet) throws Exception {
        fromResultSet(conn,programid,resultSet);
    }

    public ScenarioProgramTimeRange(JSONObject json) throws Exception {
        fromJson(json);
    }

    private void init(int id, int programid, String name, String description, LocalTime startTime, LocalTime endTime, boolean enabled, List<ProgramAction> actions, int index) {
        this.id = id;
        this.programid = programid;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.enabled = enabled;
        this.programActionList = actions;
        this.index = index;
    }

    public void start() {
        active = true;
        for (ProgramAction action: programActionList) {
            action.start();
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY,endTime.getHour());
            cal.set(Calendar.MINUTE,endTime.getMinute());
            cal.set(Calendar.SECOND,endTime.getSecond());
            action.setEndDate(cal.getTime());
        }
    }
    public void stop() {
        active = false;
        for (ProgramAction action: programActionList) {
            action.stop();
        }
    }

    public String getActionStatus() {
        String status = "";
        boolean first = true;
        for (ProgramAction action: programActionList) {
            if (!first)
                status += "; ";
            first = false;
            status += "Action: " + action.id + "." + action.name + " - Status: [" + action.getStatus() + "]";
        }
        return status;
    }

    public boolean isActive(LocalTime time) {

        if (time == null)
            return false;

        if (!enabled) return false;

        if (time.compareTo(startTime) < 0) // timerange inizia dopo
            return false;


        if (/*!endTime.equals(LocalTime.MIDNIGHT) && */time.compareTo(endTime) > 0) // timerange è già finita
            return false;

        return true;
    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("programid", programid);
            json.put("name", name);
            json.put("description", description);
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            json.put("starttime", startTime.toString());
            json.put("endtime", endTime.toString());
            json.put("enabled", enabled);
            json.put("index", index);

            JSONArray jarray = new JSONArray();
            if (programActionList != null) {
                for (ProgramAction action : programActionList) {
                    jarray.put(action.toJson());
                }
                json.put("actions", jarray);
            }

            if (active) {
                json.put("status", "Attivo");
                json.put("actionstatus", getActionStatus());
            } else {
                json.put("status", "Non attivo");
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void fromJson(JSONObject json) throws Exception {

        if (json.has("id"))
            id = json.getInt("id");
        if (json.has("programid"))
            programid = json.getInt("programid");
        if (json.has("name"))
            name = json.getString("name");
        if (json.has("description"))
            description = json.getString("description");
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        if (json.has("starttime") && json.has("endtime")) {
            String str = json.getString("starttime");
            startTime = LocalTime.parse(str, DateTimeFormatter.ISO_LOCAL_TIME);
            str = json.getString("endtime");
            endTime = LocalTime.parse(str, DateTimeFormatter.ISO_LOCAL_TIME);
            if (endTime.compareTo(startTime) < 0)
                throw new Exception("start time " + startTime.toString() + "must be before end time " + endTime.toString());
            if (endTime.equals(LocalTime.MIDNIGHT))
                throw new Exception("max end time 23:59");
        } else {
            throw new Exception("missing start/end time");
        }
        if (json.has("enabled"))
            enabled = json.getBoolean("enabled");
        if (json.has("index"))
            index = json.getInt("index");

        if (json.has("actions")) {

            ProgramActionFactory factory = new ProgramActionFactory();
            JSONArray jsonArray = json.getJSONArray("actions");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                ProgramAction action = null;
                try {
                    action = factory.fromJson(jo);
                    programActionList.add(action);
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new Exception(e.toString());
                }
            }
        }
    }

    @Override
    public void write(Connection conn) throws SQLException {

        Calendar myCal = Calendar.getInstance();
        myCal.set(Calendar.YEAR, 0);
        myCal.set(Calendar.MONTH, 0);
        myCal.set(Calendar.DAY_OF_MONTH, 0);
        myCal.set(Calendar.HOUR_OF_DAY, 0);
        myCal.set(Calendar.MINUTE, 0);
        myCal.set(Calendar.SECOND, 0);

        if (startTime == null)
            startTime = LocalTime.parse("00:00",DateTimeFormatter.ISO_LOCAL_TIME);
        if (endTime == null)
            endTime = LocalTime.parse("00:00",DateTimeFormatter.ISO_LOCAL_TIME);

        String sql;
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        sql = "INSERT INTO scenarios_programtimeranges (id, programid, name, description, starttime, endtime, scenarios_programtimeranges.index, enabled)" +
                " VALUES ("
                + id + ","
                + programid + ","
                + "\"" + name + "\","
                + "\"" + description + "\","
                + "'" + startTime.toString() + "',"
                + "'" + endTime.toString() + "',"
                + index + ","
                + "" + Core.boolToString(enabled) + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "id=" + id + ","
                + "programid=" + programid + ","
                + "name=\"" + name + "\","
                + "description=\"" + description + "\","
                + "starttime='" + startTime.toString() + "',"
                + "endtime='" + endTime.toString() + "',"
                + "scenarios_programtimeranges.index=" + index + ","
                + "enabled=" + Core.boolToString(enabled) + ";";
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }

        for (ProgramAction action : programActionList) {
            action.write(conn);
        }
        stmt.close();
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
            String sql = "DELETE FROM scenarios_programtimeranges WHERE id=" + id;
            stmt.executeUpdate(sql);
    }

    private void fromResultSet(Connection conn, int programid, ResultSet resultSet) throws Exception {
        int id = resultSet.getInt("id");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");

        java.util.Date time = resultSet.getTimestamp("starttime");
        Instant instant = Instant.ofEpochMilli(time.getTime());
        LocalTime startTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();

        time = resultSet.getTimestamp("endtime");
        instant = Instant.ofEpochMilli(time.getTime());
        LocalTime endTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();

        Boolean enabled = resultSet.getBoolean("enabled");
        int index = resultSet.getInt("index");

        List<ProgramAction> actions = readProgramActions(conn, id);
        init(id, programid, name, description, startTime, endTime, enabled, actions, index);
    }

    private static List<ProgramAction> readProgramActions(Connection conn, int programtimerangeid) throws Exception {

        List<ProgramAction> list = new ArrayList<>();
        String sql;
        Statement stmt4 = conn.createStatement();
        sql = "SELECT * FROM scenarios_programinstructions WHERE timerangeid=" + programtimerangeid + " ;";
        ResultSet instructionsResultset = stmt4.executeQuery(sql);
        ProgramActionFactory factory = new ProgramActionFactory();
        while (instructionsResultset.next()) {

            ProgramAction action = factory.fromResultSet(instructionsResultset);
            if (action != null)
                list.add(action);
        }
        instructionsResultset.close();
        stmt4.close();
        return list;
    }
}
