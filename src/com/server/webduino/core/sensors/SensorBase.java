package com.server.webduino.core.sensors;

import com.server.webduino.DBObject;
import com.server.webduino.core.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import sun.management.Sensor;

import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static com.server.webduino.core.sensors.SensorBase.SensorListener.SensorEvents;

public class SensorBase extends /*httpClient,*/ DBObject {

    private static Logger LOGGER = Logger.getLogger(SensorBase.class.getName());

    //public static final String Status_Offline = "OFFLINE";
    //public static final String Status_Online = "ONLINE";
    protected int shieldid;
    protected int parentid;
    protected boolean online = false;
    protected String subaddress;
    protected String name;
    protected String description;
    protected Date lastUpdate;
    protected String type;
    protected int id;
    protected boolean enabled;
    protected String pin;
    protected boolean testMode;
    protected String statusUpdatePath = "/sensorstatus"; // può essere overidden a seconda del tipo

    protected String status = "";
    protected String oldStatus = "";

    /// schedulatorer programm
    public Schedule sensorSchedule = null;
    protected ActiveProgram activeProgram = null;

    //protected JSONObject json = null;
    protected List<SensorListener> listeners = new ArrayList<>();

    public List<SensorBase> childSensors = new ArrayList<SensorBase>();

    public void addChildSensor(SensorBase childSensor) {
        childSensors.add(childSensor);
    }

    public SensorBase(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.subaddress = subaddress;
        this.shieldid = shieldid;
        this.pin = pin;
        this.enabled = enabled;
    }

    public ActiveProgram getActiveProgram() {

        return activeProgram;
    }

    public List<SensorBase> getAllChildSensors() {

        List<SensorBase> list = new ArrayList<>();
        for (SensorBase child : childSensors) {

            list.add(child);

            List<SensorBase> childlist = child.getAllChildSensors();
            if (childlist != null) {
                for (SensorBase elem : childlist) {
                    list.add(elem);
                }
            }
        }
        return list;
    }

    public SensorBase getSensorFromId(int id) {
        if (this.id == id) {
            return this;
        }
        for (SensorBase child : childSensors) {
            return child.getSensorFromId(id);
        }
        return null;
    }

    public SensorBase findSensorFromId(int id) {
        if (this.id == id)
            return this;
        for (SensorBase child : childSensors) {
            SensorBase s = child.findSensorFromId(id);
            if (s != null)
                return s;
        }
        return null;
    }

    public interface SensorListener {
        static public String SensorEvents = "sensor event";
        void changeOnlineStatus(boolean online);
        void changeOnlineStatus(int sensorId, boolean online);
        void onChangeStatus(String newStatus, String oldStatus);

        public abstract void changeDoorStatus(int sensorId, boolean open, boolean oldOpen);
    }

    //protected List<SensorListener> listeners = new ArrayList<SensorListener>();

    public void addListener(SensorListener toAdd) {
        listeners.add(toAdd);
    }

    public void deleteListener(SensorListener toRemove) {
        listeners.remove(toRemove);
    }

    public boolean receiveEvent(String eventtype) {
        if (eventtype == SensorEvents)
            return true;
        return false;
    }

    public boolean sendEvent(String eventtype) {
        if (eventtype == SensorEvents)
            return true;
        return false;
    }

    public void init() {

    }

    public void startPrograms() {

        sensorSchedule = new Schedule();
        sensorSchedule.init(this); // passa se stesso per agganciare il listener
        sensorSchedule.read(id);
    }

    public boolean isUpdated() {

        Date currentDate = Core.getDate();
        if (lastUpdate == null || (currentDate.getTime() - lastUpdate.getTime()) > (60 * 1000)) {
            online = false;
            return false;
        } else {
            return true;
        }
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }


    public int getId() {
        return id;
    }

    public int getShieldId() {
        return shieldid;
    }

