package com.server.webduino.core.sensors;

import com.server.webduino.DBObject;
import com.server.webduino.core.*;
import com.server.webduino.core.datalog.DataLog;
import com.server.webduino.core.sensors.commands.Command;
import com.server.webduino.core.sensors.commands.SensorCommand;
import com.server.webduino.core.webduinosystem.Status;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import sun.management.Sensor;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import static com.server.webduino.core.webduinosystem.Status.*;

//import static com.server.webduino.core.sensors.SensorBase.SensorListener.SensorEvents;

public class SensorBase extends DBObject {

    private static Logger LOGGER = Logger.getLogger(SensorBase.class.getName());
    public DataLog datalog = null;

    //public static final String Command_Statusupdate = "statusupdate"; // "programoff";

    // static states
    protected int shieldid;
    protected int parentid;
    //protected boolean online = false;
    protected String subaddress;
    protected String name;
    protected String description;
    protected boolean hasvalue;
    private double value;
    protected String valuetype;
    protected String valueunit;
    protected String valuetext;
    protected Date lastUpdate;
    protected String type;
    protected int id;
    protected boolean enabled;
    protected String pin;
    protected List<SensorBase> childSensors = new ArrayList<SensorBase>();
    //protected boolean hasIntValue = false;
    //protected boolean hasDoubleValue = false;
    //protected double doubleValue, minDoubleValue, maxDoubleValue, stepDoubleValue;
    //protected int intValue;

    protected List<Status> statusList = new ArrayList<Status>();
    protected List<ActionCommand> actionCommandList = new ArrayList<ActionCommand>();

    // dynamic state
    protected boolean testMode;

    private Status status;
    protected Status oldStatus = null;
    public boolean updating = false;

    protected List<SensorListener> listeners = new ArrayList<>();

    protected void initCommandList() {
        ActionCommand cmd = new ActionCommand(ActionCommand.ACTIONCOMMAND_STATUSUPDATE, ActionCommand.ACTIONCOMMAND_STATUSUPDATE_DESCRIPTION);
        cmd.addStatus("Stato");
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                boolean res = requestStatusUpdate();
                return res;
            }
            @Override
            public void end() {

            }

