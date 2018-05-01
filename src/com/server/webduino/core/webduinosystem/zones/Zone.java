package com.server.webduino.core.webduinosystem.zones;

import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.TemperatureSensor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class Zone extends DBObject implements SensorBase.SensorListener, TemperatureSensor.TemperatureSensorListener {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    public interface WebduinoZoneListener {
        void onUpdateTemperature(int zoneId, double newTemperature, double oldTemperature);
        void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus);
    }
    protected List<WebduinoZoneListener> listeners = new CopyOnWriteArrayList<>();
    public void addListener(WebduinoZoneListener toAdd) {
        listeners.add(toAdd);
    }

    public void removeListener(WebduinoZoneListener toRemove) {

        Iterator<WebduinoZoneListener> it = listeners.iterator();
        while(it.hasNext()){
            WebduinoZoneListener value = it.next();
            //System.out.println("List Value:"+value);
            if(value == toRemove) listeners.remove(value);
        }

    }

    public int id;
    private String name;
    private String type;
    protected List<ZoneSensor> zoneSensors = new ArrayList<>();

    private double temperature = 0.0;
    private boolean doorStatusOpen = false;
    public Date lastTemperatureUpdate = null;

    public Zone(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
        readZoneSensors(id);
    }

    public Zone(JSONObject json) throws JSONException {
        fromJson(json);
    }

    public void requestSensorStatusUpdate() {

        LOGGER.info("requestAsyncAllSensorStatusUpdate:");

        for (ZoneSensor zoneSensor : zoneSensors) {

            SensorBase sensor = Core.getSensorFromId(zoneSensor.getSensorId());
            if (sensor != null) {
                sensor.requestAsyncSensorStatusUpdate();
                /*Shield shield = Core.getShieldFromId(sensor.getShieldId());
                if (shield != null) {
                    shield.requestAsyncAllSensorStatusUpdate();
                }*/
            }
        }
    }

    public void fromJson(JSONObject json) throws JSONException {

            if (json.has("id"))
                id = json. getInt("id");
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
    }


    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM zones WHERE id=" + id;
        stmt.executeUpdate(sql);
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

    @Override
    public void write(Connection conn) throws SQLException {

            //Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
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

                Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                ResultSet rs = stmt.getGeneratedKeys();
                if (rs.next()) {
                    sensor.id = rs.getInt(1);
                }

                stmt.close();
            }
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
            sensor.removeListener(this);
        }
    }

    public ZoneSensor zoneSensorFromId(int zonesensorid) {
        for(ZoneSensor zonesensor: zoneSensors) {
            if (zonesensor.getSensorId() == zonesensorid)
                return zonesensor;
        }
        return null;
    }

    @Override
    public void onUpdateTemperature(int sensorId, double temperature, double oldtemperature) {

        lastTemperatureUpdate = Core.getDate();
        for(WebduinoZoneListener listener: listeners) {
            listener.onUpdateTemperature(id,temperature,oldtemperature);
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
    public void onChangeStatus(String newStatus, String oldStatus) {

    }

    @Override
    public void changeDoorStatus(int sensorId, boolean open, boolean oldOpen) {

        for(WebduinoZoneListener listener: listeners) {
            listener.onDoorStatusChange(id,open,oldOpen);
        }
        this.doorStatusOpen = open;
    }

    @Override
    public void changeValue(double value) {

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

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);

            if (lastTemperatureUpdate != null) {
                SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
                json.put("lasttemperatureupdate", df.format(lastTemperatureUpdate));
                json.put("temperature", temperature);
            }

            JSONArray jsonArray = new JSONArray();
            for(ZoneSensor zonesensor: zoneSensors) {
                JSONObject jsonObject = new JSONObject();
                SensorBase sensor = Core.getSensorFromId(zonesensor.getSensorId());
                if (sensor != null) {
                    jsonObject.put("id", zonesensor.getId());
                    jsonObject.put("name", sensor.getName());
                    jsonObject.put("sensorid", sensor.getId());
                    jsonObject.put("type", sensor.getType());
                    jsonArray.put(jsonObject);
                }
            }
            json.put("zonesensors", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