    public void setShieldId(int shieldId) {
        this.shieldid = shieldId;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubaddress() {
        return subaddress;
    }

    public void setSubaddress(String subaddress) {
        this.subaddress = subaddress;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setStatus(String status) {
        oldStatus = this.status;
        this.status = status;

        for (SensorListener listener: listeners)
            listener.onChangeStatus(status,oldStatus);
    }

    public String getStatus() {
        return status;
    }


    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public void setLastUpdate(Date date) {
        LOGGER.info("setLastUpdate");
        lastUpdate = date;
    }

    public Date getLastUpdate(Date date) {
        return lastUpdate;
    }

    /*public String requestStatusUpdate() { //

        LOGGER.info("requestStatusUpdate:" + statusUpdatePath);

        writeDataLog("requestStatusUpdate");

        Result result = call("GET", "", statusUpdatePath);
        if (result != null && result.res)
            return result.response;

        for (int i = 0; i < 2; i++) {

            LOGGER.log(Level.WARNING, "retry..." + (i + 1));
            result = call("GET", "", statusUpdatePath);
            if (result != null && result.res)
                return result.response;
        }
        LOGGER.info("end requestStatusUpdate" + result.response);
        return null;
    }*/

    public void writeDataLog(String event) {
    }

    public void updateFromJson(Date date, JSONObject json) {

        try {
            lastUpdate = date;
            online = true;

            if (json.has("name"))
                name = json.getString("name");
            if (json.has("shieldid"))
                shieldid = json.getInt("shieldid");
            if (json.has("subaddress"))
                subaddress = json.getString("subaddress");
            if (json.has("pin"))
                pin = json.getString("pin");
            if (json.has("enabled"))
                enabled = json.getBoolean("enabled");
            if (json.has("testmode"))
                testMode = json.getBoolean("testmode");

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }
    }

    public void getJSONField(JSONObject json) {

    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("shieldid", shieldid);
            json.put("parentid", parentid);
            json.put("online", online);
            json.put("subaddress", subaddress);
            json.put("lastupdate", Core.getStrLastUpdate(lastUpdate));
            json.put("type", type);
            json.put("name", name);
            json.put("description", description);
            json.put("enabled", Core.boolToString(enabled));
            json.put("pin", pin);
            json.put("addr", subaddress);
            json.put("testmode", testMode);

            JSONArray children = new JSONArray();
            for (SensorBase sensor : childSensors) {
                JSONObject child = sensor.toJson();
                children.put(child);
            }
            json.put("childsensors", children);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        // get custom json field
        getJSONField(json);

        return json;
    }

    @Override
    public void fromJson(JSONObject json) throws JSONException {

    }

    protected httpClient.Result call(String method, String param, String path) {

        LOGGER.info("call: " + method + "," + param + "," + path);

        Shields shields = new Shields();
        URL url = shields.getURL(shieldid);

        //URL url = new URL(shields.getURL(shieldid).toString() + shi);

        LOGGER.info("url: " + url.toString());
        //boolean res;

        httpClient client = new httpClient();
        httpClient.Result result = null;
        if (method.equals("GET")) {

            result = client.callGet(param, path, url);
            /*if (result.res) {
                try {
                    Date date = Core.getDate();
                    JSONObject json = new JSONObject(result.response);
                    updateFromJson(date, json);
                    //writeDataLog(date,"request update");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }*/
        } else if (method.equals("POST")) {
            result = client.callPost(param, path, url);
        }

        LOGGER.info("end call");
        return result;
    }

    @Override
    public void write(Connection conn) throws Exception {

        String sql = "INSERT INTO sensors (id, shieldid, parentid, subaddress, name, description, type, enabled, pin)" +
                " VALUES ("
                + id + ","
                + shieldid + ","
                + parentid + ","
                + "\"" + subaddress + "\","
                + "\"" + name + "\","
                + "\"" + description + "\","
                + "\"" + type + "\","
                + Core.boolToString(enabled) + ","
                + "\"" + pin + "\""
                + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "shieldid=" + shieldid + ","
                + "parentid=" + parentid + ","
                + "subaddress=\"" + subaddress + "\","
                + "name=\"" + name + "\","
                + "description=\"" + description + "\","
                + "type=\"" + type + "\","
                + "enabled=" + Core.boolToString(enabled) + ","
                + "pin=\"" + pin + "\";";
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }
        if (childSensors != null) {
            for (SensorBase child : childSensors) {
                if (child.parentid == 0)
                    child.parentid = id;
                if (child.parentid != id) {
                    throw new Exception("child sensor id error");
                }
                child.write(conn);
            }
        }
        stmt.close();
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM sensors WHERE id=" + id;
        stmt.executeUpdate(sql);
    }

    public static List<SensorBase> readSensors(Connection conn, int shieldid, int parentid) throws SQLException {

        Statement stmt = conn.createStatement();
        List<SensorBase> sensors = new ArrayList<>();
        String sql = "SELECT * FROM sensors WHERE shieldid = " + shieldid + " AND parentid=" + parentid;
        ResultSet sensorRs = stmt.executeQuery(sql);

        while (sensorRs.next()) {
            String type ="", name = "", subaddress = "", pin = "", description = "";
            int id = 0;
            boolean enabled = false;

            // id, type e subaddress non possono essere null o zero
            if (sensorRs.getInt("id") <= 0) continue;
                id = sensorRs.getInt("id");
            if (sensorRs.getString("type") == null || sensorRs.getString("type").equals("")) continue;
                type = sensorRs.getString("type");
            if (sensorRs.getString("subaddress") == null || sensorRs.getString("subaddress").equals("")) continue;
                subaddress = sensorRs.getString("subaddress");

            if (sensorRs.getString("name") != null)
                name = sensorRs.getString("name");
            if (sensorRs.getString("description") != null)
                description = sensorRs.getString("description");
            if (sensorRs.getString("pin") != null)
                pin = sensorRs.getString("pin");
            if (sensorRs.getString("enabled") != null)
                enabled = sensorRs.getBoolean("enabled");

            SensorBase sensor;
            sensor = SensorFactory.createSensor(type, name, description, subaddress, id, shieldid, pin, enabled);
            if (sensor == null)
                continue;

            sensor.childSensors = readSensors(conn,shieldid,id);
            sensors.add(sensor);
        }
        return sensors;
    }
}
