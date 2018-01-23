package com.server.webduino.core.sensors;

import com.server.webduino.core.Core;
import com.server.webduino.core.datalog.DataLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class HeaterDataLog extends DataLog {

    protected String status = "";
    protected double localTemperature;
    protected int activeScenario;
    protected int activeTimeIntervals;
    protected boolean releStatus;
    protected double remoteTemperature;
    protected double targetTemperature;
    private int sensorid;


    public HeaterDataLog(int sensorid) {
        super();
        this.sensorid = sensorid;
    }

    public String getSQLInsert(String event, Object object) {

        HeaterActuator heaterActuator = (HeaterActuator) object;
        String sql;

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

        sql = "INSERT INTO heaterdatalog (date, sensorid, relestatus, status, temperature, targettemperature, actionid) " +
                " VALUES (" +
                getStrDate() + ",'" +
                //heaterActuator.getId() + "'," +
                sensorid + "'," +
                heaterActuator.releStatus + ",'" +
                heaterActuator.getStatus() + "'," +
                heaterActuator.getTemperature() + "," +
                heaterActuator.targetTemperature + "," +
                heaterActuator.actionId +
                ");";
        return sql;
    }

    @Override
    public JSONObject toJson() {
        try {
            JSONObject json = new JSONObject();
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            if (date != null) {

                json.put("date", df.format(date));
                //json.put("local", localTemperature);
                json.put("remotetemperature", remoteTemperature);
                json.put("targettemperature", targetTemperature);
                json.put("relestatus", releStatus);
                //json.put("status", status);
                json.put("program", activeScenario);
                json.put("timerange", activeTimeIntervals);
                return json;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ArrayList<DataLog> getDataLog(int id, Date startDate, Date endDate) {

        ArrayList<DataLog> list = new ArrayList<DataLog>();
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();

            if (endDate == null)
                endDate = Core.getDate();
            if (startDate == null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(endDate);
                cal.add(Calendar.DAY_OF_MONTH, -3);
                startDate = cal.getTime();
            }

            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String start = df.format(startDate);
            String end = df.format(endDate);

            String sql;
            sql = "SELECT * FROM heatercommanddatalog WHERE actuatorid = " + id + " AND date BETWEEN '" + start + "' AND '" + end + "' ORDER BY date ASC";

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                //SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.");
                date = df.parse(String.valueOf(rs.getTimestamp("date")));
                releStatus = rs.getBoolean("relestatus");
                status = rs.getString("status");
                localTemperature = rs.getDouble("localtemperature");
                remoteTemperature = rs.getDouble("remotetemperature");
                targetTemperature = rs.getDouble("targettemperature");
                activeScenario = rs.getInt("activeprogram");
                activeTimeIntervals = rs.getInt("activetimerange");
                list.add(this);
            }
            // Clean-up environment
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return list;
    }
}
