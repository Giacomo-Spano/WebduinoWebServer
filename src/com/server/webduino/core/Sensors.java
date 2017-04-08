package com.server.webduino.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Sensors implements Shields.ShieldsListener {

    interface SensorsListener {
        void updatedSensorValue(SensorBase sensor);
    }

    private static List<SensorBase> list = new ArrayList<>();
    protected List<SensorsListener> listeners = new ArrayList<>();

    public void addListener(SensorsListener toAdd) {
        listeners.add(toAdd);
    }

    public Sensors() {
        read();
    }

    public List<SensorBase> getLastSensorData() {
        return list;
    }

    public SensorBase getSensorFromId(int sensorId) {
        for (SensorBase sensor : list) {
            if (sensor.id == sensorId)
                return sensor;
        }
        return null;
    }

    public List<SensorBase> getSensorList() {

        return list;
    }


    public SensorBase getFromShieldIdandSubaddress(int shieldid, String subaddress) {
        for (SensorBase sensor: list) {
            if (sensor.subaddress.equals(subaddress) && sensor.shieldid == shieldid)
                return sensor;
        }
        return null;
    }


    boolean updateSensors(int shieldid, JSONArray jsonArray) {

        Date date = Core.getDate();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject json = null;
            try {
                json = jsonArray.getJSONObject(i);
                if (json.has("addr")) {
                    String subaddress = json.getString("addr");
                    SensorBase sensor = getFromShieldIdandSubaddress(shieldid, subaddress);
                    if (sensor != null)
                        sensor.updateFromJson(date,json);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        /*for (TemperatureSensor.TemperatureSensorListener listener : sensor.lis) {
            listener.changeAvTemperature(shieldid,subaddress,sensor.);
        }

        for (SensorsListener listener : listeners) {
            listener.updatedSensorValue(sensor);
        }*/

        return  true;
    }


    public void read() {

        //List<SensorBase> list = new ArrayList<>();
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM sensors";
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            while (rs.next()) {

                SensorBase sensor = null;
                if (true) {
                    sensor = new TemperatureSensor();
                }
                sensor.id = rs.getInt("id");
                sensor.shieldid = rs.getInt("shieldid");
                if (rs.getString("subaddress") != null)
                    sensor.subaddress = rs.getString("subaddress");
                if (rs.getString("name") != null)
                    sensor.name = rs.getString("name");
                if (rs.getString("type") != null)
                    sensor.type = rs.getString("type");
                list.add(sensor);
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
        //return list;
    }

    @Override
    public void addedActuator(Actuator actuator) {
    }

    @Override
    public void addedSensor(SensorBase sensor) {
        list.add(sensor);
    }

    @Override
    public void addedShield(Shield shield) {
    }

    @Override
    public void updatedActuator(Actuator actuator) {
    }

    @Override
    public void updatedSensor(SensorBase sensor) {
    }

    @Override
    public void updatedShield(Shield shield) {
    }


}
