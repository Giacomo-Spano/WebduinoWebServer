package com.server.webduino.core.datalog;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.HeaterActuator;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class ReleDataLog extends DataLog {
    protected String status = "";
    protected double localTemperature;
    protected int activeProgram;
    protected int activeTimerange;
    protected boolean releStatus;
    protected double remoteTemperature;
    protected double targetTemperature;

    private int sensorid;

    public ReleDataLog(int sensorid) {
        super();
        this.sensorid = sensorid;
    }


    @Override
    public String getSQLInsert(String event, Object object) {

        HeaterActuator heaterActuator = (HeaterActuator) object;
        String sql = "";

        /*sql = "INSERT INTO heaterdatalog (id, date, event, relestatus, status, localtemperature, remotetemperature, targettemperature, activeprogram, activetimerange, activesensor) " +
                " VALUES (" + heaterActuator.id + ", " +
                getStrDate() + ",'" +
                event + "'," +
                heaterActuator.releStatus + ",'" +
                heaterActuator.getDoorStatus() + "'," +
                heaterActuator.getAvTemperature() + "," +
                heaterActuator.getRemoteTemperature() + "," +
                heaterActuator.targetTemperature + "," +
                heaterActuator.activeProgramID + "," +
                heaterActuator.activeTimeRangeID + "," +
                heaterActuator.activeSensorID + "" +
                ");";*/
        return sql;
    }

    public JSONObject getJson() {
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
                json.put("program", activeProgram);
                json.put("timerange", activeTimerange);
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

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String start = dateFormat.format(startDate);
            String end = dateFormat.format(endDate);

            String sql;
            sql = "SELECT * FROM heaterdatalog WHERE id = " + id + " AND event='update' AND date BETWEEN '" + start + "' AND '" + end + "'" + "ORDER BY date ASC";

            ResultSet rs = stmt.executeQuery(sql);

            while (rs.next()) {
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.");
                date = df.parse(String.valueOf(rs.getTimestamp("date")));
                releStatus = rs.getBoolean("relestatus");
                status = rs.getString("status");
                localTemperature = rs.getDouble("localtemperature");
                remoteTemperature = rs.getDouble("remotetemperature");
                targetTemperature = rs.getDouble("targettemperature");
                activeProgram = rs.getInt("activeprogram");
                activeTimerange = rs.getInt("activetimerange");
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

    /*@Override
    DataLog getInterpolatedDataLog(Date t, DataLog dataA, DataLog dataB) {


        HeaterDataLog dlA = (HeaterDataLog) dataA, dlB = (HeaterDataLog) dataB;
        HeaterDataLog interpolatedDataLog = new HeaterDataLog();

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //DateFormat timeFormat = new SimpleDateFormat("");

        try {
            interpolatedDataLog.date = dateFormat.parse(dateFormat.format(t));
            //interpolatedDataLog.time = timeFormat.parse(timeFormat.format(t));
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }


        long xa = dataA.getDatetime().getTime(), xb = dataB.getDatetime().getTime(), x = t.getTime();
        if (xa == xb) {
            interpolatedDataLog.localTemperature = dlA.localTemperature;
            interpolatedDataLog.remoteTemperature = dlA.remoteTemperature;
            interpolatedDataLog.targetTemperature = dlA.targetTemperature;
            interpolatedDataLog.releStatus = dlA.releStatus;

        } else {
            interpolatedDataLog.localTemperature = dlA.localTemperature * (x - xb) / (xa - xb) - dlB.localTemperature * (x - xa) / (xa - xb);
            interpolatedDataLog.remoteTemperature = dlA.remoteTemperature * (x - xb) / (xa - xb) - dlB.remoteTemperature * (x - xa) / (xa - xb);
            interpolatedDataLog.targetTemperature = dlA.targetTemperature * (x - xb) / (xa - xb) - dlB.targetTemperature * (x - xa) / (xa - xb);
            interpolatedDataLog.releStatus = dlA.releStatus;
        }
        return interpolatedDataLog;
    }*/
}
