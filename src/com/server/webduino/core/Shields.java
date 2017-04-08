package com.server.webduino.core;

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

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Shields {

    public JSONArray getShieldsJsonArray() {
        Shields shields = new Shields();
        List<Shield> list = shields.getShields();

        JSONArray jarray = new JSONArray();
        for (Shield shield : list) {
            JSONObject json = shield.toJson();
            jarray.put(json);
        }
        return jarray;
    }

    interface ShieldsListener {
        void addedActuator(Actuator actuator);
        void addedSensor(SensorBase sensor);
        void addedShield(Shield shield);

        void updatedActuator(Actuator actuator);
        void updatedSensor(SensorBase sensor);
        void updatedShield(Shield shield);
    }

    protected List<ShieldsListener> listeners = new ArrayList<>();
    public void addListener(ShieldsListener toAdd) {
        listeners.add(toAdd);
    }

    private static final Logger LOGGER = Logger.getLogger(Shields.class.getName());

    private static List<TemperatureSensor> mTemperatureSensorList = new ArrayList<TemperatureSensor>();
    private static Actuators mActuators;
    public static Sensors mSensors;


    public void addTemeratureSensorListener(TemperatureSensor.TemperatureSensorListener toAdd) {

        for (SensorBase sensor : mSensors.getLastSensorData()) {
                try { // aggiungi un listener solo se è un sensore di temperatura
                    TemperatureSensor ts = (TemperatureSensor) sensor;
                    ts.addListener(toAdd);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
        }
    }

    public Shields() {
    }

    public void init() {

        mActuators = new Actuators();
        mSensors = new Sensors();
        addListener(mSensors);
        addListener(mActuators);
    }

    public ArrayList<Actuator> getActuators() {
        return mActuators.getActuatorList();
    }

    public List<SensorBase> getLastSensorData() {
        return mSensors.getLastSensorData();
    }

    boolean updateSensors(int shieldid, JSONArray jsonArray) {
        return mSensors.updateSensors(shieldid,jsonArray);
    }

    public void requestActuatorsUpdate() {

        for(SensorBase sensor : mSensors.getSensorList()){
            requestSensorStatusUpdate(sensor);
        }

        for(SensorBase actuator : mActuators.getActuatorList()){
            requestSensorStatusUpdate(actuator);
        }

    }

    public void requestSensorStatusUpdate(SensorBase sensor) {
        if (!sensor.isUpdated()) {
            String res = sensor.requestStatusUpdate();
            if (res == null) {
                LOGGER.severe("sensor " + sensor.id + " OFFLINE");
                Core.sendPushNotification(SendPushMessages.notification_error, "errore", "ACTUATOR " + sensor.id + " OFFLINE", "0",sensor.id);
            } else {
                LOGGER.info(res);

                try {
                    Date date = Core.getDate();
                    JSONObject json = new JSONObject(res);
                    sensor.updateFromJson(date, json);
                    //writeDataLog(date,"request update");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public Actuator getFromShieldId(int shieldid, String subaddress) {
        return mActuators.getFromShieldId(shieldid, subaddress); }

    public Actuator getFromId(int id) {
        return mActuators.getFromId(id); }

    public List<TemperatureSensor> getSensorList() {
        //return mTemperatureSensorList;
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


            for (SensorBase sensor : shield.sensors) {
                //SensorBase sensor = Shields.getSensorFromId(id);
                sql = "INSERT INTO sensors (shieldid, type, subaddress, name)" +
                        " VALUES ("
                        + "\"" + lastid + "\","
                        + "\"" + sensor.type + "\","
                        + "\"" + sensor.subaddress + "\","
                        + "\"" + sensor.name + "\" ) " +
                        "ON DUPLICATE KEY UPDATE "
                        + "shieldid=\"" + lastid + "\","
                        + "type=\"" + sensor.type + "\","
                        + "subaddress=\"" + sensor.subaddress + "\","
                        + "name=\"" + sensor.name + "\""
                        + ";";
                stmt.executeUpdate(sql);
            }

            for(SensorBase actuator : shield.actuators) {
                //SensorBase actuator = Shields.getActuatorFromId(id);
                sql = "INSERT INTO actuators (shieldid, type, subaddress, name)" +
                        " VALUES ("
                        + "\"" + lastid + "\","
                        + "\"" + actuator.type + "\","
                        + "\"" + actuator.subaddress + "\","
                        + "\"" + actuator.name + "\" ) " +
                        "ON DUPLICATE KEY UPDATE "
                        + "shieldid=\"" + lastid + "\","
                        + "type=\"" + actuator.type + "\","
                        + "subaddress=\"" + actuator.subaddress + "\","
                        + "name=\"" + actuator.name + "\""
                        + ";";
                stmt.executeUpdate(sql);
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

        if (affectedRows == 2) { // row updated
            shield.id = lastid;
            for(ShieldsListener listener : listeners) {
                listener.updatedShield(shield);
            }
        } else if (affectedRows == 1) { // row inserted
            shield.id = lastid;

            for(ShieldsListener listener : listeners) {
                listener.addedShield(shield);
                for(SensorBase actuator : shield.actuators) {
                    //SensorBase actuator = Shields.getActuatorFromId(id);
                    listener.addedActuator((Actuator) actuator);
                }
                for(SensorBase sensor : shield.sensors) {
                    //SensorBase sensor = Shields.getActuatorFromId(id);
                    listener.addedSensor(sensor);
                }
            }
        } else { // error

        }

        return lastid;

    }

    public static SensorBase getSensorFromId(int id) {

        return mSensors.getSensorFromId(id);
    }

    public static Actuator getActuatorFromId(int id) {

        return mActuators.getActuatorFromId(id);
    }

    public List<Shield> getShields() {

        List<Shield> list = new ArrayList<>();
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM shields";
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

                list.add(shield);
            }
            // Clean-up environment
            rs.close();

            for (Shield shield : list) {
                sql = "SELECT * FROM sensors " +
                        "WHERE shieldid = " + shield.id;

                ResultSet sensorRs = stmt.executeQuery(sql);
                while (sensorRs.next()) {
                    if (sensorRs.getString("type").equals("temperature")) {
                        TemperatureSensor sensor = new TemperatureSensor();

                        if (sensorRs.getString("subaddress") != null)
                            sensor.subaddress = sensorRs.getString("subaddress");
                        if (sensorRs.getString("name") != null)
                            sensor.name = sensorRs.getString("name");
                        if (sensorRs.getString("id") != null)
                            sensor.id = Integer.valueOf(sensorRs.getString("id"));

                        shield.sensors.add(sensor);
                    }
                    /*if (sensorRs.getInt("id") != 0) {
                        SensorBase sensor = new s
                        int id = sensorRs.getInt("id");
                        shield.sensorIds.add(id);
                    }*/
                }
                sensorRs.close();
            }

            for (Shield shield : list) {
                sql = "SELECT * FROM actuators " +
                        "WHERE shieldid = " + shield.id;

                ResultSet actuatorRs = stmt.executeQuery(sql);
                while (actuatorRs.next()) {
                    if (actuatorRs.getString("type").equals("temperature")) {
                        HeaterActuator actuator = new HeaterActuator();

                        if (actuatorRs.getString("subaddress") != null)
                            actuator.subaddress = actuatorRs.getString("subaddress");
                        if (actuatorRs.getString("name") != null)
                            actuator.name = actuatorRs.getString("name");
                        if (actuatorRs.getString("id") != null)
                            actuator.id = Integer.valueOf(actuatorRs.getString("id"));

                        shield.actuators.add(actuator);
                    }
                    /*if (actuatorRs.getInt("id") != 0) {
                        int id = actuatorRs.getInt("id");
                        shield.actuatorIds.add(id);
                    }*/
                }
                actuatorRs.close();
            }

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


    public static TemperatureSensor get(int index) {

        if (index < 0 || index >= mTemperatureSensorList.size())
            return null;

        return mTemperatureSensorList.get(index);
    }

    /*
    public int update(TemperatureSensor sensor) {

        int lastid;
        String sql;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String date = "NULL";
            date = "'" + df.format((sensor.getLastUpdate())) + "'";

            sql = "INSERT INTO shields (id, lastupdate, name, url, macaddress, boardname)" +
                    " VALUES (" + "" + sensor.id
                    + "," + date + ",\""
                    + sensor.name + "\",\""
                    + sensor.url + "\",\""
                    + sensor.MACAddress + "\",\""
                    + sensor.boardName + "\" ) " +
                    "ON DUPLICATE KEY UPDATE lastupdate=" + date
                    + ",name=\"" + sensor.name + "\""
                    + ",url=\"" + sensor.url + "\""
                    + ",macaddress=\"" + sensor.MACAddress + "\""
                    + ",boardname=\"" + sensor.boardName + "\""
                    + ",url=\"" + sensor.url + "\""
                    + ";";

            Statement stmt = conn.createStatement();
            Integer numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);

                for (ShieldsListener listener : listeners) {// TODO da verificare se serve
                    listener.addedSensor(sensor);
                }

            } else {
                lastid = sensor.id;
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
        return lastid;
    }
*/
    public List<TemperatureSensor> getTemperatureSensorList() {

        List<TemperatureSensor> list = new ArrayList<>();
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            //sql = "SELECT id, url, name FROM sensorIds";
            sql = "SELECT * FROM shields";
            ResultSet rs = stmt.executeQuery(sql);

            list = new ArrayList<>();
            // Extract data from result set
            while (rs.next()) {

                String str = rs.getString("url");
                URL url = new URL(str);
                int id = rs.getInt("id");
                int shieldid = rs.getInt("shieldid");
                Date date = rs.getDate("lastupdate");
                String name = rs.getString("name");
                String MACAddress = rs.getString("MACAddress");
                String boardName = rs.getString("boardName");
                TemperatureSensor temperatureSensor = new TemperatureSensor();

                list.add(temperatureSensor);
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
                strurl = strurl.replace("http://","");
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
