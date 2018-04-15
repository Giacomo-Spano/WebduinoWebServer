package com.server.webduino.core;

import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.TriggerListener;

import java.awt.*;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created by giaco on 17/05/2017.
 */
public class Trigger extends DBObject {

    private List<ActionCommand> actionCommandList = new ArrayList<>();
    protected List<String> statusList = new ArrayList<String>();
    public int id = 0;
    public String name = "";
    public boolean status = false;
    public java.util.Date date;

    //final public String STATUS_ENABLED ="enabled";
    //final public String STATUS_DISABLED ="disabled";

    public interface TriggerListener {
        void onChangeStatus(boolean status);
    }

    protected List<TriggerListener> listeners = new ArrayList<TriggerListener>();

    public void addListener(TriggerListener toAdd) {
        for (TriggerListener listener : listeners) {
            if (listener == toAdd)
                return;
        }
        listeners.add(toAdd);
    }

    public void deleteListener(TriggerListener toRemove) {
        listeners.remove(toRemove);
    }

    protected void createStatusList() {
        statusList.add("on");
        statusList.add("off");
    }



    public Trigger(int id, String name, boolean status, java.util.Date date) {
        createStatusList();
        this.id = id;
        this.name = name;
        this.status = status;
        this.date = date;
        ActionCommand cmd = new ActionCommand("enable","Abilita");
        cmd.addStatus("Stato");
        actionCommandList.add(cmd);
        cmd = new ActionCommand("disable","Disabilita");
        cmd.addStatus("Stato");
        actionCommandList.add(cmd);
    }

    public Trigger(JSONObject json) throws Exception {
        createStatusList();
        fromJson(json);
    }


    public String getStatus() {
        return "--";
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("zonesensorstatus", status);
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        if (date != null)
            json.put("nextjobdate", df.format(date));
        json.put("actioncommandlist", getActionCommandListJSONArray());
        json.put("statuslist", getStatusListJSONArray());

        return json;
    }

    public JSONArray getStatusListJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (String status: statusList) {
            jsonArray.put(status);
        }
        return jsonArray;
    }

    public JSONArray getActionCommandListJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (ActionCommand command: actionCommandList) {
            jsonArray.put(command.toJson());
        }
        return jsonArray;
    }

    @Override
    public void fromJson(JSONObject json) throws Exception {

        if (json.has("id"))
            id = json.getInt("id");
        if (json.has("name"))
            name = json.getString("name");
        if (json.has("zonesensorstatus"))
            status = json.getBoolean("zonesensorstatus");
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM triggers WHERE id=" + id;
        stmt.executeUpdate(sql);
    }


    public void saveStatus() throws Exception {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
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

    public void write(Connection conn) throws SQLException {

        String sql;
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = Core.getDate();

        sql = "INSERT INTO triggers (id, name, status, lastupdate)" +
                " VALUES ("
                + id + ","
                + "\"" + name + "\","
                + status + ","
                + "'" + df.format(date) + "' "
                + ") ON DUPLICATE KEY UPDATE "
                + "name=\"" + name + "\","
                + "status=" + status + ","
                + "lastupdate='" + df.format(date) + "' "
                + ";";
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }
    }

    public void enable(boolean enabled) throws Exception {

        if (enabled != status) {
            status = enabled;
            saveStatus();
            for (TriggerListener listener : listeners) {
                listener.onChangeStatus(status);
            }
        }
    }


}
