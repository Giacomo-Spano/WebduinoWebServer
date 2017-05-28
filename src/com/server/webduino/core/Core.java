package com.server.webduino.core;

import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.*;
import com.server.webduino.core.webduinosystem.exits.Exit;
import com.server.webduino.core.webduinosystem.exits.ExitFactory;
import com.server.webduino.core.webduinosystem.keys.Key;
import com.server.webduino.core.webduinosystem.keys.KeyFactory;
import com.server.webduino.core.webduinosystem.programinstructions.ProgramInstructions;
import com.server.webduino.core.webduinosystem.programinstructions.ProgramInstructionsFactory;
import com.server.webduino.core.webduinosystem.scenario.Scenario;
import com.server.webduino.core.webduinosystem.scenario.ScenarioTimeInterval;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Core implements SampleAsyncCallBack.SampleAsyncCallBackListener, SimpleMqttClient.SimpleMqttClientListener,Shields.ShieldsListener {

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

    private static List <WebduinoSystem> webduinoSystems = new ArrayList<>();
    private static List<Zone> zones = new ArrayList<>();
    private static List<Scenario> scenarios = new ArrayList<>();
    private static List<Exit> exits = new ArrayList<>();
    private static List<Key> keys = new ArrayList<>();
    public static Shields mShields; // rendere private
    public static Schedule mSchedule;// DA ELIMINARE

    public static Devices mDevices = new Devices();

    static SimpleMqttClient smc;

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
        if (tmpDir.equals("C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\temp")  ||
                tmpDir.equals("C:\\Program Files\\Apache Software Foundation\\Tomcat 7.0\\temp"))
            return false;
        else
            return true;
    }

    public static Zone getZoneFromId(int zoneid) {

        for(Zone zone : zones) {
                if (zoneid == zone.getId()) {
                    return zone;
                }
        }
        return null;
    }

    public static JSONArray getScenariosJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for(Scenario scenario : scenarios) {
            jsonArray.put(scenario.toJSON());
        }
        return jsonArray;
    }

    public JSONArray getTimeIntervalsJSONArray(int id) {
        JSONArray jsonArray = new JSONArray();
        Scenario.ScenarioCalendar calendar = getScenarioFromId(id).calendar;
        for(ScenarioTimeInterval timeinterval : calendar.timeIntervals) {
            jsonArray.put(timeinterval.toJson());
        }
        return jsonArray;
    }

    public static Scenario getScenarioFromId(int id) {
        for (Scenario scenario : scenarios) {
            if (scenario.id == id) {
                return scenario;
            }
        }
        return null;
    }

    public static List<ProgramInstructions> getProgramInstructions(int timeintervalid) {
        for (Scenario scenario : scenarios) {
            for (ScenarioTimeInterval timeInterval: scenario.calendar.timeIntervals) {
                if (timeInterval.id == timeintervalid) {
                    return timeInterval.programInstructionsList;
                }
            }
        }
        return null;
    }

    public static JSONArray getZonesJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for(Zone zone : zones) {
            jsonArray.put(zone.toJSON());
        }
        return jsonArray;
    }

    public void init() {

        LOGGER.info("init");

        smc = new SimpleMqttClient("CoreClient");
        smc.runClient();
        smc.subscribe("toServer/#");
        smc.subscribe("uuid/#");
        smc.addListener(this);

        mShields = new Shields();
        mShields.init();

        mSchedule = new Schedule(); // DA ELIMINARE

        readWebduinoSystems();
        readZones();
        readExits();
        readKeys();
        // questa deve esserer chiamata dopo la creazione dei sensor altrimenti i listener non funzionano
        addZoneSensorListeners();

        readScenarios();
        for (Scenario scenario: scenarios) {
            scenario.init();
        }

        mShields.addListener(this);

        // DA ELIMINARE, non più usato
        Settings settings = new Settings();

        mDevices.read();
    }

    public void addZoneSensorListeners() {
        for(Zone zone: zones) {
            zone.addSensorListeners();
        }
    }

    public void clearZoneSensorListeners() {
        for(Zone zone: zones) {
            zone.clearSensorListeners();
        }
    }

    public void readZones() {

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
            ResultSet rs = stmt.executeQuery(sql);
            // Extract data from result set
            zones.clear();
            while (rs.next()) {
                ZoneFactory factory = new ZoneFactory();
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                Zone zone = factory.createWebduinoZone(id, name, type);
                if (zone != null)
                    zones.add(zone);
            }
            // Clean-up environment
            rs.close();
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
                WebduinoSystem system = factory.createWebduinoSystem(id,name,type);
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
                Exit exit = factory.createWebduinoExit(id,name,type,actuatorid);
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
            sql = "SELECT * FROM keys";
            ResultSet rs = stmt.executeQuery(sql);
            keys = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                int actuatorid = rs.getInt("sensorid");
                KeyFactory factory = new KeyFactory();
                Key key = factory.createWebduinoKey(id,name,type,actuatorid);
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

    private void readScenarios() {
        LOGGER.info("readScenarios");
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM scenarios" + " ORDER BY priority ASC;";;
            ResultSet rs = stmt.executeQuery(sql);
            scenarios = new ArrayList<>();
            while (rs.next()) {
                Scenario scenario = new Scenario();
                scenario.id = rs.getInt("id");
                scenario.name = rs.getString("name");
                scenario.calendar.setDateEnabled(rs.getBoolean("dateenabled"));
                scenario.calendar.setStartDate(rs.getDate("startdate"));
                scenario.calendar.setEndDate(rs.getDate("enddate"));

                Statement stmt2 = conn.createStatement();
                String sql2 = "SELECT * FROM scenarios_timeintervals WHERE scenarioid=" + scenario.id + " ORDER BY priority ASC";
                ResultSet rs2 = stmt2.executeQuery(sql2);
                while (rs2.next()) {
                    ScenarioTimeInterval timeInterval = new ScenarioTimeInterval();
                    timeInterval.id = rs2.getInt("id");
                    timeInterval.scenarioId = rs2.getInt("scenarioid");
                    timeInterval.name = rs2.getString("name");
                    timeInterval.startTime = rs2.getTime("starttime");
                    timeInterval.endTime = rs2.getTime("endtime");

                    timeInterval.setSunday(rs2.getBoolean("sunday"));
                    timeInterval.setMonday(rs2.getBoolean("monday"));
                    timeInterval.setTuesday(rs2.getBoolean("tuesday"));
                    timeInterval.setWednesday(rs2.getBoolean("wednesday"));
                    timeInterval.setThursday(rs2.getBoolean("thursday"));
                    timeInterval.setFriday(rs2.getBoolean("friday"));
                    timeInterval.setSaturday(rs2.getBoolean("saturday"));
                    timeInterval.setPriority(rs2.getInt("priority"));

                    //timeInterval.programInstructions = rs2.getInt("programinstructionsid");
                    timeInterval.priority = rs2.getInt("priority");
                    scenario.calendar.addTimeIntervals(timeInterval);

                    Statement stmt3 = conn.createStatement();
                    String sql3 = "SELECT * FROM program_instructions WHERE timeintervalid=" + timeInterval.id + " ;";
                    ResultSet rs3 = stmt3.executeQuery(sql3);

                    ProgramInstructionsFactory factory = new ProgramInstructionsFactory();
                    while (rs3.next()) {

                        int id = rs3.getInt("id");
                        String type = rs3.getString("type");
                        String name = rs3.getString("name");
                        int actuatorid = rs3.getInt("actuatorid");
                        float targetValue = rs3.getFloat("targetvalue");
                        int zoneId = rs3.getInt("zoneid");

                        int seconds = 0;
                        Time time = rs3.getTime("time");
                        if (time != null) {
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(time);
                            seconds = cal.get(Calendar.SECOND);
                        }
                        //ZoneProgram zoneProgram = factory.createZoneProgram(programId,programName,type,cal.get(Calendar.SECOND));
                        ProgramInstructions programInstructions = factory.createProgramInstructions(id, name, type, actuatorid, targetValue, zoneId, seconds);
                        if (programInstructions != null) {
                            timeInterval.programInstructionsList.add(programInstructions);
                        }
                    }
                    rs3.close();
                    stmt3.close();
                }
                rs2.close();
                stmt2.close();

                scenarios.add(scenario);
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

    public static void sendPushNotification(String type, String title, String description, String value, int id) {

        LOGGER.info("sendPushNotification type=" + type + "title=" + title + "value=" + value);
        new PushNotificationThread(type, title, description, value, id).start();
        LOGGER.info("sendPushNotification sent");
    }

    public List<Shield> getShields() {
        return mShields.getShields();
    }

    public List<SensorBase> getLastSensorData() {
        return mShields.getLastSensorData();
    }

    boolean updateSensors(int shieldid, JSONArray jsonArray) {
        return mShields.updateShieldSensors(shieldid, jsonArray);
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

    public static Program getProgramFromId(int id) {
        return mSchedule.getProgramFromId(id);
    }

    public ActiveProgram getActiveProgram(int id) {
        SensorBase sensor = getSensorFromId(id);
        return sensor.getActiveProgram();
    }

    public Date getLastActiveProgramUpdate() {
        return mSchedule.getLastActiveProgramUpdate();
    }

    public static SensorBase getSensorFromId(int id) {
        return mShields.getSensorFromId(id);
    }

    public int deleteProgram(int id) {
        return mSchedule.delete(id);
    }

    public int updatePrograms(Program program) {
        return mSchedule.insert(program);
    }

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
                } else if (list[1].equals("response")){
                    if (list.length > 2) {
                        String uuid = list[2];
                        for (CoreListener listener: listeners){
                            listener.onCommandResponse(uuid,message);
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

                if (jsonObj.has("sensors")) {
                    JSONArray jsonArray = jsonObj.getJSONArray("sensors");
                    updateSensors(shieldid, jsonArray);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else if (command.equals("settingsupdate")) {
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
                    shield.FromJson(shieldJson);
                    int id = registerShield(shield);
                    //SimpleMqttClient smc = new SimpleMqttClient();
                    return smc.publish("fromServer/shield/" + shield.MACAddress + "/registerresponse", ""+id);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            //SimpleMqttClient smc = new SimpleMqttClient();
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
