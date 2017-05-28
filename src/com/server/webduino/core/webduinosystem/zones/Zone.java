package com.server.webduino.core.webduinosystem.zones;

import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.WebduinoTrigger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class Zone implements SensorBase.SensorListener {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    public interface WebduinoZoneListener {
        void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature);
        void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus);
    }
    protected List<WebduinoZoneListener> listeners = new ArrayList<WebduinoZoneListener>();
    public void addListener(WebduinoZoneListener toAdd) {
        listeners.add(toAdd);
    }

    private int id;
    private String name;
    protected List<ZoneSensor> zoneSensors = new ArrayList<>();

    private double temperature = 0.0;

    public Zone(int id, String name) {
        this.id = id;
        this.name = name;
        readZoneSensors(id);
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addSensorListeners() {
        for(ZoneSensor zonesensor: zoneSensors) {
            SensorBase sensor = Core.getSensorFromId(zonesensor.getSensorId());
            if (sensor != null)
                sensor.addListener(this);
        }
    }
    public void clearSensorListeners() {
        for(ZoneSensor zonesensor: zoneSensors) {
            SensorBase sensor = Core.getSensorFromId(zonesensor.getSensorId());
            sensor.deleteListener(this);
        }
    }

    @Override
    public void onChangeTemperature(int sensorId, double temperature, double oldtemperature) {
        for(WebduinoZoneListener listener: listeners) {
            listener.onTemperatureChange(id,temperature,oldtemperature);
        }
        this.temperature = temperature;
    }

    @Override
    public void changeAvTemperature(int sensorId, double avTemperature) {

    }

    @Override
    public void changeOnlineStatus(boolean online) {

    }

    @Override
    public void changeOnlineStatus(int sensorId, boolean online) {

    }

    @Override
    public void changeDoorStatus(int sensorId, boolean open, boolean oldOpen) {

        for(WebduinoZoneListener listener: listeners) {
            listener.onDoorStatusChange(id,open,oldOpen);
        }
        this.temperature = temperature;
    }

    public void readZoneSensors(int zoneid) {

        LOGGER.info(" readZoneSensors Security Zone Sensors");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // readZoneSensors zone sensors
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM zonesensors WHERE zoneid=" + zoneid;
            ResultSet rs = stmt.executeQuery(sql);
            zoneSensors.clear();
            while (rs.next()) {
                ZoneSensor zoneSensor = new ZoneSensor();
                zoneSensor.setId(rs.getInt("id"));
                zoneSensor.setSensorId(rs.getInt("sensorid"));
                zoneSensors.add(zoneSensor);
            }
            rs.close();
            stmt.close();
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
    }

    public void init() {
    }

    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            JSONArray jsonArray = new JSONArray();
            for(ZoneSensor zonesensor: zoneSensors) {
                JSONObject jsonObject = new JSONObject();
                SensorBase sensor = Core.getSensorFromId(zonesensor.getSensorId());
                jsonObject.put("id",zonesensor.getId());
                jsonObject.put("name",sensor.getName());
                //jsonObject.put("name",sensor.getS);
                jsonArray.put(jsonObject);
            }
            json.put("zonesensors", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
