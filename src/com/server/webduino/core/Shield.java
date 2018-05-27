package com.server.webduino.core;

import com.server.webduino.DBObject;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.SensorFactory;
import com.server.webduino.core.sensors.commands.Command;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


public class Shield extends DBObject {

    private static Logger LOGGER = Logger.getLogger(Shield.class.getName());

    protected int id;
    public String MACAddress;
    protected String boardName;
    protected String description = "";
    protected boolean enabled = false;
    protected Date lastUpdate = new Date();
    protected List<SensorBase> sensors = new ArrayList<>();

    public String swVersion = "";

    protected boolean online = false;

    private ShieldSettings settings = new ShieldSettings();

    public static final String updateStatus_updated = "updated";
    public static final String updateStatus_updating = "updating";
    public static final String updateStatus_notUpdated = "notupdated";

    private String settingsStatus = updateStatus_notUpdated;
    private String sensorStatus = updateStatus_notUpdated;

    public String getSettingStatus() {
        return settingsStatus;
    }

    public String getSensorStatus() {
        return sensorStatus;
    }

    public Shield() {
    }

    public Shield(JSONObject json) throws Exception {
        fromJson(json);
    }

    // invia una richiesta di aggiornamento per il singolo sensore
    public void requestAsyncAllSensorStatusUpdate() {

        LOGGER.info("requestSensorStatus:");
        sensorStatus = updateStatus_updating;

        for (SensorBase sensor : sensors) {
            sensor.requestAsyncSensorStatusUpdate();
        }
    }

    public boolean requestReboot() { //

        LOGGER.info("requestReboot:");
        sensorStatus = updateStatus_updating;
        return Core.publish("fromServer/shield/" + MACAddress + "/reboot", "immediate");
    }

    public boolean requestResetSettings() { //

        LOGGER.info("requestResetSettings:");
        sensorStatus = updateStatus_updating;
        return Core.publish("fromServer/shield/" + MACAddress + "/resetsettings", "immediate");
    }



    public boolean requestSettingUpdate() { //

        LOGGER.info("requestSettingUpdate:");

        settingsStatus = updateStatus_updating;

        return Core.publish("fromServer/shield/" + MACAddress + "/updatesettingstatusrequest", "");
    }

    public void fromJson(JSONObject json) throws Exception {

        if (json.has("shieldid"))
            id = json.getInt("shieldid");
        Date date = Core.getDate();
        lastUpdate = date;
        if (json.has("swversion"))
            swVersion = json.getString("swversion");
        if (json.has("macaddress"))
            MACAddress = json.getString("macaddress");
        if (json.has("name"))
            boardName = json.getString("name");
        if (json.has("description"))
            description = json.getString("description");
        if (json.has("enabled"))
            enabled = json.getBoolean("enabled");
        if (json.has("sensors")) {
            JSONArray jsonArray = json.getJSONArray("sensors");
            sensors = getSensors(jsonArray);
        }
    }

    public void updateFromJson(JSONObject json) throws JSONException {

        Date date = Core.getDate();
        lastUpdate = date;
        if (json.has("swversion"))
            swVersion = json.getString("swversion");
    }

    private List<SensorBase> getSensors(JSONArray jsonArray) throws Exception {

        List<SensorBase> sensors = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject j = null;
            j = jsonArray.getJSONObject(i);
            SensorFactory factory = new SensorFactory();
            SensorBase sensor = factory.fromJson(j);
            if (j.has("childsensors")) {
                sensor.setChildSensors(getSensors(j.getJSONArray("childsensors")));
            }
            sensors.add(sensor);
        }
        return sensors;
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("shieldid", id);
            json.put("lastupdate", Core.getStrLastUpdate(lastUpdate));
            json.put("enabled", enabled);
            if (boardName != null)
                json.put("shieldname", boardName);
            if (description != null)
                json.put("description", description);
            if (swVersion != null)
                json.put("swversion", swVersion);
            if (MACAddress != null)
                json.put("macaddress", MACAddress);

