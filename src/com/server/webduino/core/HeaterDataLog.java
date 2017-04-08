package com.server.webduino.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HeaterDataLog extends DataLog {
    //public Date date = new Date();
    //public Date time = new Date();
    protected String status = "";
    protected double localTemperature;
    protected int activeProgram;
    protected int activeTimerange;
    protected boolean releStatus;
    protected double remoteTemperature;
    protected double targetTemperature;


    @Override
    public String getSQLInsert(String event, SensorBase sensor) {

        HeaterActuator heaterActuator = (HeaterActuator) sensor;
        String sql;

        sql = "INSERT INTO heaterdatalog (id, date, event, relestatus, status, localtemperature, remotetemperature, targettemperature, activeprogram, activetimerange, activesensor) " +
                " VALUES (" + heaterActuator.id + ", " +
                getStrDate() + ",'" +
                event + "'," +
                heaterActuator.releStatus + ",'" +
                heaterActuator.getStatus() + "'," +
                heaterActuator.getAvTemperature() + "," +
                heaterActuator.getRemoteTemperature() + "," +
                heaterActuator.targetTemperature + "," +
                heaterActuator.activeProgramID + "," +
                heaterActuator.activeTimeRangeID + "," +
                heaterActuator.activeSensorID + "" +
                ");";
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
                HeaterDataLog data = new HeaterDataLog();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.");
                data.date = df.parse(String.valueOf(rs.getTimestamp("date")));
                data.releStatus = rs.getBoolean("relestatus");
                data.status = rs.getString("status");
                data.localTemperature = rs.getDouble("localtemperature");
                data.remoteTemperature = rs.getDouble("remotetemperature");
                data.targetTemperature = rs.getDouble("targettemperature");
                data.activeProgram = rs.getInt("activeprogram");
                data.activeTimerange = rs.getInt("activetimerange");
                list.add(data);
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
