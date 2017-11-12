package com.server.webduino.core;

import com.server.webduino.DBObject;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.SensorFactory;
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

//import static com.server.webduino.core.sensors.SensorBase.Status_Offline;

public class Shield extends DBObject/*httpClient*/ {

    private static Logger LOGGER = Logger.getLogger(Shield.class.getName());

    protected int id;
    protected String MACAddress;
    protected String boardName;
    protected String description = "";
    protected boolean enabled = false;
    protected Date lastUpdate = new Date();
    protected List<SensorBase> sensors = new ArrayList<>();
    public URL url;
    public int port = 80;
    public String server;
    public int serverport;
    public String mqttserver;
    public int mqttport;

    public String swVersion = "";


    private ShieldSettings settings = new ShieldSettings();

    public static final String updateStatus_updated = "updated";
    public static final String updateStatus_updating = "updating";
    public static final String updateStatus_notUpdated = "notupdated";

    private String settingsStatus = updateStatus_notUpdated;
    private String sensorStatus = updateStatus_notUpdated;
    private SensorBase[] allSensors;


    public String getSettingStatus() {
        return settingsStatus;
    }

    public String getSensorStatus() {
        return sensorStatus;
    }

    protected String statusUpdatePath = "/sensorstatus";

    public Shield() {
    }

    public Shield(JSONObject json) throws Exception {
        fromJson(json);
    }


    public boolean postCommand(Command command) { //

        LOGGER.info("postCommand:");

        return Core.publish("fromServer/shield/" + MACAddress + "/command", command.getJSON().toString());
    }

    public boolean requestSensorStatusUpdate() { //

        LOGGER.info("requestStatusUpdate:");
        sensorStatus = updateStatus_updating;
        return Core.publish("fromServer/shield/" + MACAddress + "/updatesensorstatusrequest", "requestSensorsStatusUpdate");
    }

    public boolean requestReboot() { //

        LOGGER.info("requestStatusUpdate:");
        sensorStatus = updateStatus_updating;
        return Core.publish("fromServer/shield/" + MACAddress + "/reboot", "immediate");
    }

    public boolean requestSettingUpdate() { //

        LOGGER.info("requestSettingUpdate:");

        settingsStatus = updateStatus_updating;

        return Core.publish("fromServer/shield/" + MACAddress + "/updatesettingstatusrequest", "");
    }

    protected httpClient.Result call(String method, String param, String path) {

        LOGGER.info("call: " + method + "," + param + "," + path);
        LOGGER.info("url: " + url.toString());

        httpClient client = new httpClient();
        httpClient.Result result = null;
        if (method.equals("GET")) {
            result = client.callGet(param, path, url);
        } else if (method.equals("POST")) {
            result = client.callPost(param, path, url);
        }

        LOGGER.info("end call");
        return result;
    }


    public boolean settingsFromJSON(JSONObject json) {
        try {
            if (json.has("MAC"))
                MACAddress = json.getString("MAC");
            if (json.has("shieldname"))
                boardName = json.getString("shieldname");
            if (json.has("localport"))
                port = json.getInt("localport");
            if (json.has("server"))
                server = json.getString("server");
            if (json.has("serverport"))
                serverport = json.getInt("serverport");
            if (json.has("mqttserver"))
                mqttserver = json.getString("mqttserver");
            if (json.has("mqttport"))
                mqttport = json.getInt("mqttport");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            return false;
        }
        return true;
    }

