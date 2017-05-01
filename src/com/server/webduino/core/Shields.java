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

    public JSONObject getShieldSettingJson(int shieldid) {
        Shield shield = fromId(shieldid);
        return shield.getShieldSettingJson();
    }

    public JSONObject getShieldSensorsJson(int shieldid) {
        Shield shield = fromId(shieldid);
        return shield.getShieldSensorsJson();
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

        requestSensorStatusUpdate();
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

    // DA ELIMINARE
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
        return  null;
    }

    public boolean postCommand(int shieldid, Command command) {
        Shield shield = fromId(shieldid);
        if (shield != null) {
            return shield.postCommand(command);
        }
        return false;
    }

    public boolean requestShieldSettingStatusUpdate(int shieldid) {
        Shield shield = fromId(shieldid);
        if (shield != null) {
            boolean res = shield.requestSettingUpdate();
            return res;
        }
        return false;
    }

    public boolean requestShieldSensorsStatusUpdate(int shieldid) {
        Shield shield = fromId(shieldid);
        if (shield != null) {
            boolean res = shield.requestSensorStatusUpdate();
            return res;
        }
        return false;
    }

    public void requestSensorStatusUpdate() {

        for (Shield shield : getShields()) {
            boolean res = shield.requestSensorStatusUpdate();
            if (res == false) {
                LOGGER.severe("sensors " + shield.id + " OFFLINE");
                Core.sendPushNotification(SendPushMessages.notification_error, "errore", "SHIELD " + shield.id + " OFFLINE", "0", shield.id);
            }
        }
    }

    public List<TemperatureSensor> getSensorList() {
        return mTemperatureSensorList;
    }

    public int register(Shield shield) {

        int lastid = -1;
        int shieldid = 0;
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

            /*Statement stmt = conn.createStatement();
            affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = -1;
            }*/

            Statement stmt2 = conn.createStatement();
            String query = "SELECT * FROM shields WHERE macaddress=\"" + shield.MACAddress + "\";";
            ResultSet rs2 = stmt2.executeQuery(query);

            if (rs2.next()) {
                shieldid = rs2.getInt(1);
            } else {
                shieldid = lastid;
            }
            stmt2.close();

            Statement stmt = conn.createStatement();

            //String pèrova = "hjkk";
            for (SensorBase sensor : shield.sensors) {

                sql = "INSERT INTO sensors (shieldid, parentid, type, subaddress, name, enabled, pin)" +
                        " VALUES ("
                        + "\"" + shieldid + "\","
                        + "\"" + 0 + "\","
                        + "\"" + sensor.getType() + "\","
                        + "\"" + sensor.getSubaddress() + "\","
                        + "\"" + sensor.getName() + "\"," +
                        Core.boolToString(sensor.getEnabled()) + ","
                        + "\"" + sensor.getPin() + "\") " +
                        "ON DUPLICATE KEY UPDATE "
                        + "shieldid=\"" + shieldid + "\","
                        + "parentid=\"" + 0 + "\","
                        + "type=\"" + sensor.getType() + "\","
                        + "subaddress=\"" + sensor.getSubaddress() + "\","
                        + "name=\"" + sensor.getName() + "\","
                        + "enabled=" + Core.boolToString(sensor.getEnabled()) + ","
                        + "pin=\"" + sensor.getPin() + "\""
                        + ";";
                stmt.executeUpdate(sql);
                int affectedRows2 = stmt.executeUpdate(sql);


                /*Statement */stmt2 = conn.createStatement();
                /*String */query = "SELECT * FROM sensors WHERE shieldid=" + shieldid + " AND subaddress=\"" + sensor.getSubaddress() + "\";";
                /*ResultSet*/ rs2 = stmt2.executeQuery(query);
                int sensorid = 0;
                if (rs2.next()) {
                    sensorid = rs2.getInt(1);
                } else {
                    sensorid = 0;
                }
                stmt2.close();

                for (int i = 0; i < sensor.childSensors.size(); i++) {
                    SensorBase tempSensor = sensor.childSensors.get(i);
                    String subaddress = sensor.getSubaddress() + "." + tempSensor.getId();
                    sql = "INSERT INTO sensors (shieldid, parentid, type, subaddress, name, enabled, pin)" +
                            " VALUES ("
                            + "\"" + shieldid + "\","
                            + "\"" + sensorid + "\","
                            + "\"" + "temperature" + "\","
                            + "\"" + subaddress + "\","
                            + "\"" + sensor.getName() + "\"," +
                            Core.boolToString(sensor.getEnabled()) + ","
                            + "\"" + sensor.getPin() + "\") " +
                            "ON DUPLICATE KEY UPDATE "
                            + "shieldid=\"" + shieldid + "\","
                            + "parentid=\"" + sensorid + "\","
                            + "type=\"" + "temperature" + "\","
                            + "subaddress=\"" + subaddress + "\","
                            + "name=\"" + sensor.getName() + "\","
                            + "enabled=" + Core.boolToString(sensor.getEnabled()) + ","
                            + "pin=\"" + sensor.getPin() + "\""
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




        if (shieldid != 0 && shield != null) {
            shield.id = shieldid;
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

        return shieldid;
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
                        "WHERE shieldid = " + shield.id + " AND parentid=0";

                ResultSet sensorRs = stmt2.executeQuery(sql2);
                while (sensorRs.next()) {
                    String type = "";
                    String name = "";
                    int id = 0;
                    int shieldid = 0;
                    String subaddress = "";
                    String pin = "";
                    boolean enabled;
                    if (sensorRs.getString("type") != null)
                        type = sensorRs.getString("type");
                    id = sensorRs.getInt("id");
                    shieldid = sensorRs.getInt("shieldid");
                    if (sensorRs.getString("subaddress") != null)
                        subaddress = sensorRs.getString("subaddress");
                    if (sensorRs.getString("name") != null)
                        name = sensorRs.getString("name");
                    if (sensorRs.getString("pin") != null)
                        pin = sensorRs.getString("pin");
                    enabled = sensorRs.getBoolean("enabled");

                    SensorBase sensor;
                    sensor = SensorFactory.createSensor(type, name, subaddress, id, shieldid,pin,enabled);
                    if (sensor == null)
                        continue;

                    // add child sensors
                    Statement stmt3 = conn.createStatement();
                    String sql3 = "SELECT * FROM sensors " +
                            "WHERE shieldid = " + shield.id + " AND parentid="+id;
                    ResultSet sensorRs3 = stmt3.executeQuery(sql3);
                    while (sensorRs3.next()) {

                        if (sensorRs3.getString("type") != null)
                            type = sensorRs3.getString("type");
                        id = sensorRs3.getInt("id");
                        shieldid = sensorRs3.getInt("shieldid");
                        if (sensorRs3.getString("subaddress") != null)
                            subaddress = sensorRs3.getString("subaddress");
                        if (sensorRs3.getString("name") != null)
                            name = sensorRs3.getString("name");
                        if (sensorRs3.getString("enabled") != null)
                            enabled = sensorRs.getBoolean("enabled");

                        SensorBase child;
                        child = SensorFactory.createSensor(type, name, subaddress, id, shieldid,"",enabled);
                        if (sensor == null)
                            continue;
                        sensor.childSensors.add(child);
                    }
                    stmt3.close();


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