            JSONArray jarray = new JSONArray();
            for (SensorBase sensor : sensors) {
                if (sensor != null)
                    jarray.put(sensor.toJson());
            }
            json.put("sensors", jarray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public boolean updateSettings(JSONObject json) {

        Date date = Core.getDate();

        if (settings.updateFromJson(date, json)) {
            settingsStatus = updateStatus_updated;
            return true;
        } else {
            settingsStatus = updateStatus_notUpdated;
            return false;
        }
    }

    public SensorBase getFromSubaddress(String subaddress) {

        for (SensorBase sensor : sensors) {
            if (sensor.getSubaddress().equals(subaddress))
                return sensor;
            for (SensorBase child : sensor.getChildSensors()) {
                if (child.getSubaddress().equals(subaddress))
                    return child;
            }
        }
        return null;
    }

    public SensorBase findSensorFromId(int id) {

        for (SensorBase sensor : sensors) {
            SensorBase s = sensor.findSensorFromId(id);
            if (s != null)
                return s;
        }
        return null;
    }

    boolean updateShieldStatus(JSONObject json) {

        try {
            if (json.has("swversion")) {
                swVersion = json.getString("swversion");
            }
            lastUpdate = Core.getDate();

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }

    }

    boolean updateSensors(JSONArray jsonArray) {

        Date date = Core.getDate();
        this.lastUpdate = date;

        for (int i = 0; i < jsonArray.length(); i++) {

            try {
                JSONObject json = jsonArray.getJSONObject(i);
                String subaddress = "";
                if (json.has("addr")) {
                    subaddress = json.getString("addr");
                }
                SensorBase sensor = getFromSubaddress(subaddress);
                if (sensor != null) {
                    sensor.updateFromJson(date, json);

                    if (json.has("children")) {
                        JSONArray jsonChildSensorArray = json.getJSONArray("children");
                        for (int k = 0; k < jsonChildSensorArray.length(); k++) {
                            JSONObject childSensor = jsonChildSensorArray.getJSONObject(k);
                            if (childSensor.has("addr")) {
                                subaddress = childSensor.getString("addr");
                                SensorBase child = getFromSubaddress(subaddress);
                                if (child != null)
                                    child.updateFromJson(date, childSensor);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
                sensorStatus = updateStatus_notUpdated;
            }
        }
        sensorStatus = updateStatus_updated;
        return true;
    }


    public JSONObject getShieldSettingJson() {
        return settings.getJson();
    }

    public JSONObject getShieldSensorsJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("shieldid", id);

            JSONArray jarray = new JSONArray();
            for (SensorBase sensor : sensors) {
                if (sensor != null)
                    jarray.put(sensor.toJson());
            }

            json.put("sensors", jarray);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    //update sensor name
    public boolean updateSensor(int id, String name) {


        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = null;
            stmt = conn.createStatement();

            String sql = "UPDATE sensors SET name='" + name + "' WHERE id=" + id;
            stmt.executeUpdate(sql);
            stmt.close();
            return true;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void write(Connection conn) throws Exception {

        DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "INSERT INTO shields (id, name, enabled, description, macaddress, lastupdate)" +
                " VALUES ("
                + id + ","
                + "\"" + boardName + "\","
                + Core.boolToString(enabled) + ","
                + "\"" + description + "\","
                + "\"" + MACAddress + "\","
                + "'" + tf.format(lastUpdate) + "'"
                + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "name=\"" + boardName + "\","
                + "enabled=" + Core.boolToString(enabled) + ","
                + "description=\"" + description + "\","
                + "macaddress=\"" + MACAddress + "\","
                + "lastupdate='" + tf.format(lastUpdate) + "';";

        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }

        if (sensors != null) {
            for (SensorBase sensor : sensors) {
                if (sensor.getShieldId() == 0)
                    sensor.setShieldId(id);
                if (sensor.getShieldId() != id) {
                    throw new Exception("sensor id error");
                }
                sensor.write(conn);
            }
        }
        deleteMissing(conn);
    }

    private void deleteMissing(Connection conn) throws SQLException {
        String sql;
        ResultSet rs;
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        sql = "SELECT * FROM sensors WHERE shieldid=" + id;
        rs = stmt.executeQuery(sql);

        List<Integer> toBeRemoved = new ArrayList<>();

        while (rs.next()) {
            int sensorid = rs.getInt(1);

            boolean sensorFound = false;
            for (SensorBase sensor : getAllSensors()) {
                if (sensor.getId() == sensorid) {
                    sensorFound = true;
                    break;
                }
            }
            if (!sensorFound) {
                toBeRemoved.add(sensorid);
            }
        }

        sql = "DELETE FROM sensors WHERE id in (";
        int count = 0;
        for (Integer id : toBeRemoved) {
            if (count++ > 0)
                sql += ", ";
            sql += id;
        }
        sql += ")";
        if (count > 0)
            stmt.executeUpdate(sql);
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM shields WHERE id=" + id;
        stmt.executeUpdate(sql);
    }

    public List<SensorBase> getAllSensors() {

        List<SensorBase> list = new ArrayList<>();
        for (SensorBase sensor : sensors) {
            list.add(sensor);

            List<SensorBase> childlist = sensor.getAllChildSensors();
            if (childlist != null) {
                for (SensorBase elem : childlist) {
                    list.add(elem);
                }
            }
        }
        return list;
    }
}