    public SensorBase sensorFromJSON(JSONObject json) {


        SensorBase sensor = null;
        try {

            if (json.has("addr") && json.has("name") && json.has("type")
                    && json.has("enabled") && json.has("pin")) {
                String subaddress, name, type, pin;
                Boolean enabled;
                subaddress = json.getString("addr");
                name = json.getString("name");
                type = json.getString("type");
                enabled = json.getBoolean("enabled");
                pin = json.getString("pin");

                int sensorid = 0;
                if (json.has("sensorid")) {
                    sensorid = json.getInt("sensorid");
                }
                SensorFactory factory = new SensorFactory();
                sensor = factory.createSensor(type, description, name, subaddress, sensorid, 0/*shieldid*/, pin, enabled);

                if (json.has("childsensors")) {
                    JSONArray children = json.getJSONArray("childsensors");
                    for (int i = 0; i < children.length(); i++) {
                        JSONObject childjson = children.getJSONObject(i);
                        SensorBase child = sensorFromJSON(childjson);
                        sensor.addChildSensor(child);
                    }
                }
            }

        } catch (JSONException e1) {
            e1.printStackTrace();
            return null;
        }

        return sensor;
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
        if (json.has("port"))
            port = json.getInt("port");
        if (json.has("description"))
            description = json.getString("description");
        if (json.has("enabled"))
            enabled = json.getBoolean("enabled");
        if (json.has("server"))
            server = json.getString("server");
        if (json.has("serverport"))
            serverport = json.getInt("serverport");
        if (json.has("mqttserver"))
            mqttserver = json.getString("mqttserver");
        if (json.has("mqttport"))
            mqttport = json.getInt("mqttport");

        if (json.has("sensors")) {
            JSONArray jsonArray = json.getJSONArray("sensors");
            sensors = getSensors(jsonArray);
        }
    }


