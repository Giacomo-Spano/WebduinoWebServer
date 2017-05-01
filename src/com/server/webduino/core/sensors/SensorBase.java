package com.server.webduino.core.sensors;

import com.server.webduino.core.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import sun.management.Sensor;

import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.server.webduino.core.sensors.SensorBase.SensorListener.SensorEvents;
import static com.server.webduino.core.sensors.TemperatureSensor.TemperatureSensorListener.TemperatureEvents;

public class SensorBase extends httpClient {

    private static Logger LOGGER = Logger.getLogger(SensorBase.class.getName());

    //public static final String Status_Offline = "OFFLINE";
    //public static final String Status_Online = "ONLINE";
    protected int shieldid;
    protected boolean online = false;
    protected String subaddress;
    protected String name; // valore letto dal db
    protected Date lastUpdate;
    protected String type;
    protected int id;
    protected boolean enabled;
    protected String pin;
    protected String statusUpdatePath = "/sensorstatus"; // può essere overidden a seconda del tipo

    /// schedulatorer programm
    public Programs sensorPrograms = null;
    protected ActiveProgram activeProgram = null;

    protected JSONObject json = null;

    public List<SensorBase> childSensors = new ArrayList<SensorBase>();

    public void addChildSensor(SensorBase childSensor) {
        childSensors.add(childSensor);
    }

    public SensorBase(int id, String name, String subaddress, int shieldid, String pin, boolean enabled) {

        this.id = id;
        this.name = name;
        this.subaddress = subaddress;
        this.shieldid = shieldid;
        this.pin = pin;
        this.enabled = enabled;
    }

    public ActiveProgram getActiveProgram() {

        return activeProgram;
    }

    public interface SensorListener {
        static public String SensorEvents = "sensor event";
        void changeOnlineStatus(boolean online);
    }

    protected List<SensorListener> listeners = new ArrayList<SensorListener>();

    public void addListener(SensorListener toAdd) {
        listeners.add(toAdd);
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

        sensorPrograms = new Programs();
        sensorPrograms.init(this); // passa se stesso per agganciare il listener
        sensorPrograms.read(id);
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

    /*public void setData(int shieldid, String subaddress, String name, Date date) {
        this.shieldid = shieldid;
        this.subaddress = subaddress;
        this.name = name;
        this.lastUpdate = date;

    }*/

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public String getStrLastUpdate() {
        if (lastUpdate == null)
            return "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(lastUpdate);
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

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getPin() {
        return pin;
    }

    public void setLastUpdate(Date date) {
        LOGGER.info("setLastUpdate");
        lastUpdate = date;
    }

    public Date getLastUpdate(Date date) {
        return lastUpdate;
    }

    public String requestStatusUpdate() { //

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
    }

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

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }
    }

    public void getJSONField() {

    }

    public JSONObject getJson() {

        json = new JSONObject();
        try {
            json.put("id", getId());
            json.put("shieldid", shieldid);
            json.put("online", online);
            json.put("subaddress", subaddress);
            json.put("lastupdate", getStrLastUpdate());
            json.put("type", type);
            json.put("name", getName());
            json.put("enabled", Core.boolToString(enabled));
            json.put("pin", pin);
            json.put("addr", subaddress);

            JSONArray children = new JSONArray();
            for(SensorBase sensor: childSensors) {
                JSONObject child = sensor.getJson();
                children.put(child);
            }
            json.put("childsensors", children);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        // get custom json field
        getJSONField();

        return json;
    }

    protected Result call(String method, String param, String path) {

        LOGGER.info("call: " + method + "," + param + "," + path);

        Shields shields = new Shields();
        URL url = shields.getURL(shieldid);

        //URL url = new URL(shields.getURL(shieldid).toString() + shi);

        LOGGER.info("url: " + url.toString());
        //boolean res;

        Result result = null;
        if (method.equals("GET")) {

            result = callGet(param, path, url);
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
            result = callPost(param, path, url);
        }

        LOGGER.info("end call");
        return result;
    }
}