            @Override
            public JSONObject getResult() {
                return null;
            }
        });
        actionCommandList.add(cmd);
    }

    public SensorBase(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {

        this.id = id;
        this.name = name;
        this.description = description;
        this.subaddress = subaddress;
        this.shieldid = shieldid;
        this.pin = pin;
        this.enabled = enabled;
        datalog = new DataLog();
        valuetype = "generic";
        valueunit = "unit";
        hasvalue = false;

        createStatusList();
        initCommandList();

    }

    protected void createStatusList() {
        Status status = new Status(STATUS_OFFLINE,STATUS_DESCRIPTION_OFFLINE);
        this.status = status;
        statusList.add(status);
        status = new Status(STATUS_DISABLED,STATUS_DESCRIPTION_DISABLED);
        statusList.add(status);
        status = new Status(STATUS_IDLE,STATUS_DESCRIPTION_IDLE);
        statusList.add(status);
    }

    public void requestAsyncSensorStatusUpdate() { // async sensor zonesensorstatus request
        SensorCommand cmd = new SensorCommand(SensorCommand.Command_RequestSensorStatusUpdate, shieldid, id);
        updating = true;

        SendCommandThread sendCommandThread = new SendCommandThread(cmd);
        Thread thread = new Thread(sendCommandThread, "sendCommandThread");
        thread.start();
    }

    public boolean requestStatusUpdate() {

        SensorCommand command = new SensorCommand(SensorCommand.Command_RequestSensorStatusUpdate, shieldid, id);
        boolean res = command.send();
        if (!res /*&& !status.status.equals(STATUS_OFFLINE)*/) {
            setStatus(STATUS_OFFLINE);
        }
        return res;
    }

    public void addChildSensor(SensorBase childSensor) {
        childSensors.add(childSensor);
    }

    public List<SensorBase> getChildSensors() { // ritorna la lista dei figli diretti
        return childSensors;
    }

    public JSONArray getStatusListJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (Status status: statusList) {
            jsonArray.put(status.status);
        }
        return jsonArray;
    }

    public JSONArray getActionCommandListJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (ActionCommand command: actionCommandList) {
            jsonArray.put(command.toJson());
        }
        return jsonArray;
    }

    public void setChildSensors(List<SensorBase> chidren) {
        childSensors = chidren;
    }

    public List<SensorBase> getAllChildSensors() { // ritorna tutti i figli inclusi quelli non diretti

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

    public SensorBase getSensorFromId(int id) { // ritorna il sensore stesso oppure uno dei figli
        if (this.id == id) {
            return this;
        }
        for (SensorBase child : childSensors) {
            if (child.id == id)
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


    public ActionCommand.Command sendCommand(String cmd, JSONObject json) {
        for (ActionCommand actionCommand : actionCommandList) {
            if (cmd.equals(actionCommand.command)) {
                actionCommand.commandMethod.execute(json);
                return actionCommand.commandMethod;
            }
        }
        return null;
    }

    public boolean sendCommand(JSONObject json) {
        for (ActionCommand actionCommand : actionCommandList) {
            String cmd = null;
            try {
                cmd = json.getString("command");
                if (cmd.equals(actionCommand.command)) {
                    return actionCommand.commandMethod.execute(json);
                    //return actionCommand.commandMethod;
                }
            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }


    public Boolean endCommand() {
        return false;
    }

    /*public Boolean hasIntValue() {
        return hasIntValue;
    }

    public Boolean hasDoubleValue() {
        return hasDoubleValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public double getMinDoubleValue() {
        return minDoubleValue;
    }

    public double getMaxDoubleValue() {
        return maxDoubleValue;
    }

    public double getStepDoubleValue() {
        return stepDoubleValue;
    }

    public int getIntValue() {
        return intValue;
    }*/

    public interface SensorListener {
        //static public String SensorEvents = "sensor event";
        public void onChangeOnlineStatus(Status newStatus, Status oldStatus);
        //public void onChangeOnlineStatus(int sensorId, boolean online);
        public void onChangeStatus(SensorBase sensor, Status newStatus, Status oldStatus);
        //public void changeDoorStatus(int sensorId, boolean open, boolean oldOpen);
        public void onChangeValue(SensorBase sensor, double value);
    }

    public void addListener(SensorListener toAdd) {
        listeners.add(toAdd);
    }

    public void removeListener(SensorListener toRemove) {
        listeners.remove(toRemove);
    }

    public void init() {

    }

    public void startPrograms() {

    }

    /*public boolean isUpdated() {

        Date currentDate = Core.getDate();
        if (lastUpdate == null || (currentDate.getTime() - lastUpdate.getTime()) > (60 * 1000)) {
            online = false;
            return false;
        } else {
            return true;
        }
    }*/

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

    public double getValue() {
        return value;
    }

    public void setValue(double val) {
        value = val;
    }

    public String getValueUnit() {
        return valueunit;
    }

    public String getValueType() {
        return valuetype;
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

    public boolean setStatus(String status) {
        oldStatus = this.status;
        for (Status sensorstatus: statusList) {
            if (sensorstatus.status.equals(status)) {
                this.status = sensorstatus;
                if (!status.equals(oldStatus.status)) {
                    for (SensorListener listener : listeners) {
                        listener.onChangeStatus(this, this.status, oldStatus);

                        if (status.equals(STATUS_OFFLINE) && !oldStatus.equals(STATUS_OFFLINE)) {
                            listener.onChangeOnlineStatus(this.status, oldStatus);
                        } else if (!status.equals(STATUS_OFFLINE) && oldStatus.equals(STATUS_OFFLINE)) {
                            listener.onChangeOnlineStatus(this.status, oldStatus);
                        }

                    }
                }

                if (status.equals(STATUS_OFFLINE))
                    Core.updateHomeAssistant("homeassistant/sensor/" + id + "/availability", "offline");
                else
                    Core.updateHomeAssistant("homeassistant/sensor/" + id + "/availability", "online");
                return true;
            }
        }
        Core.updateHomeAssistant("homeassistant/sensor/" + id + "/availability", "offline");
        return false;
    }

    /*public boolean updateHomeAssistant(String path, String message) {
        LOGGER.info("SensorBase::updateHomeAssistant");

        SimpleMqttClient smc;
        smc = new SimpleMqttClient("homeassistantClient");
        if (!smc.runClient("giacomocasa.duckdns.org",1883)) {
            LOGGER.severe("cannot open MQTT client");
            return false;
        }

        smc.publish(path,message);
        smc.disconnect();
        return true;
    }*/

    public Status getStatus() {
        return status;
    }

    public Status getOldStatus() {
        return oldStatus;
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

    public void writeDataLog(String event) {
    }

    public SensorBase getFromSubaddress(String subaddress) {


            for (SensorBase child : this.getChildSensors()) {
                if (child.getSubaddress().equals(subaddress))
                    return child;
            }
        return null;
    }

    public void updateFromJson(Date date, JSONObject json) {

        System.out.println("updateFromJson " + json.toString());
        try {
            lastUpdate = date;
            //online = true;
            if (json.has("enabled"))
                enabled = json.getBoolean("enabled");
            if (json.has("status")) {
                String status = json.getString("status");
                setStatus(status);
            }
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
            writeDataLog("updateFromJson");
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
            //json.put("online", online);
            json.put("subaddress", subaddress);
            if (lastUpdate != null)
                json.put("lastupdate", Core.getStrLastUpdate(lastUpdate));
            json.put("type", type);
            json.put("name", name);
            json.put("description", description);
            json.put("enabled", Core.boolToString(enabled));
            json.put("pin", pin);
            json.put("addr", subaddress);
            json.put("testmode", testMode);

            json.put("hasvalue", hasvalue);
            json.put("value", value);
            json.put("valuetext", valuetext);
            json.put("valueunit", valueunit);
            json.put("valuetype", valuetype);

            json.put("status", getStatus().toJson());
            //json.put("statusdetails", getStatus().description);

            json.put("statuslist", getStatusListJSONArray());
            json.put("actioncommandlist", getActionCommandListJSONArray());
            /*if (hasIntValue)
                json.put("intvalue", intValue);
            if (hasDoubleValue) {
                json.put("doublevalue", doubleValue);
                json.put("mindoublevalue", minDoubleValue);
                json.put("maxdoublevalue", maxDoubleValue);
                json.put("stepdoublevalue", stepDoubleValue);
            }*/
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

    /*protected httpClientResult call(String method, String param, String path) {

        LOGGER.info("call: " + method + "," + param + "," + path);

        Shields shields = new Shields();
        URL url = shields.getURL(shieldid);

        LOGGER.info("url: " + url.toString());
        //boolean res;

        httpClient client = new httpClient();
        httpClientResult result = null;
        if (method.equals("GET")) {

            result = client.callGet(param, path, url);
        } else if (method.equals("POST")) {
            result = client.callPost(param, path, url);
        }

        LOGGER.info("end call");
        return result;
    }*/

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

    class SendCommandThread implements Runnable {

        private volatile boolean execute; // variabile di sincronizzazione
        Command command;

        public SendCommandThread(Command command) {
            this.command = command;
        }

        @Override
        public void run() {
            /*boolean res = command.send();
            if (!res) {
                SensorBase sensor = Core.getSensorFromId(command.sensorid);
                if (sensor != null && !sensor.status.status.equals(STATUS_OFFLINE)) {
                    sensor.setStatus(STATUS_OFFLINE);
                    String description = "Sensor " + sensor.name + " offline";
                    Core.sendPushNotification(SendPushMessages.notification_offline, "Offline", description, "0", 0);
                }
            }*/
            SensorBase sensor = Core.getSensorFromId(command.actuatorid);
            sensor.requestStatusUpdate();
            LOGGER.info("SendCommandThread command.uuid=" + command.uuid.toString());
        }
    }
}