    private List<SensorBase> getSensors(JSONArray jsonArray) throws Exception {

        List<SensorBase> sensors = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject j = null;
                j = jsonArray.getJSONObject(i);
                SensorFactory factory = new SensorFactory();
                SensorBase sensor = factory.fromJson(j);
                if (j.has("childsensors")) {
                    sensor.childSensors = getSensors(j.getJSONArray("childsensors"));
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
            json.put("port", port);
            json.put("enabled", enabled);
            if (boardName != null)
                json.put("shieldname", boardName);
            if (description != null)
                json.put("description", description);
            if (swVersion != null)
                json.put("swversion", swVersion);
            if (server != null)
                json.put("server", server);
            json.put("serverport", serverport);
            if (mqttserver != null)
                json.put("mqttserver", mqttserver);
            json.put("mqttport", mqttport);
            if (MACAddress != null)
                json.put("macaddress", MACAddress);
            if (url != null)
                json.put("url", url);

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
            for (SensorBase child : sensor.childSensors) {
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
        if (json.has("sensors")) {
            JSONArray jsonArray = null;
            try {
                if (json.has("swversion")) {
                    swVersion = json.getString("swversion");
                }

                jsonArray = json.getJSONArray("sensors");
                updateSensors(jsonArray);
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
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

                    if (json.has("childsensors")) {
                        JSONArray jsonChildSensorArray = json.getJSONArray("childsensors");
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

    private void deleteMissingSensor(Connection conn, List<SensorBase> updatedSensors) {

        for (SensorBase sensor : sensors) {
            boolean sensorFound = false;
            for (SensorBase updSensor : updatedSensors) {
                if (updSensor.getId() == sensor.getId()) {
                    sensorFound = true;
                    break;
                }
            }
            if (!sensorFound) {
                deleteSensor(conn, sensor);
            }
        }
    }

    private void deleteSensor(Connection conn, SensorBase sensor) {
        try {
            Statement stmt = conn.createStatement();
            String sql = "DELETE FROM sensors WHERE id=" + sensor.getId() + ";";
            stmt.executeUpdate(sql);
            stmt.close();

            for (SensorBase child : sensor.childSensors) {
                deleteSensor(conn, child);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int writeSensor(Connection conn, SensorBase sensor, int shieldid, int parentid) {

        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            String sql = "INSERT INTO sensors (shieldid, parentid, type, subaddress, name, enabled, pin)" +
                    " VALUES ("
                    + "\"" + shieldid + "\","
                    + "\"" + parentid + "\","
                    + "\"" + sensor.getType() + "\","
                    + "\"" + sensor.getSubaddress() + "\","
                    + "\"" + sensor.getName() + "\","
                    + Core.boolToString(sensor.getEnabled()) + ","
                    + "\"" + sensor.getPin() + "\") " +
                    "ON DUPLICATE KEY UPDATE "
                    + "shieldid=\"" + shieldid + "\","
                    + "parentid=\"" + parentid + "\","
                    + "type=\"" + sensor.getType() + "\","
                    + "subaddress=\"" + sensor.getSubaddress() + "\","
                    + "name=\"" + sensor.getName() + "\","
                    + "enabled=" + Core.boolToString(sensor.getEnabled()) + ","
                    + "pin=\"" + sensor.getPin() + "\""
                    + ";";
            stmt.executeUpdate(sql);
            stmt.close();

            stmt = conn.createStatement();
            String query = "SELECT * FROM sensors WHERE shieldid=\"" + shieldid + "\"" +
                    " AND parentid=\"" + parentid + "\"" +
                    " AND subaddress=\"" + sensor.getSubaddress() + "\"";
            ResultSet rs = stmt.executeQuery(query);
            int lastid = -1;
            if (rs.next()) {
                lastid = rs.getInt("id");
            }
            rs.close();
            stmt.close();

            sensor.setId(lastid);
            for (SensorBase child : sensor.childSensors) {
                writeSensor(conn, child, shieldid, sensor.getId());
            }

            return lastid;

        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        //return -1;
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


    public Shield saveSettings(JSONObject json) {

        Shield updatedShield = new Shield();
        updatedShield.id = id;
        updatedShield.settingsFromJSON(json);

        int affectedRows = 0;
        try {

            if (json.has("sensors")) {
                JSONArray jsonArray = json.getJSONArray("sensors");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject sensorjson = jsonArray.getJSONObject(i);
                    SensorBase sensor = updatedShield.sensorFromJSON(sensorjson);
                    updatedShield.sensors.add(sensor);
                }
            }

            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            Statement stmt = conn.createStatement();
            String sql = "UPDATE shields SET boardname=\"" + updatedShield.boardName + "\", " +
                    " description=\"" + updatedShield.description + "\", " +
                    " enabled=" + Core.boolToString(enabled) + "," +
                    " server=\"" + updatedShield.server + "\", " +
                    " serverport=" + updatedShield.serverport + ", " +
                    " mqttserver=\"" + updatedShield.mqttserver + "\", " +
                    " mqttport=" + updatedShield.mqttport +
                    " WHERE id=" + updatedShield.id + ";";
            affectedRows = stmt.executeUpdate(sql);
            stmt.close();

            deleteMissingSensor(conn, updatedShield.sensors);

            for (SensorBase sensor : updatedShield.sensors) {
                writeSensor(conn, sensor, updatedShield.id, 0);
            }

            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            LOGGER.severe(se.toString());
            return null;
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            LOGGER.severe(e.toString());
            return null;
        }

        return updatedShield;
    }

    public boolean sendRestartCommand(JSONObject json) {
        return Core.publish("fromServer/shield/" + MACAddress + "/reboot", "");
        //return false;
    }

    @Override
    public void write(Connection conn) throws Exception {

        DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "INSERT INTO shields (id, name, enabled, description, port, macaddress, lastupdate, server, serverport, mqttserver, mqttport)" +
                " VALUES ("
                + id + ","
                + "\"" + boardName + "\","
                + Core.boolToString(enabled) + ","
                + "\"" + description + "\","
                + port + ","
                + "\"" + MACAddress + "\","
                + "'" + tf.format(lastUpdate) + "',"
                + "\"" + server + "\","
                + serverport + ","
                + "\"" + mqttserver + "\","
                + mqttport
                + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "name=\"" + boardName + "\","
                + "enabled=" + Core.boolToString(enabled) + ","
                + "description=\"" + description + "\","
                + "port=" + port + ","
                + "macaddress=\"" + MACAddress + "\","
                + "lastupdate='" + tf.format(lastUpdate) + "',"
                + "server=\"" + server + "\","
                + "serverport=" + serverport + ","
                + "mqttserver=\"" + mqttserver + "\","
                + "mqttport=" + mqttport + ";";

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
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM shields WHERE id=" + id;
        stmt.executeUpdate(sql);
    }

    public List<SensorBase> getAllSensors() {

        List<SensorBase> list = new ArrayList<>();
        for (SensorBase sensor: sensors) {
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
