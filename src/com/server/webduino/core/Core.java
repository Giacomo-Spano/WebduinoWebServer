package com.server.webduino.core;

import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.*;
import com.server.webduino.core.webduinosystem.exits.Exit;
import com.server.webduino.core.webduinosystem.exits.ExitFactory;
import com.server.webduino.core.webduinosystem.keys.Key;
import com.server.webduino.core.webduinosystem.keys.KeyFactory;
import com.server.webduino.core.webduinosystem.scenario.*;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramAction;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramActionFactory;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Core implements SampleAsyncCallBack.SampleAsyncCallBackListener, SimpleMqttClient.SimpleMqttClientListener, Shields.ShieldsListener {

    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());

    protected static String production_envVar;
    protected static String appDNS_envVar;
    protected static String mysqlDBHost_envVar;
    protected static String mysqlDBPort_envVar;
    protected static String tmpDir_envVar;
    protected static String dataDir_envVar;
    private static String version = "0.11";

    public static String APP_DNS_OPENSHIFT = "webduinocenter.rhcloud.com";
    public static String APP_DNS_OPENSHIFTTEST = "webduinocenterbeta-giacomohome.rhcloud.com";

    private static List<WebduinoSystem> webduinoSystems = new ArrayList<>();
    private static List<Zone> zones = new ArrayList<>();
    //private static List<Trigger> triggers = new ArrayList<>();
    private static Triggers triggerClass = new Triggers();
    private static Scenarios scenarios = new Scenarios();
    private static List<Exit> exits = new ArrayList<>();
    private static List<Key> keys = new ArrayList<>();
    public static Shields mShields; // rendere private
    public static Schedule mSchedule;// DA ELIMINARE

    private static List<SWVersion> swversions = new ArrayList<>();

    public static Devices mDevices = new Devices();

    static SimpleMqttClient smc;

    public static boolean sendRestartCommand(JSONObject json) {
        return mShields.sendRestartCommand(json);
    }


    public interface CoreListener {
        void onCommandResponse(String uuid, String response);
    }

    static protected List<CoreListener> listeners = new ArrayList<CoreListener>();

    static public void addListener(CoreListener toAdd) {
        listeners.add(toAdd);
    }

    static public void removeListener(CoreListener toRemove) {
        listeners.remove(toRemove);
    }


    public Core() {
        production_envVar = System.getenv("PRODUCTION");
        appDNS_envVar = System.getenv("OPENSHIFT_APP_DNS");
        mysqlDBHost_envVar = System.getenv("OPENSHIFT_MYSQL_DB_HOST");
        mysqlDBPort_envVar = System.getenv("OPENSHIFT_MYSQL_DB_PORT");
        tmpDir_envVar = System.getenv("OPENSHIFT_TMP_DIR");
        dataDir_envVar = System.getenv("OPENSHIFT_DATA_DIR");
    }

    public static String getUser() {
        if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFT))
            return "adminUp6Qw2f";
        else if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFTTEST))
            return "adminjNm7VUk";
            //return "adminw8ZVVu2";
        else
            //return "adminzdVX5dl";// production
            return "root";
    }

    public static String getPassword() {
        if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFT))
            return "rmIf9KYneg1C";
        else if (appDNS_envVar != null && appDNS_envVar.equals(APP_DNS_OPENSHIFTTEST))
            return "xX1MAIXQLLHq";
            //return "MhbY-61ZlqU4";
        else
            //return "eEySMcJ6WCj4"; //production
            return "giacomo";
    }

    public static String getDbUrl() {
        //test
        if (production_envVar != null && production_envVar.equals("0")) {
            //LOGGER.info("jdbc:mysql://127.0.0.1:3306/webduino_debug");
            return "jdbc:mysql://127.0.0.1:3306/webduino_debug";
        } else {
            //LOGGER.info("jdbc:mysql://127.0.0.1:3306/webduino");
            return "jdbc:mysql://127.0.0.1:3306/webduino";
        }
        //return "jdbc:mysql://127.0.0.1:3307/jbossews"; // production
    }

    public static boolean isProduction() {

        String tmpDir = System.getProperty("java.io.tmpdir");
        if (tmpDir.equals("C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\temp") ||
                tmpDir.equals("C:\\Program Files\\Apache Software Foundation\\Tomcat 7.0\\temp"))
            return false;
        else
            return true;
    }

    public static Zone getZoneFromId(int zoneid) {

        for (Zone zone : zones) {
            if (zoneid == zone.getId()) {
                return zone;
            }
        }
        return null;
    }

    public static JSONArray getSensorsJSONArray(int shieldid, String type) {
        JSONArray jsonArray = new JSONArray();
        for (SensorBase sensor : mShields.getLastSensorData()) {

            if (type != null && !type.equals("")) {
                if (!type.equals(sensor.getType()))
                    continue;
            }

            if (shieldid <= 0) {
                JSONObject json = sensor.toJson();
                jsonArray.put(json);
            } else {
                if (sensor.getShieldId() == shieldid) {
                    JSONObject json = sensor.toJson();
                    jsonArray.put(json);
                }
            }
        }
        return jsonArray;
    }

    public static JSONArray getTriggersJSONArray()  {
        JSONArray jsonArray = new JSONArray();
        for (Trigger trigger : triggerClass.list) {
            try {
                jsonArray.put(trigger.toJson());
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }
        return jsonArray;
    }

    public static JSONArray getZonesJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (Zone zone : zones) {
            jsonArray.put(zone.toJson());
        }
        return jsonArray;
    }


    public void initMQTT() {

        LOGGER.info("initMQTT");

        // inizializzazione code MQTT
        smc = new SimpleMqttClient("CoreClient");
        smc.runClient();
        smc.subscribe("toServer/#");
        smc.subscribe("uuid/#");
        smc.addListener(this);
    }

    public void init() {

        LOGGER.info("start");

        // versione sw
        readSoftwareVersions();

        // inizializzazione schede.
        // Le schede ed isensori devono essere caricati prima degli scenari e zone altrimenti non funzionano i listener
        mShields = new Shields();
        mShields.init();
        //mSchedule = new Schedule(); // DA ELIMINARE

        // caricamento dati scernari e zone
        readWebduinoSystems();
        readZones();
        readTriggers();
        readExits();
        readKeys();
        // questa deve esserer chiamata dopo la creazione dei sensor altrimenti i listener non funzionano
        addZoneSensorListeners(); //

        scenarios.initScenarios();

        mShields.addListener(this);

        // DA ELIMINARE, non più usato
        Settings settings = new Settings();

        // inizializzazione client android remoti
        mDevices.read();
    }


    public void addZoneSensorListeners() {
        for (Zone zone : zones) {
            zone.addSensorListeners();
        }
    }

    public void clearZoneSensorListeners() {
        for (Zone zone : zones) {
            zone.clearSensorListeners();
        }
    }

    public void readSoftwareVersions() {

        LOGGER.info(" read software version");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM swversions";//" WHERE systemid=" + systemid;
            ResultSet swversionsResultSet = stmt.executeQuery(sql);
            // Extract data from result set
            swversions.clear();
            while (swversionsResultSet.next()) {
                int id = swversionsResultSet.getInt("id");
                String name = swversionsResultSet.getString("name");
                String version = swversionsResultSet.getString("version");
                String path = swversionsResultSet.getString("path");
                String filename = swversionsResultSet.getString("filename");
                SWVersion swversion = new SWVersion(id, name, version, path, filename);
                swversions.add(swversion);
            }
            swversionsResultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void readZones() {

        LOGGER.info(" readZoneSensors Security zones");

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM zones";//" WHERE systemid=" + systemid;
            ResultSet zonesResultSet = stmt.executeQuery(sql);
            // Extract data from result set
            zones.clear();
            while (zonesResultSet.next()) {
                ZoneFactory factory = new ZoneFactory();
                int id = zonesResultSet.getInt("id");
                String name = zonesResultSet.getString("name");
                String type = zonesResultSet.getString("type");
                Zone zone = factory.createWebduinoZone(id, name, type);
                if (zone != null)
                    zones.add(zone);
            }
            // Clean-up environment
            zonesResultSet.close();
            stmt.close();

            //schedule = new Schedule();
            //schedule.readZoneSensors(id);

            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    public static void readTriggers() {

        LOGGER.info(" readTriggers");

        try {
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM triggers";//" WHERE systemid=" + systemid;
            ResultSet triggersResultSet = stmt.executeQuery(sql);
            triggerClass.clear();
            while (triggersResultSet.next()) {
                ZoneFactory factory = new ZoneFactory();
                int id = triggersResultSet.getInt("id");
                String name = triggersResultSet.getString("name");
                String status = triggersResultSet.getString("status");
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.");
                Date date = null;
                if (triggersResultSet.getTimestamp("lastupdate") != null)
                    date = df.parse(String.valueOf(triggersResultSet.getTimestamp("lastupdate")));
                Trigger trigger = new Trigger(id, name, status, date);
                triggerClass.add(trigger);
            }
            // Clean-up environment
            triggersResultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    private void readWebduinoSystems() {
        LOGGER.info(" readWebduinoSystems");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM webduino_systems";
            ResultSet rs = stmt.executeQuery(sql);
            // Extract data from result set
            webduinoSystems = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                WebduinoSystemFactory factory = new WebduinoSystemFactory();
                WebduinoSystem system = factory.createWebduinoSystem(id, name, type);
                if (system != null)
                    webduinoSystems.add(system);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readExits() {
        LOGGER.info(" readWebduinoSystems");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM exits";
            ResultSet rs = stmt.executeQuery(sql);
            exits = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                int actuatorid = rs.getInt("sensorid");
                ExitFactory factory = new ExitFactory();
                Exit exit = factory.createWebduinoExit(id, name, type, actuatorid);
                if (exit != null)
                    exits.add(exit);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void readKeys() {
        LOGGER.info(" readWebduinoSystems");

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM webduino.keys";
            ResultSet rs = stmt.executeQuery(sql);
            keys = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                int actuatorid = rs.getInt("sensorid");
                KeyFactory factory = new KeyFactory();
                Key key = factory.createWebduinoKey(id, name, type, actuatorid);
                if (key != null)
                    keys.add(key);
            }
            rs.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    static public Scenario saveScenario(JSONObject json) throws Exception {
        Scenario scenario = new Scenario(json);
        scenario.save();
        scenarios.initScenarios();
        return scenario;
    }

    static public JSONArray removeScenario(JSONObject json) throws Exception {

        Scenario scenario = new Scenario(json);
        scenario.remove();
        scenarios.initScenarios();
        JSONArray jarray = Scenarios.getScenariosJSONArray();
        return jarray;
    }

    static public ScenarioProgram saveScenarioProgram(JSONObject json) throws Exception {
        ScenarioProgram program = new ScenarioProgram(json);
        program.save();
        scenarios.initScenarios();
        return program;
    }

    static public Scenario removeScenarioProgram(JSONObject json) throws Exception {

        ScenarioProgram program = new ScenarioProgram(json);
        int scenarioid = program.scenarioId;
        program.remove();
        scenarios.initScenarios();
        Scenario scenario = Scenarios.getScenarioFromId(scenarioid);
        return scenario;
    }

    static public Trigger saveTrigger(JSONObject json) throws Exception {
        Trigger trigger = new Trigger(json);
        trigger.save();
        scenarios.initScenarios();
        return trigger;
    }

    static public void enableTrigger(int id, boolean enable) throws Exception {
        Trigger trigger = getTriggerFromId(id);
        trigger.enable(enable);
        scenarios.initScenarios();
    }

    static public Trigger removeTrigger(JSONObject json) throws Exception {

        Trigger trigger = new Trigger(json);
        int triggerid = trigger.id;
        trigger.remove();
        Core.readTriggers();
        scenarios.initScenarios();
        return trigger;
    }

    static public Triggers saveTriggers(JSONObject json) throws Exception {
        Triggers triggers = new Triggers(json);
        triggers.save();
        Core.readTriggers();
        scenarios.initScenarios();
        return triggers;
    }

    static public Triggers removeTriggers(JSONObject json) throws Exception {

        Triggers triggers = new Triggers(json);
        //int triggerid = trigger.id;
        triggers.remove();
        Core.readTriggers();
        scenarios.initScenarios();
        return triggers;
    }

    static public ScenarioTrigger saveScenarioTrigger(JSONObject json) throws Exception {
        ScenarioTrigger trigger = new ScenarioTrigger(json);
        trigger.save();
        scenarios.initScenarios();
        return trigger;
    }

    static public Scenario removeScenarioTrigger(JSONObject json) throws Exception {

        ScenarioTrigger trigger = new ScenarioTrigger(json);
        int scenarioid = trigger.scenarioid;
        trigger.remove();
        scenarios.initScenarios();
        Scenario scenario = Scenarios.getScenarioFromId(scenarioid);
        return scenario;
    }

    static public ScenarioTimeInterval saveScenarioTimeinterval(JSONObject json) throws Exception {
        ScenarioTimeInterval timeInterval = new ScenarioTimeInterval(json);
        timeInterval.save();
        scenarios.initScenarios();
        return timeInterval;
    }

    static public Scenario removeScenarioTimeinterval(JSONObject json) throws Exception {

        ScenarioTimeInterval timeInterval = new ScenarioTimeInterval(json);
        int scenarioid = timeInterval.scenarioid;
        timeInterval.remove();
        scenarios.initScenarios();
        Scenario scenario = Scenarios.getScenarioFromId(scenarioid);
        return scenario;
    }

    static public ScenarioProgramTimeRange saveScenarioProgramTimeRange(JSONObject json) throws Exception {
        ScenarioProgramTimeRange timerange = new ScenarioProgramTimeRange(json);
        timerange.save();
        scenarios.initScenarios();
        return timerange;
    }

    static public ScenarioProgram removeScenarioProgramTimeRange(JSONObject json) throws Exception {
        ScenarioProgramTimeRange timerange = new ScenarioProgramTimeRange(json);
        int programid = timerange.programid;
        timerange.remove();
        scenarios.initScenarios();
        ScenarioProgram program = Scenarios.getScenarioProgramFromId(programid);
        return program;
    }

    static public ProgramAction saveScenarioProgramTimeRangeInstruction(JSONObject json) throws Exception {
        ProgramActionFactory factory = new ProgramActionFactory();
        ProgramAction action = factory.fromJson(json);
        action.save();
        scenarios.initScenarios();
        return action;
    }

    static public ScenarioProgramTimeRange removeScenarioProgramTimeRangeInstruction(JSONObject json) throws Exception {
        ProgramActionFactory factory = new ProgramActionFactory();
        ProgramAction instruction = factory.fromJson(json);
        int timerangeid = instruction.timerangeid;
        instruction.remove();
        scenarios.initScenarios();
        ScenarioProgramTimeRange timerange = Scenarios.getScenarioProgramTimeRangeFromId(timerangeid);
        return timerange;
    }

    static public Zone saveZone(JSONObject json) throws Exception {
        Zone zone = new Zone(json);
        zone.save();
        readZones();
        scenarios.initScenarios();
        return zone;
    }



    static public Zone removeZone(JSONObject json) throws Exception {
        Zone zone = new Zone(json);
        zone.remove();
        readZones();
        scenarios.initScenarios();
        return zone;
    }

    public static void sendPushNotification(String type, String title, String description, String value, int id) {

        LOGGER.info("sendPushNotification type=" + type + "title=" + title + "value=" + value);
        new PushNotificationThread(type, title, description, value, id).start();
        LOGGER.info("sendPushNotification sent");
    }

    public List<Shield> getShields() {
        return mShields.getShields();
    }

    public static List<SensorBase> getLastSensorData() {
        return mShields.getLastSensorData();
    }

    boolean updateSensors(int shieldid, JSONArray jsonArray) {
        return mShields.updateShieldSensors(shieldid, jsonArray);
    }

    static public boolean updateSensor(int id, JSONObject json) {
        return mShields.updateShieldSensor(id, json);
    }

    boolean updateShieldStatus(int shieldid, JSONObject json) {
        return mShields.updateShieldStatus(shieldid, json);
    }

    boolean updateSettings(int shieldid, JSONObject json) {
        return mShields.updateSettings(shieldid, json);
    }

    static public String getShieldSettingStatus(int shieldid) {
        return mShields.getSettingStatus(shieldid);
    }

    static public String getShieldSensorsStatus(int shieldid) {
        return mShields.getSensorStatus(shieldid);
    }

    public static SensorBase getFromShieldId(int shieldid, String subaddress) {
        return mShields.getFromShieldIdandSubaddress(shieldid, subaddress);
    }

    public static Shield getShieldFromId(int shieldid) {
        return mShields.fromId(shieldid);
    }

    public static boolean requestShieldSettingsUpdate(int shieldid) {
        return mShields.requestShieldSettingStatusUpdate(shieldid);
    }

    public static boolean requestShieldSensorsUpdate(int shieldid) {
        return mShields.requestShieldSensorsStatusUpdate(shieldid);
    }

    public ArrayList<Program> getPrograms(int systemId) {
        Schedule schedule = getWebduinoSystemSchedule(systemId);
        if (schedule != null)
            return schedule.getProgramList();
        return null;
    }

    public static int registerShield(Shield shield) {
        return mShields.register(shield);
    }

    public ArrayList<ActiveProgram> getNextActiveProgramlist() {
        return mSchedule.getActiveProgramList();
    }

    /*public static Program getProgramFromId(int id) {
        return mSchedule.getProgramFromId(id);
    }*/

    /*public ActiveProgram getActiveProgram(int id) {
        SensorBase sensor = getSensorFromId(id);
        return sensor.getActiveProgram();
    }*/

    /*public Date getLastActiveProgramUpdate() {
        return mSchedule.getLastActiveProgramUpdate();
    }*/

    public static SensorBase getSensorFromId(int id) {
        return mShields.getSensorFromId(id);
    }
    public static Trigger getTriggerFromId(int id) {
        return triggerClass.getFromId(id);
    }


    /*public int deleteProgram(int id) {
        return mSchedule.delete(id);
    }

    public int updatePrograms(Program program) {
        return mSchedule.insert(program);
    }*/

    public JSONArray getShieldsJsonArray() {
        return mShields.getShieldsJsonArray();
    }

    public static Date getDate() {

        LOGGER.info("getDate");
        Date date = new Date();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        df.setTimeZone(TimeZone.getTimeZone("Europe/Rome"));

        final String dateInString = df.format(date);


        Date newDate = null;
        try {

            newDate = df.parse(dateInString);

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return newDate;
    }

    public static LocalTime getTime() {
        Date time = getDate();
        Instant instant = Instant.ofEpochMilli(time.getTime());
        LocalTime res = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
        return res;
    }

    public static String getStrLastUpdate(Date date) {
        if (date == null)
            return "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(date);
    }

    public static String boolToString(boolean val) {
        if (val)
            return "true";
        else
            return "false";
    }

    @Override
    public void messageReceived(String topic, String message) {

        parseTopic(topic, message);

    }


    public void parseTopic(String topic, String message) {

        String[] list = topic.split("/");

        if (list == null) {
            return;
        }

        if (list[0].equals("toServer")) {
            if (list.length > 1) {
                if (list[1].equals("shield")) {
                    if (list.length > 2) {
                        int shieldid = Integer.parseInt(list[2]);
                        if (list.length > 3) {
                            String command = list[3];
                            callCommand(command, shieldid, message);
                        }
                    }
                } else if (list[1].equals("register")) {
                    callCommand("register", 0, message);
                } else if (list[1].equals("response")) {
                    if (list.length > 2) {
                        String uuid = list[2];
                        for (CoreListener listener : listeners) {
                            listener.onCommandResponse(uuid, message);
                        }
                    }
                }
            }
        } else if (list[0].equals("send")) {

        }
    }

    public boolean callCommand(String command, int shieldid, String json) {
        if (command.equals("sensorsupdate")) {
            try {
                JSONObject jsonObj = new JSONObject(json);
                updateShieldStatus(shieldid, jsonObj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } /*else if (command.equals("requestzonetemperature")) {
            try {
                JSONObject jsonObj = new JSONObject(json);
                if (jsonObj.has("shieldid") && jsonObj.has("zoneid")) {
                    int actuatorid = jsonObj.getInt("id");
                    int zoneid = jsonObj.getInt("zoneid");
                    sendTemperature(shieldid, actuatorid, zoneid);

                }
            } catch (JSONException e) {
                e.printStackTrace();
            }


        }*/ else if (command.equals("settingsupdate")) {
            try {
                JSONObject jsonObj = new JSONObject(json);
                updateSettings(shieldid, jsonObj);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (command.equals("register")) {
            try {
                JSONObject jsonObj = new JSONObject(json);
                if (jsonObj.has("shield")) {
                    JSONObject shieldJson = jsonObj.getJSONObject("shield");
                    Shield shield = new Shield();
                    shield.fromJson(shieldJson);
                    int id = registerShield(shield);
                    //SimpleMqttClient smc = new SimpleMqttClient();
                    return smc.publish("fromServer/shield/" + shield.MACAddress + "/registerresponse", "" + id);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return smc.publish("fromServer", "prova");
        }
        return false;
    }



    static public boolean publish(String topic, String message) {

        if (smc != null)
            return smc.publish(topic, message);

        return false;
    }

    public void mqttDisconnect() {
        smc.disconnect();
    }

    public static JSONObject getShieldSettingJson(int shieldid) {
        return mShields.getShieldSettingJson(shieldid);
    }

    public static JSONObject getShieldSensorsJson(int shieldid) {
        return mShields.getShieldSensorsJson(shieldid);
    }

    public static boolean postCommand(Command command) {
        return mShields.postCommand(command);
    }


    public static JSONObject loadShieldSettings(String macAddress) {
        return mShields.loadShieldSettings(macAddress);
    }

    public static boolean saveShieldSettings(JSONObject json) {
        return mShields.saveShieldSettings(json);
    }

    public Schedule getWebduinoSystemSchedule(int systemId) {
        for (WebduinoSystem system : webduinoSystems) {
            return system.getSchedule();
        }
        return null;
    }

    @Override
    public void addedSensor(SensorBase sensor) {

    }

    @Override
    public void addedShield(Shield shield) {

    }

    @Override
    public void updatedSensor(SensorBase sensor) {

    }

    @Override
    public void updatedShields() {
        // se cambiano i sensori riregistra i listener
        clearZoneSensorListeners();
        //securitySystem.clearZoneSensorListeners();


        addZoneSensorListeners();
        //securitySystem.addZoneSensorListeners();
    }
}
