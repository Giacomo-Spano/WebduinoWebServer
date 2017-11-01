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
    private String type;
    protected List<ZoneSensor> zoneSensors = new ArrayList<>();

    private double temperature = 0.0;
    private boolean doorStatusOpen = false;

    public Zone(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        readZoneSensors(id);
    }

    public Zone(JSONObject json) {
        fromJson(json);
    }

    public boolean fromJson(JSONObject json) {

        try {
            if (json.has("id"))
                id = json.getInt("id");
            if (json.has("name"))
                name = json.getString("name");
            if (json.has("type"))
                type = json.getString("type");
            if (json.has("zonesensors")) {
                JSONArray sensors = json.getJSONArray("zonesensors");
                for (int i = 0; i < sensors.length(); i++) {
                    JSONObject jsonObject = sensors.getJSONObject(i);
                    ZoneSensor zoneSensor = new ZoneSensor();
                    if (jsonObject.has("id"))
                        zoneSensor.id = jsonObject.getInt("id");
                    if (jsonObject.has("name"))
                        zoneSensor.name = jsonObject.getString("name");
                    if (jsonObject.has("sensorid"))
                        zoneSensor.setSensorId(jsonObject.getInt("sensorid"));
                    zoneSensors.add(zoneSensor);
                }
            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean write() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = null;

            stmt = conn.createStatement();
            String sql = "INSERT INTO zones (id, name, type)" +
                    " VALUES ("
                    + id + ","
                    + "\"" + name + "\","
                    + "\"" + type + "\" ) " +
                    "ON DUPLICATE KEY UPDATE "
                    + "name=\"" + name + "\","
                    + "type=\"" + type + "\";";
            stmt.executeUpdate(sql);
            stmt.close();

            for (ZoneSensor sensor : zoneSensors) {
                stmt = conn.createStatement();
                sql = "INSERT INTO zonesensors (id, sensorid, zoneid, name)" +
                        " VALUES ("
                        + sensor.id + ","
                        + sensor.getSensorId() + ","
                        + id + "," //zoneid
                        + "\"" + sensor.name + "\" ) " +
                        "ON DUPLICATE KEY UPDATE "
                        + "sensorid=" + sensor.id + ","
                        + "zoneid=" + id + ","
                        + "sensorid=" + sensor.getSensorId() + ";";

                stmt.executeUpdate(sql);
                stmt.close();
            }
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
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

    public double getTemperature() {
        return temperature;
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
        this.doorStatusOpen = open;
    }

    public boolean getDoorStatusOpen() {
        return doorStatusOpen;
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
            ResultSet zoneSensorResultSet = stmt.executeQuery(sql);
            zoneSensors.clear();
            while (zoneSensorResultSet.next()) {
                ZoneSensor zoneSensor = new ZoneSensor();
                zoneSensor.setId(zoneSensorResultSet.getInt("id"));
                zoneSensor.setSensorId(zoneSensorResultSet.getInt("sensorid"));
                zoneSensor.name = zoneSensorResultSet.getString("name");
                zoneSensors.add(zoneSensor);
            }
            zoneSensorResultSet.close();
            stmt.close();
            zoneSensorResultSet.close();
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
                jsonObject.put("sensorid",sensor.getId());
                jsonObject.put("type",sensor.getType());
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
