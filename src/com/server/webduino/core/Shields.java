package com.server.webduino.core;

import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.TemperatureSensor;
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
        Shields shields = new Shields();
        JSONArray jarray = new JSONArray();
        for (Shield shield : list) {
            JSONObject json = shield.toJson();
            jarray.put(json);
        }
        return jarray;
    }

    interface ShieldsListener {

        void addedSensor(SensorBase sensor);

        void addedShield(Shield shield);

        void updatedSensor(SensorBase sensor);

        void updatedShield(Shield shield);
    }

    private static final Logger LOGGER = Logger.getLogger(Shields.class.getName());

    public void addListener(ShieldsListener toAdd) {
        listeners.add(toAdd);
    }

    protected List<ShieldsListener> listeners = new ArrayList<>();
    private static List<TemperatureSensor> mTemperatureSensorList = new ArrayList<TemperatureSensor>();

    public Shields() {
    }

    public void init() {

        read();

        requestShieldsUpdate();
        initPrograms();

        //mSensors.initPrograms(); // non può esserre chiamata dentro costruttore sensors perchà usa mSensor
        //addListener(mSensors);
    }

    public void initPrograms() {
        // questa funzione non può essere chiamata dentro il costruttore
        // perchè altrimenti la lista diu sensori dalvata in core non è ancora inizializzata
        for (Shield shield : list) {
            for (SensorBase sensor : shield.sensors) {
                if (sensor.sensorPrograms != null)
                    sensor.sensorPrograms.checkProgram();
            }
        }
    }

    public List<SensorBase> getLastSensorData() {

        List<SensorBase> sensorList = new ArrayList<>();
        for (Shield shield : list) {
            for (SensorBase sensor : shield.sensors) {
                sensorList.add(sensor);
            }
        }
        return sensorList;

    }

    boolean updateSensors(int shieldid, JSONArray jsonArray) {

        Date date = Core.getDate();
        for (int i = 0; i < jsonArray.length(); i++) {

            try {
                JSONObject json = jsonArray.getJSONObject(i);
                String subaddress = "";
                if (json.has("addr")) {
                    subaddress = json.getString("addr");
                }
                SensorBase sensor = getFromShieldIdandSubaddress(shieldid, subaddress);
                if (sensor != null) {
                    sensor.updateFromJson(date, json);

                    if (json.has("childsensors")) {
                        JSONArray jsonChildSensorArray = json.getJSONArray("childsensors");
                        for (int k = 0; k < jsonChildSensorArray.length(); k++) {
                            JSONObject childSensor = jsonChildSensorArray.getJSONObject(k);
                            if (childSensor.has("addr")) {
                                subaddress = childSensor.getString("addr");
                                SensorBase child = getFromShieldIdandSubaddress(shieldid, subaddress);
                                if (child != null)
                                    child.updateFromJson(date, childSensor);
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

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

    public void requestShieldsUpdate() {

        for (Shield shield : getShields()) {
            String res = shield.requestStatusUpdate();
            if (res == null) {
                LOGGER.severe("sensors " + shield.id + " OFFLINE");
                Core.sendPushNotification(SendPushMessages.notification_error, "errore", "SHIELD " + shield.id + " OFFLINE", "0", shield.id);
            } else {
                LOGGER.info(res);
                try {
                    JSONObject json = new JSONObject(res);

                    if (json.has("shieldid")) {
                        int shieldid = json.getInt("shieldid");
                        if (json.has("sensors")) {
                            JSONArray jsonArray = json.getJSONArray("sensors");
                            //Date lastupdate = Core.getDate();
                            updateSensors(shieldid, jsonArray);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public List<TemperatureSensor> getSensorList() {
        return mTemperatureSensorList;
    }

    public int register(Shield shield) {

        int lastid = -1;
        String sql;
        Integer affectedRows = 0;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = "NULL";
            date = "'" + df.format(shield.lastUpdate) + "'";
            sql = "INSERT INTO shields (lastupdate, url, port, macaddress, boardname)" +
                    " VALUES ("
                    + date + ",\""
                    + shield.url + "\","
                    + shield.port + ",\""
                    + shield.MACAddress + "\",\""
                    + shield.boardName + "\" ) " +
                    "ON DUPLICATE KEY UPDATE lastupdate=" + date
                    + ",url=\"" + shield.url + "\""
                    + ",port=" + shield.port
                    + ",macaddress=\"" + shield.MACAddress + "\""
                    + ",boardname=\"" + shield.boardName + "\""
                    + ";";

            Statement stmt = conn.createStatement();
            affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = -1;
            }

            //String pèrova = "hjkk";
            for (SensorBase sensor : shield.sensors) {

                sql = "INSERT INTO sensors (shieldid, type, subaddress, name, enabled, pin)" +
                        " VALUES ("
                        + "\"" + lastid + "\","
                        + "\"" + sensor.getType() + "\","
                        + "\"" + sensor.getSubaddress() + "\","
                        + "\"" + sensor.getName() + "\"," +
                        Core.boolToString(sensor.getEnabled()) + ","
                        + sensor.getPin() + ") " +
                        "ON DUPLICATE KEY UPDATE "
                        + "shieldid=\"" + lastid + "\","
                        + "type=\"" + sensor.getType() + "\","
                        + "subaddress=\"" + sensor.getSubaddress() + "\","
                        + "name=\"" + sensor.getName() + "\","
                        + "enabled=" + Core.boolToString(sensor.getEnabled()) + ","
                        + "pin=" + sensor.getPin()
                        + ";";
                stmt.executeUpdate(sql);

                for (int i = 0; i < sensor.childSensors.size(); i++) {
                    SensorBase tempSensor = sensor.childSensors.get(i);
                    String subaddress = sensor.getSubaddress() + "." + tempSensor.getId();
                    sql = "INSERT INTO sensors (shieldid, type, subaddress, name, enabled, pin)" +
                            " VALUES ("
                            + "\"" + lastid + "\","
                            + "\"" + "temperature" + "\","
                            + "\"" + subaddress + "\","
                            + "\"" + sensor.getName() + "\"," +
                            Core.boolToString(sensor.getEnabled()) + ","
                            + sensor.getPin() + ")" +
                            "ON DUPLICATE KEY UPDATE "
                            + "shieldid=\"" + lastid + "\","
                            + "type=\"" + "temperature" + "\","
                            + "subaddress=\"" + subaddress + "\","
                            + "name=\"" + sensor.getName() + "\","
                            + "enabled=" + Core.boolToString(sensor.getEnabled()) + ","
                            + "pin=" + sensor.getPin()
                            + ";";
                    stmt.executeUpdate(sql);
                }
            }

            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            LOGGER.severe(se.toString());
            return 0;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            LOGGER.severe(e.toString());
            return 0;
        }

        if (lastid != -1 && shield != null) {
            shield.id = lastid;
            for (SensorBase sensor : shield.sensors) {
                sensor.setShieldId(shield.id);
                for (SensorBase child : sensor.childSensors) {
                    child.setShieldId(shield.id);
                }
            }
        }

        if (affectedRows == 2) { // row updated

            for (ShieldsListener listener : listeners) {
                addShield(shield);
            }
        } else if (affectedRows == 1) { // row inserted
            for (ShieldsListener listener : listeners) {
                addShield(shield);
            }
        } else { // error

        }

        return lastid;
    }

    public static SensorBase getSensorFromId(int id) {

        for (Shield shield : list) {
            for (SensorBase sensor : shield.sensors) {
                if (sensor.getId() == id)
                    return sensor;
            }
        }
        return null;
    }

    public List<Shield> getShields() {
        return list;
    }

    public void read() {

        //List<Shield> list = new ArrayList<>();
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql = "SELECT * FROM shields";
            ResultSet rs = stmt.executeQuery(sql);
            // Extract data from result set
            while (rs.next()) {

                Shield shield = new Shield();
                shield.id = rs.getInt("id");
                if (rs.getString("macaddress") != null)
                    shield.MACAddress = rs.getString("MACAddress");
                if (rs.getString("boardname") != null)
                    shield.boardName = rs.getString("boardname");
                shield.port = rs.getInt("port");
                if (rs.getString("url") != null)
                    shield.url = new URL(rs.getString("url"));

                Statement stmt2 = conn.createStatement();
                String sql2 = "SELECT * FROM sensors " +
                        "WHERE shieldid = " + shield.id;

                ResultSet sensorRs = stmt2.executeQuery(sql2);
                while (sensorRs.next()) {
                    String type = "";
                    String name = "";
                    int id = 0;
                    int shieldid = 0;
                    String subaddress = "";
                    if (sensorRs.getString("type") != null)
                        type = sensorRs.getString("type");
                    id = sensorRs.getInt("id");
                    shieldid = sensorRs.getInt("shieldid");
                    if (sensorRs.getString("subaddress") != null)
                        subaddress = sensorRs.getString("subaddress");
                    if (sensorRs.getString("name") != null)
                        name = sensorRs.getString("name");

                    SensorBase sensor;
                    sensor = SensorFactory.createSensor(type, name, subaddress, id, shieldid);
                    if (sensor == null)
                        continue;

                    shield.sensors.add(sensor);
                }

                sensorRs.close();
                stmt2.close();
                //list.add(shield);
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
    protected void updateSensorListeners(Shield shield) {

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
    }

    // chiamata quand una shield si regiustra oppure quando viene letta da db

    public void addShield(Shield shield) {

        // controlla che nion ci sia già in memoria una shield con lo stesso id ed eventualmete la elimina
        for (Shield s : list) {
            if (s.id == shield.id)
                list.remove(s);
        }

        list.add(shield);

        updateSensorListeners(shield);
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
