package com.server.webduino.core;

import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.Command;
import com.server.webduino.servlet.SendPushMessages;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

import static com.server.webduino.core.sensors.TemperatureSensor.TemperatureSensorListener.TemperatureEvents;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Shields {

    static private List<Shield> list = new ArrayList<>();//shields.getShields();

    public JSONArray getShieldsJsonArray() {
        JSONArray jarray = new JSONArray();
        for (Shield shield : list) {
            JSONObject json = shield.toJson();
            jarray.put(json);
        }
        return jarray;
    }

    public JSONObject getShieldSettingJson(int shieldid) {
        Shield shield = fromId(shieldid);
        return shield.getShieldSettingJson();
    }

    public JSONObject getShieldSensorsJson(int shieldid) {
        Shield shield = fromId(shieldid);
        return shield.getShieldSensorsJson();
    }


    private static final Logger LOGGER = Logger.getLogger(Shields.class.getName());

    /*public void addListener(ShieldsListener toAdd) {
        listeners.add(toAdd);
    }

    protected List<ShieldsListener> listeners = new ArrayList<>();*/

    public Shields() {
    }

    SimpleMqttClient smc;

    public void init() {

        read();

        /*SimpleMqttClient*/
        smc = new SimpleMqttClient("ShieldClient");
        if (!smc.runClient())
            return;

        smc.subscribe("toServer/shield/#");
        //smc.subscribe("toServer/sensor");
        smc.addListener(new SimpleMqttClient.SimpleMqttClientListener() {
            @Override
            public synchronized void messageReceived(String topic, String message) {

                if (topic.equals("toServer/shield/loadsettings")) {  // chiamata all'inizio dalla schesa


                    try {
                        JSONObject json = new JSONObject(message);
                        if (json.has("MACAddress")) {
                            String MACAddress = json.getString("MACAddress");

                            String rebootreason = "";
                            if (json.has("rebootreason")) {
                                rebootreason = json.getString("rebootreason");
                            }
                            if (smc != null) {
                                JSONObject jsonResult = loadShieldSettings(MACAddress);
                                smc.publish("fromServer/shield/" + MACAddress + "/settings", jsonResult.toString());
                            }

                            Shield shield = Core.getShieldFromMACAddress(MACAddress);
                            if (shield != null) {
                                shield.datalog.writelog("loadsettings - reason: " + rebootreason, shield);
                                String description = "Shield " + shield.boardName + " restarted";
                                Core.sendPushNotification(SendPushMessages.notification_restarted, "Restart", description + " " + rebootreason, "0", 0);
                            }
                        } else {
                            return;
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    /*String MACAddress = message;
                    JSONObject jsonResult = loadShieldSettings(MACAddress);
                    if (smc != null)
                        smc.publish("fromServer/shield/" + MACAddress + "/settings", jsonResult.toString());
                    // questo va cambiato. Dovrebbe chiamare un comando e dovrebbe essere messo tutto nell'if successivop

                    Shield shield = Core.getShieldFromMACAddress(MACAddress);

                    if (shield != null) {
                        shield.datalog.writelog("loadsettings", shield);
                        String description = "Shield " + shield.boardName + " restarted";
                        Core.sendPushNotification(SendPushMessages.notification_restarted, "Restart", description, "0", 0);
                    }*/

                } else if (topic.equals("toServer/shield/time")) {  // chiamata all'inizio dalla schesa


                    String MACAddress = message;
                    JSONObject jsonResult = loadShieldSettings(MACAddress);
                    if (smc != null) {
                        Date date = Core.getDate();
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);
                        int timezone = cal.getTimeZone().SHORT;
                        long time = date.getTime() / 1000 + Core.getTimeOffset();
                        ;
                        smc.publish("fromServer/shield/" + MACAddress + "/time", "" + time);
                    }
                    // questo va cambiato. Dovrebbe chiamare un comando e dovrebbe essere messo tutto nell'if successivop

                    //Shield shield = Core.getShieldFromMACAddress(MACAddress);

                    /*if (shield != null) {
                        shield.datalog.writelog("loadsettings",shield);
                        String description = "Shield " + shield.boardName + " restarted";
                        Core.sendPushNotification(SendPushMessages.notification_restarted, "Restart", description, "0", 0);
                    }*/

                } else if (topic.equals("toServer/shield/sensor/update")) { // chiamata dalla scheda quando un sensore cambia qualcosa

                    try {
                        JSONObject json = new JSONObject(message);
                        if (json.has("sensorid")) {
                            int sensorid = json.getInt("sensorid");
                            SensorBase sensorBase = getSensorFromId(sensorid);
                            if (sensorBase != null) {
                                sensorBase.updateFromJson(Core.getDate(), json);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (topic.equals("toServer/shield/update")) { // chiamata dalla scheda quando un sensore cambia qualcosa
                    // da eliminarte

                    try {
                        JSONObject json = new JSONObject(message);
                        if (json.has("MAC")) {
                            String MACAddress = json.getString("MAC");
                            //int sensorid = json.getInt("MAC");
                            Shield shield = getShieldFromMACAddress(MACAddress);
                            if (shield != null) {
                                shield.updateShieldStatus(json);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else if (topic.equals("toServer/shield/log/#")) { // chiamata dalla scheda quando un sensore cambia qualcosa
                    // da eliminarte

                    try {
                        JSONObject json = new JSONObject(message);
                        if (json.has("MAC")) {
                            String MACAddress = json.getString("MAC");
                            //int sensorid = json.getInt("MAC");
                            Shield shield = getShieldFromMACAddress(MACAddress);
                            if (shield != null) {
                                shield.updateShieldStatus(json);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }

            }

            @Override
            public void connectionLost() {

            }
        });


        requestSensorsStatusUpdate();

    }

    public List<SensorBase> getLastSensorData() {

        List<SensorBase> sensorList = new ArrayList<>();
        for (Shield shield : list) {
            for (SensorBase sensor : shield.sensors) {
                sensorList.add(sensor);
                for (SensorBase child : sensor.getChildSensors()) {
                    sensorList.add(child);
                }
            }
        }
        return sensorList;
    }

    public String getSettingStatus(int shieldid) {
        Shield shield = fromId(shieldid);
        return shield.getSettingStatus();
    }

    public String getSensorStatus(int shieldid) {
        Shield shield = fromId(shieldid);
        return shield.getSensorStatus();
    }

    boolean updateSettings(int shieldid, JSONObject json) {
        Shield shield = fromId(shieldid);
        return shield.updateSettings(json);
    }

    boolean updateShieldSensors(int shieldid, JSONArray jsonArray) {
        Shield shield = fromId(shieldid);
        return shield.updateSensors(jsonArray);
    }

    boolean updateShieldSensor(int id, JSONObject json) {
        Shield shield = fromSensorId(id);
        if (shield == null)
            return false;
        if (json.has("name")) {
            String name = null;
            try {
                name = json.getString("name");
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
            return shield.updateSensor(id, name);
        } else
            return false;
    }


    boolean updateShieldStatus(int shieldid, JSONObject json) {
        Shield shield = fromId(shieldid);
        return shield.updateShieldStatus(json);
    }

    // DA ELIMINARE
    public SensorBase getFromShieldIdandSubaddress(int shieldid, String subaddress) {

        for (Shield shield : list) {
            if (shield.id == shieldid) {
                for (SensorBase sensor : shield.sensors) {
                    if (sensor.getSubaddress().equals(subaddress))
                        return sensor;
                }
            }
        }
        return null;
    }

    public Shield fromId(int id) {
        for (Shield shield : getShields()) {
            if (shield.id == id) {
                return shield;
            }
        }
        return null;
    }

    public Shield fromSensorId(int id) {
        for (Shield shield : getShields()) {
            if (shield.findSensorFromId(id) != null) {
                return shield;
            }
        }
        return null;
    }

    public boolean requestShieldSettingStatusUpdate(int shieldid) {
        Shield shield = fromId(shieldid);
        if (shield != null) {
            boolean res = shield.requestSettingUpdate();
            return res;
        }
        return false;
    }

    public void requestSensorsStatusUpdate() {

        for (Shield shield : getShields()) {
            boolean res = shield.checkHealth(); // questo andrebbe messo in un quartz separato
            if (!res) {
                //String description = "Shield " + shield.boardName + " offline";
                //Core.sendPushNotification(SendPushMessages.notification_offline, "Offline", description, "0", 0);
            }
            //shield.requestAsyncAllSensorStatusUpdate();
        }
    }

    public JSONObject loadShieldSettings(String MACAddress) {  // questo viene chiamato da ogni shield all'avvio

        JSONObject json = new JSONObject();

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM shields WHERE macaddress='" + MACAddress + "'";
            ResultSet shieldRs = stmt.executeQuery(sql);


            if (shieldRs.next()) {

                int shieldid = shieldRs.getInt("id");
                json.put("shieldid", shieldid);
                json.put("name", shieldRs.getString("name"));
                json.put("enabled", shieldRs.getBoolean("enabled"));

                int parentId = 0;
                JSONArray sensors = addChildSensors(conn, shieldid, parentId);
                json.put("sensors", sensors);
            } else {
                String boardname = "newboard";
                int shieldid = addNewShield(conn, boardname, MACAddress);
                json.put("shieldid", shieldid);
                json.put("name", boardname);
            }
            shieldRs.close();
            stmt.close();

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

        return json;
    }

    private int addNewShield(Connection conn, String boardname, String macAddress) {

        int lastid = 0;
        try {
            Statement stmt = conn.createStatement();

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = "NULL";
            date = "'" + df.format(Core.getDate()) + "'";
            String sql = "INSERT INTO shields (lastupdate, macaddress, name)" +
                    " VALUES ("
                    + date + ",\""
                    + macAddress + "\",\""
                    + boardname + "\" ) ";

            int affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();

            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = -1;
            }
            rs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastid;
    }

    private JSONArray addChildSensors(Connection conn, int shieldid, int parentId) {

        JSONArray jsonarray = new JSONArray();

        Statement stmt = null;
        try {
            stmt = conn.createStatement();

            String sql2 = "SELECT * FROM sensors " +
                    "WHERE shieldid = " + shieldid + " AND parentid=" + parentId;
            ResultSet sensorRs = stmt.executeQuery(sql2);

            while (sensorRs.next()) {

                JSONObject json = new JSONObject();
                int sensorid = sensorRs.getInt("id");
                json.put("id", sensorid);
                json.put("parentid", parentId);
                json.put("shieldid", sensorRs.getInt("shieldid"));
                json.put("subaddress", sensorRs.getString("subaddress"));
                json.put("name", sensorRs.getString("name"));
                json.put("type", sensorRs.getString("type"));
                json.put("enabled", sensorRs.getBoolean("enabled"));
                json.put("pin", sensorRs.getString("pin"));

                JSONArray child = addChildSensors(conn, shieldid, sensorid);
                if (child != null)
                    json.put("childsensors", child);

                jsonarray.put(json);
            }
            sensorRs.close();
            stmt.close();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (jsonarray.length() == 0)
            return null;
        return jsonarray;
    }

    public SensorBase getSensorFromId(int id) {
        for (Shield shield : list) {
            for (SensorBase sensor : shield.sensors) {
                SensorBase ret = sensor.getSensorFromId(id);
                if (ret != null)
                    return ret;
            }
        }
        return null;
    }

    public Shield getShieldFromMACAddress(String MACAddress) {
        for (Shield shield : list) {
            if (shield.MACAddress.equalsIgnoreCase(MACAddress))
                return shield;
        }
        return null;
    }

    public List<Shield> getShields() {
        return list;
    }

    public void read() {

        list.clear();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM shields";
            ResultSet rs = stmt.executeQuery(sql);
            // Extract data from result set
            while (rs.next()) {
                Shield shield = new Shield();
                if (rs.getInt("id") <= 0) continue;
                shield.id = rs.getInt("id");
                if (rs.getString("macaddress") == null || rs.getString("macaddress").equals("")) continue;
                shield.MACAddress = rs.getString("MACAddress");
                if (rs.getString("name") != null)
                    shield.boardName = rs.getString("name");
                if (rs.getString("description") != null)
                    shield.description = rs.getString("description");
                shield.enabled = rs.getBoolean("enabled");

                shield.sensors = SensorBase.readSensors(conn, shield.id, 0);
                addShield(shield);
            }
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return;
        }
    }

    // chiamata per aggiungere un sensore
    /*protected void updateSensorListeners(Shield shield) {

        for (SensorBase sensor : shield.sensors) {
            if (sensor.receiveEvent(TemperatureEvents)) {
                for (Shield s : list) {
                    for (SensorBase sender : s.sensors) {
                        if (sender.sendEvent(TemperatureEvents)) {
                            sender.addListener((SensorBase.SensorListener) sensor);
                        }
                    }
                }
            }

            if (sensor.sendEvent(TemperatureEvents)) {
                for (Shield s : list) {
                    for (SensorBase receiver : s.sensors) {
                        if (receiver.receiveEvent(TemperatureEvents)) {
                            sensor.addListener((SensorBase.SensorListener) receiver);
                        }
                    }
                }
            }
        }
    }*/

    // chiamata quand una shield si regiustra oppure quando viene letta da db

    public void addShield(Shield shield) {

        // controlla che nion ci sia già in memoria una shield con lo stesso id ed eventualmete la elimina
        for (Shield s : list) {
            if (s.id == shield.id)
                list.remove(s);
        }

        list.add(shield);

        //updateSensorListeners(shield);
    }

    public URL getURL(int id) {

        URL url = null;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            //sql = "SELECT id, url, name FROM sensorIds";
            sql = "SELECT * FROM shields WHERE id=" + id;
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                String strurl = rs.getString("url");
                strurl = strurl.replace("http://", "");
                url = new URL("http://" + strurl + ":" + rs.getInt("port"));
            }
            // Clean-up environment
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return null;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return null;
        }
        return url;
    }
}
