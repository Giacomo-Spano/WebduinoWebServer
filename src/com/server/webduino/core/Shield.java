package com.server.webduino.core;

import com.server.webduino.core.sensors.Actuator;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.ActuatorCommand;
import com.sun.org.apache.xpath.internal.operations.Bool;
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
import java.util.logging.Level;
import java.util.logging.Logger;

//import static com.server.webduino.core.sensors.SensorBase.Status_Offline;

public class Shield extends httpClient {

    private static Logger LOGGER = Logger.getLogger(Shield.class.getName());

    protected int id;
    protected String MACAddress;
    protected String boardName;
    protected Date lastUpdate;
    protected List<SensorBase> sensors = new ArrayList<>();
    public URL url;
    public int port;
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


    public String getSettingStatus() {
        return settingsStatus;
    }

    public String getSensorStatus() {
        return sensorStatus;
    }

    protected String statusUpdatePath = "/sensorstatus";

    public Shield() {
    }

    public boolean postCommand(Command command) { //

        LOGGER.info("postCommand:");

        return Core.publish("fromServer/shield/" + MACAddress + "/command", command.getJSON().toString());
    }

    public boolean requestSensorStatusUpdate() { //

        LOGGER.info("requestStatusUpdate:");

        sensorStatus = updateStatus_updating;

        return Core.publish("fromServer/shield/" + MACAddress + "/updatesensorstatusrequest", "");
    }

    public boolean requestSettingUpdate() { //

        LOGGER.info("requestSettingUpdate:");

        settingsStatus = updateStatus_updating;

        return Core.publish("fromServer/shield/" + MACAddress + "/updatesettingstatusrequest", "");
    }

    protected Result call(String method, String param, String path) {

        LOGGER.info("call: " + method + "," + param + "," + path);
        LOGGER.info("url: " + url.toString());

        Result result = null;
        if (method.equals("GET")) {
            result = callGet(param, path, url);
        } else if (method.equals("POST")) {
            result = callPost(param, path, url);
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
                    sensorid =json.getInt("sensorid");
                }
                SensorFactory factory = new SensorFactory();
                sensor = factory.createSensor(type, name, subaddress, sensorid, 0/*shieldid*/, pin, enabled);

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

    public boolean FromJson(JSONObject json) {

        try {
            Date date = Core.getDate();
            lastUpdate = date;
            if (json.has("swversion"))
                swVersion = json.getString("swversion");
            if (json.has("MAC"))
                MACAddress = json.getString("MAC");
            if (json.has("shieldName"))
                boardName = json.getString("shieldName");
            if (json.has("localport"))
                port = json.getInt("localport");
            else
                port = 80;
            if (json.has("shieldName"))
                boardName = json.getString("shieldName");
            if (json.has("shieldName"))
                boardName = json.getString("shieldName");


            if (json.has("localIP")) {
                try {
                    url = new URL("http://" + json.getString("localIP"));
                    if (url.equals(new URL("http://0.0.0.0"))) {
                        LOGGER.info("url error: " + url.toString());
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.info("url error: " + e.toString());
                    return false;
                }
            }
            if (json.has("sensors")) {
                JSONArray jsonArray = json.getJSONArray("sensors");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject j = jsonArray.getJSONObject(i);
                    if (j.has("type")) {
                        String type = j.getString("type");
                        String name = "";
                        String subaddress = "";
                        String pin = "";
                        boolean enabled = true;
                        if (j.has("name"))
                            name = j.getString("name");
                        if (j.has("addr"))
                            subaddress = j.getString("addr");
                        if (j.has("pin"))
                            pin = j.getString("pin");
                        if (j.has("enabled"))
                            enabled = j.getBoolean("enabled");

                        SensorBase sensor = SensorFactory.createSensor(type, name, subaddress, 0, 0, pin, enabled);
                        if (sensor == null) {
                            continue;
                        } else {

                            if (j.has("childsensors")) {
                                JSONArray tempSensorArray = j.getJSONArray("childsensors");
                                for (int k = 0; k < tempSensorArray.length(); k++) {


                                    String childSubaddress = "";
                                    if (tempSensorArray.getJSONObject(k).has("addr"))
                                        childSubaddress = tempSensorArray.getJSONObject(k).getString("addr");

                                    String childName = "";
                                    if (tempSensorArray.getJSONObject(k).has("name"))
                                        childName = tempSensorArray.getJSONObject(k).getString("name");

                                    if (tempSensorArray.getJSONObject(k).has("id"))
                                        id = tempSensorArray.getJSONObject(k).getInt("id");

                                    if (tempSensorArray.getJSONObject(k).has("enabled"))
                                        enabled = tempSensorArray.getJSONObject(k).getBoolean("enabled");


                                    SensorBase childSensor = SensorFactory.createSensor("temperature", childName, childSubaddress, id, 0, "", enabled);
                                    if (childSensor != null)
                                        sensor.addChildSensor(childSensor);
                                }
                            }
                        }
                        sensors.add(sensor);
                    }
                }
            }
            /*if (json.has("actuators")) {
                JSONArray jsonArray = json.getJSONArray("actuators");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject j = jsonArray.getJSONObject(i);
                    if (j.has("type")) {
                        String type = j.getString("type");
                        Actuator actuator;
                        if (type.equals("heater")) {
                            actuator = (Actuator) new HeaterActuator();
                        } else if (type.equals("current")) {
                            actuator = (Actuator) new ReleActuator();
                        } else {
                            continue;
                        }
                        if (j.has("name"))
                            actuator.name = j.getString("name");
                        if (j.has("addr"))
                            actuator.subaddress = j.getString("addr");
                        if (j.has("type"))
                            actuator.type = j.getString("type");
                        actuators.add(actuator);
                    }
                }
            }*/

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            return false;
        }
        return true;
    }


    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("shieldid", id);
            json.put("lastupdate", Core.getStrLastUpdate(lastUpdate));
            json.put("port", port);
            if (boardName != null)
                json.put("shieldname", boardName);
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
                //SensorBase sensors = Shields.getSensorFromId(id);
                if (sensor != null)
                    jarray.put(sensor.getJson());
            }
            json.put("sensors", jarray);

            /*jarray = new JSONArray();
            for (SensorBase actuator : actuators) {
                //SensorBase actuator = Shields.getActuatorFromId(id);
                if (actuator != null)
                    jarray.put(actuator.getJson());
            }
            json.put("actuatorIds", jarray);*/

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
                    jarray.put(sensor.getJson());
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
                deleteSensor(conn,sensor);
            }
        }
    }

    private void deleteSensor(Connection conn,SensorBase sensor) {
        try {
            Statement stmt = conn.createStatement();
            String sql = "DELETE FROM sensors WHERE id=" + sensor.getId() + ";";
            stmt.executeUpdate(sql);
            stmt.close();

            for (SensorBase child: sensor.childSensors) {
                deleteSensor(conn,child);
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
}
