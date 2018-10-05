package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.Core;
import com.server.webduino.core.datalog.ActionDataLog;
import com.server.webduino.core.webduinosystem.scenario.Conflict;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by giaco on 17/05/2017.
 */
public class ScenarioProgramInstruction /*implements Zone.WebduinoZoneListener*/ {

    public int id = 0;
    public int timerangeid = 0;
    public String name = "";
    public String description = "";
    public int priority = 0;
    //public int sensorid = 0;
    public boolean enabled = true;
    public Date endDate = null;
    public Date startDate = null;
    protected List<Condition> conditions = new ArrayList<>();
    public List<Action> actions = new ArrayList<>();

    public boolean active = false;


   //protected List<ProgramInstructionListener> listeners = new ArrayList<>();

    private boolean conditionsActive = false;
    private Condition.ConditionListener conditionListener = new Condition.ConditionListener() {
        @Override
        public void onActiveChange(boolean active) {
            checkConditions();
        }
    };

    public void setActionListener(Action.ActionListener toAdd) {
        if (actions != null) {
            for (Action action : actions) {
                action.addListener(toAdd);
            }
        }
    }


    /*public interface ProgramInstructionListener {
        void onStart(ScenarioProgramInstruction action);

        void onStop(ScenarioProgramInstruction action);
    }*/

    /*public void addListener(ProgramInstructionListener toAdd) {
        listeners.add(toAdd);
    }

    public void deleteListener(ProgramInstructionListener toRemove) {
        listeners.remove(toRemove);
    }*/


    public ScenarioProgramInstruction(int id, int programtimerangeid, String name, String description, int priority, boolean enabled) {
        this.id = id;
        this.timerangeid = programtimerangeid;
        this.name = name;
        this.description = description;
        this.priority = priority;
        this.enabled = enabled;
    }

    public ScenarioProgramInstruction(JSONObject json) throws Exception {
        fromJson(json);
    }




    public void setEndDate(Date date) {
        endDate = date;
    }

    public void setStartDate(Date date) {
        startDate = date;
    }

    /*@Override
    public void onUpdateTemperature(int zoneId, double newTemperature, double oldTemperature) {
    }*/

    /*@Override
    public void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus) {
    }*/

    /*@Override
    public void onChangeStatus(int zoneId, int sensorid, String status, String oldstatus) {

    }*/

    public void init() {
    }

    public void start() {
        if (!enabled)
            return;

        startDate = Core.getDate();


        active = true;

        // questo serve???
        /*for (ProgramInstructionListener listener : listeners) {
            listener.onStart(this);
        }*/

        for (Condition condition:conditions) {
            condition.start();
            condition.addListener(conditionListener);
        }
        checkConditions();
    }

    public void stop() {

        active = false;
        /*for (ProgramInstructionListener listener : listeners) {
            listener.onStop(this);
        }*/

        for (Condition condition:conditions) {
            condition.stop();
            condition.deleteListener(conditionListener);
        }
    }

    public void checkConditions() {

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
                    action.start();
                } else {
                    action.stop();
                }
            }
        }
    }

    public String getStatus() {
        return "--";
    }

    public void fromJson(JSONObject json) throws Exception {

        if (json.has("id"))
            id = json.getInt("id");
        if (json.has("timerangeid")) timerangeid = json.getInt("timerangeid");
        if (json.has("name")) name = json.getString("name");
        if (json.has("description")) description = json.getString("description");
        if (json.has("priority")) priority = json.getInt("priority");
        if (json.has("enabled")) enabled = json.getBoolean("enabled");
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


    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("timerangeid", timerangeid);
            json.put("name", name);
            json.put("description", description);
            json.put("enabled", enabled);
            json.put("priority", priority);

            JSONArray jarray = new JSONArray();
            if (conditions != null) {
                for (Condition condition : conditions) {
                    jarray.put(condition.toJson());
                }
                json.put("conditions", jarray);
            }
            jarray = new JSONArray();
            if (actions != null) {
                for (Action action : actions) {
                    jarray.put(action.toJson());
                }
                json.put("actions", jarray);
            }

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

        sql = "INSERT INTO scenarios_programinstructions (id, timerangeid, name, description, priority, enabled)" +
                " VALUES ("
                + id + ","
                + timerangeid + ","
                + "\"" + name + "\","
                + "\"" + description + "\","
                + priority + ","
                + Core.boolToString(enabled) + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "id=" + id + ","
                + "timerangeid=" + timerangeid + ","
                + "name=\"" + name + "\","
                + "name=\"" + description + "\","
                + "priority=" + priority + ","
                + "enabled=" + Core.boolToString(enabled) + ";";
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }
        if (conditions != null) {
            for (Condition condition : conditions) {
                condition.write(conn);
            }
        }
        if (actions != null) {
            for (Action action : actions) {
                action.write(conn);
            }
        }
    }

    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM scenarios_programinstructions WHERE id=" + id;
        stmt.executeUpdate(sql);
    }
}
