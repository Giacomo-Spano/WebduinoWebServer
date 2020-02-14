package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.scenario.actions.Action;
import com.server.webduino.core.webduinosystem.scenario.actions.Condition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.sql.Date;
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
    protected List<Condition> conditions = new ArrayList<>();
    public List<Action> actions = new ArrayList<>();

    private boolean conditionsActive = false;
    private Condition.ConditionListener conditionListener = new Condition.ConditionListener() {
        @Override
        public void onActiveChange(boolean active) {
            checkConditionsandStartActions(Core.getTime(),endTime);
        }
    };

    public ScenarioProgramTimeRange(Connection conn, int programid, ResultSet resultSet) throws Exception {
        fromResultSet(conn, programid, resultSet);
    }

    public ScenarioProgramTimeRange(JSONObject json) throws Exception {
        fromJson(json);
    }

    private void init(int id, int programid, String name, String description, LocalTime startTime, LocalTime endTime, boolean enabled, List<Condition> conditions, List<Action> actions, int index) {
        this.id = id;
        this.programid = programid;
        this.name = name;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.enabled = enabled;
        this.conditions = conditions;
        this.actions = actions;
        this.index = index;
    }

    public void start() {

        active = true;
        for (Condition condition:conditions) {
            condition.start(startTime,endTime);
            condition.addListener(conditionListener);
        }
        conditionsActive = false;////
        checkConditionsandStartActions(Core.getTime(),endTime);
    }

    public void stop() {
        active = false;
        // ferma tutte le conditio. Non serve fermare le action
        // perchè dovrebbero fermari da sole con la onactionchange
        for (Condition condition:conditions) {
            condition.stop();
            condition.deleteListener(conditionListener);
        }
        for (Action action: actions) {
            action.stop();
        }
    }

    public void checkConditionsandStartActions(LocalTime startTime, LocalTime endTime) {

        boolean active = true;

        for (Condition condition:conditions) {
            if (!condition.isActive()) {
                active = false;
                break;
            }
        }
        if (conditionsActive != active) {
            conditionsActive = active;
            for (Action action : actions) {
                if (conditionsActive) {
                    action.start(startTime,endTime);
                } else {
                    action.stop();
                }
            }
        }
    }

    public Action getActionFromId(int id) {
        for (Action action : actions) {
            if (action.id == id)
                return action;
        }
        return null;
    }

    public String getActionStatus() {
        String status = "";
        boolean first = true;
        for (Condition condition : conditions) {
            if (!first)
                status += "; ";
            first = false;
            status += "Condition: " + condition.id + " - Status: [" + condition.getStatus() + "]";
        }
        first = true;
        for (Action action : actions) {
            if (!first)
                status += "; ";
            first = false;
            status += "Action: " + action.id + " - Status: [" + action.getStatus() + "]";
        }
        return status;
    }

    public boolean isActive(LocalTime time) {

        if (time == null)
            return false;

        if (!enabled)
            return false;

        if (time.compareTo(startTime) < 0) // timerange inizia dopo
            return false;

        if (time.compareTo(endTime) > 0) // timerange è già finita
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

            JSONArray jarrayconditions = new JSONArray();
            if (conditions != null) {
                for (Condition condition : conditions) {
                    jarrayconditions.put(condition.toJson());
                }
                json.put("conditions", jarrayconditions);
            }
            JSONArray jarrayactions = new JSONArray();
            if (actions != null) {
                for (Action action : actions) {
                    jarrayactions.put(action.toJson());
                }
                json.put("actions", jarrayactions);
            }

            json.put("status", getStatus());
            json.put("actionstatus", getActionStatus());

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public String getStatus() {
        if (active)
            return "active";
        return "not active";
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
        if (json.has("conditions")) {
            JSONArray jsonArray = json.getJSONArray("conditions");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject j = jsonArray.getJSONObject(i);
                Condition condition = new Condition(j);
                conditions.add(condition);
            }
        }
        if (json.has("actions")) {
            JSONArray jsonArray = json.getJSONArray("actions");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject j = jsonArray.getJSONObject(i);
                Action action = new Action(j);
                actions.add(action);
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
            startTime = LocalTime.parse("00:00", DateTimeFormatter.ISO_LOCAL_TIME);
        if (endTime == null)
            endTime = LocalTime.parse("00:00", DateTimeFormatter.ISO_LOCAL_TIME);

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

        if (conditions != null) {
            for (Condition condition : conditions) {
                condition.timerangeid = id;
                condition.write(conn);
            }
        }
        if (actions != null) {
            for (Action action : actions) {
                action.timerangeid = id;
                action.write(conn);
            }
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

        int offset = TimeZone.getTimeZone("Europe/Rome").getOffset(Instant.now().toEpochMilli());
        offset = offset / 1000 / 60 / 60;

        Timestamp ts = resultSet.getTimestamp("starttime");
        LocalTime startTime = ts.toLocalDateTime().toLocalTime();
        startTime = startTime.plusHours(-offset);
        startTime = startTime.withSecond(0);

        ts = resultSet.getTimestamp("endtime");
        LocalTime endTime = ts.toLocalDateTime().toLocalTime();
        endTime = endTime.plusHours(-offset);
        endTime = endTime.plusMinutes(-1);
        endTime = endTime.withSecond(59);


        //java.util.Date time = resultSet.getTimestamp("starttime");
        //Instant instant = Instant.ofEpochMilli(time.getTime());
        //LocalTime startTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
        //startTime = startTime.withSecond(0);

        //time = resultSet.getTimestamp("endtime");
        //instant = Instant.ofEpochMilli(time.getTime());

        //LocalTime endTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
        //endTime = endTime.plusMinutes(-1);
        //endTime = endTime.withSecond(59);

        Boolean enabled = resultSet.getBoolean("enabled");
        int index = resultSet.getInt("index");

        conditions = readConditions(conn,id);
        actions = readActions(conn,id);


        init(id, programid, name, description, startTime, endTime, enabled, conditions, actions, index);
    }

    private List<Condition> readConditions(Connection conn, int timerangeid) throws Exception {

        List<Condition> list = new ArrayList<>();
        String sql;
        Statement stmt = conn.createStatement();
        sql = "SELECT * FROM scenarios_conditions WHERE timerangeid=" + timerangeid + " ;";
        ResultSet resultSet = stmt.executeQuery(sql);
        //ScenarioProgramInstructionFactory factory = new ScenarioProgramInstructionFactory();
        while (resultSet.next()) {
            Condition condition = new Condition(conn,resultSet);
            if (condition != null)
                list.add(condition);
        }
        resultSet.close();
        stmt.close();
        return list;
    }

    private List<Action> readActions(Connection conn, int timerangeid) throws Exception {

        List<Action> list = new ArrayList<>();
        String sql;
        Statement stmt = conn.createStatement();
        sql = "SELECT * FROM scenarios_actions WHERE timerangeid=" + timerangeid + " ;";
        ResultSet resultSet = stmt.executeQuery(sql);
        //ScenarioProgramInstructionFactory factory = new ScenarioProgramInstructionFactory();
        while (resultSet.next()) {
            Action action = new Action(conn,resultSet);
            if (action != null)
                list.add(action);
        }
        resultSet.close();
        stmt.close();
        return list;
    }

    public void setActionListener(Action.ActionListener toAdd) {

        if (actions != null) {
            for (Action action : actions) {
                action.addListener(toAdd);
            }
        }
    }
}
