package com.server.webduino.core.datalog;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.commands.IRActuatorCommand;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class IRCommandDataLog extends CommandDataLog {

    int id;
    Date date;
    int shieldid;
    int actuatorid;
    int duration;
    double target;
    double temperature;
    String command;
    Date enddate;
    Boolean success;
    int actionid;

    public IRCommandDataLog() {
        super("commanddatalog");
    }

    @Override
    public String getSQLInsert(String event, Object object) {

        IRActuatorCommand heaterCommand = (IRActuatorCommand) object;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql;
        sql = "INSERT INTO " + tableName + " (command, shieldid, actuatorid, uuid) VALUES ("
                + "'" + heaterCommand.command + "'"
                + "," + heaterCommand.shieldid
                + "," + heaterCommand.actuatorid
                + ",'" + heaterCommand.uuid + "'"
                + ");";
        return sql;
    }

    /*@Override
    public DataLog.DataLogValues getDataLogValue(int id, Date startDate, Date endDate) {
        try {
            //Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            String sql;
            sql = "SELECT * FROM " + tableName + " WHERE actuatorid=" + id + ";";

            ResultSet resultSet = stmt.executeQuery(sql);
            List<DataLog> list = new ArrayList<>();
            while (resultSet.next()) {
                IRCommandDataLog dataLog = new IRCommandDataLog();
                dataLog.fromResulSet(conn, resultSet);
                list.add(dataLog);
            }
            resultSet.close();
            stmt.close();
            conn.close();
            return list;
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }*/

    private void fromResulSet(Connection conn, ResultSet datalogResultSet) throws Exception {
        //WebduinoSystemScenario scenario = new WebduinoSystemScenario();
        this.id = datalogResultSet.getInt("id");
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.date = df.parse(String.valueOf(datalogResultSet.getTimestamp("date")));
        this.shieldid = datalogResultSet.getInt("shieldid");
        this.actuatorid = datalogResultSet.getInt("sensorid");
        this.duration = datalogResultSet.getInt("duration");
        this.target = datalogResultSet.getDouble("target");
        this.temperature = datalogResultSet.getDouble("temperature");
        this.command = datalogResultSet.getString("command");
        this.enddate = df.parse(String.valueOf(datalogResultSet.getTimestamp("enddate")));
        this.success = datalogResultSet.getBoolean("success");
        this.actionid = datalogResultSet.getInt("actionid");
    }

    /*@Override
    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            json.put("date", df.format(date));
            json.put("shieldid", shieldid);
            json.put("sensorid", actuatorid);
            json.put("duration", duration);
            json.put("target", target);
            json.put("temperature", temperature);
            json.put("command", command);
            json.put("enddate", df.format(enddate));
            json.put("success", success);
            json.put("actionid", actionid);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }*/
}
