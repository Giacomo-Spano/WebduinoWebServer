package com.server.webduino.core;

import com.server.webduino.core.datalog.DataLog;
import com.server.webduino.core.sensors.Actuator;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.*;
import com.server.webduino.core.webduinosystem.exits.Exit;
import com.server.webduino.core.webduinosystem.exits.ExitFactory;
import com.server.webduino.core.webduinosystem.keys.Key;
import com.server.webduino.core.webduinosystem.keys.KeyFactory;
import com.server.webduino.core.webduinosystem.scenario.*;
import com.server.webduino.core.webduinosystem.scenario.actions.Action;
import com.server.webduino.core.webduinosystem.scenario.actions.Condition;
import com.server.webduino.core.webduinosystem.services.Service;
import com.server.webduino.core.webduinosystem.services.ServiceFactory;
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
public class Core {

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
    private static List<Service> services = new ArrayList<>();
    private static Triggers triggerClass = new Triggers();
    private Scenarios scenarios = new Scenarios();
    private List<Exit> exits = new ArrayList<>();
    private List<Key> keys = new ArrayList<>();
    public static Shields mShields; // rendere private
    private static List<SWVersion> swversions = new ArrayList<>();

    public static Devices mDevices = new Devices();

    static SimpleMqttClient smc;

    public static Trigger triggerFromId(int triggerid) {
        for (Trigger trigger : triggerClass.list) {
            if (trigger.id == triggerid)
                return trigger;
        }
        return null;
    }

    public List<NextTimeRangeAction> getNextTimeRangeActions(/*int scenarioProgramId*/) {
        return scenarios.nextTimeRangeActions;
    }

    public List<DataLog> getCommandDatalogs(int actuatorId, Date start, Date end) {
        SensorBase sensor = getSensorFromId(actuatorId);
        if (sensor != null && sensor instanceof Actuator) {

            Actuator actuator = (Actuator) sensor;
            if (actuator.command.commandDataLog != null)
                return actuator.command.commandDataLog.getDataLog(actuatorId, start, end);
        }
        return null;

    }

    public void initScenarios() {

        if (scenarios.scenarioList != null) {
            for (WebduinoSystemScenario scenario : scenarios.scenarioList) {
                scenario.stop();
            }
        }

        readWebduinoSystems();
        scenarios.scenarioList.clear();
        scenarios.scenarioList = getWebduinoSystemScenarios();
        for (WebduinoSystemScenario scenario:scenarios.scenarioList) {
            scenario.setActionListener(new Action.ActionListener() {
                @Override
                public void onStart(Action action) {
                    scenarios.checkConflict(action);
                }

                @Override
                public void onStop(Action action) {
                    scenarios.removeConflict(action);
                }
            });
            scenario.start();
        }


        scenarios.checkNextTimeRangeActions(Core.getDate());
    }

    public Condition removeCondition(JSONObject json) throws Exception {
        Condition condition = new Condition(json);
        condition.remove();
        initScenarios();
        return condition;
    }

    public Action removeAction(JSONObject json) throws Exception {
        Action action = new Action(json);
        action.remove();
        initScenarios();
        return action;
    }

    public Action saveAction(JSONObject json) throws Exception {
        Action action = new Action(json);
        action.save();
        initScenarios();
        return action;
    }

    public Condition saveCondition(JSONObject json) throws Exception {
        Condition condition = new Condition(json);
        condition.save();
        initScenarios();
        return condition;
    }

    public interface CoreListener {
        void onCommandResponse(String uuid, String response);
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

    public static WebduinoSystem getWebduinoSystemFromId(int systemid) {
        for (WebduinoSystem system : webduinoSystems) {
            if (systemid == system.getId()) {
                return system;
            }
        }
        return null;
    }

    public WebduinoSystemActuator getWebduinoSystemActuatorFromId(int id) {
        for (WebduinoSystem system : webduinoSystems) {
            for (WebduinoSystemActuator webduinoSystemActuator: system.actuators) {
                if (webduinoSystemActuator.actuatorid == id)
                    return webduinoSystemActuator;
            }
        }
        return null;
    }

    public WebduinoSystemZone getWebduinoSystemZoneFromId(int id) {
        for (WebduinoSystem system : webduinoSystems) {
            for (WebduinoSystemZone webduinoSystemZone: system.zones) {
                if (webduinoSystemZone.zoneid == id)
                    return webduinoSystemZone;
            }
        }
        return null;
    }

    public WebduinoSystemService getWebduinoSystemServiceFromId(int id) {
        for (WebduinoSystem system : webduinoSystems) {
            for (WebduinoSystemService webduinoSystemService: system.services) {
                if (webduinoSystemService.serviceid == id)
                    return webduinoSystemService;
            }
        }
        return null;
    }

    public List<WebduinoSystemScenario> getWebduinoSystemScenarios() {
        List<WebduinoSystemScenario> list = new ArrayList<>();
        for (WebduinoSystem system : webduinoSystems) {
            for (WebduinoSystemScenario scenario: system.getScenarios()) {
                list.add(scenario);
            }
        }
        return list;
    }


    public static Service getServiceFromId(int serviceid) {
        for (Service service : services) {
            if (serviceid == service.getId()) {
                return service;
            }
        }
        return null;
    }


    public static JSONArray getServicesJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (Service service : services) {
            jsonArray.put(service.toJson());
        }
        return jsonArray;
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

    public static JSONArray getTriggersJSONArray() {
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

    public JSONArray getZonesJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (Zone zone : zones) {
            jsonArray.put(zone.toJson());
        }
        return jsonArray;
    }

    public JSONArray getWebduinoSystemJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (WebduinoSystem system : webduinoSystems) {
            jsonArray.put(system.toJson());
        }
        return jsonArray;
    }

   /* public WebduinoSystem getWebduinoSystemFromId(int id) {
        for (WebduinoSystem system : webduinoSystems) {
           if(system.id == id)
               return system;
        }
        return null;
    }*/


    public void initMQTT() {

        LOGGER.info("initMQTT");

        // inizializzazione code MQTT
        smc = new SimpleMqttClient("CoreClient");
        smc.runClient();
        //smc.subscribe("toServer/#");
        //smc.subscribe("uuid/#");
        //smc.addListener(this);
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
        readZones();
        readServices();
        readWebduinoSystems();

        readTriggers();
        readExits();
        readKeys();
        // questa deve esserer chiamata dopo la creazione dei sensor altrimenti i listener non funzionano
        addZoneSensorListeners(); //

        initScenarios();
        //mShields.addListener(this);

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
                //boolean status = triggersResultSet.getBoolean("status");
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.");
                Date date = null;
                if (triggersResultSet.getTimestamp("lastupdate") != null)
                    date = df.parse(String.valueOf(triggersResultSet.getTimestamp("lastupdate")));
                Trigger trigger = new Trigger(id, name, date);
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

    public void readServices() {

        LOGGER.info(" readServices");

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM services";//" WHERE systemid=" + systemid;
            ResultSet servicesResultSet = stmt.executeQuery(sql);
            // Extract data from result set
            services.clear();
            while (servicesResultSet.next()) {
                ServiceFactory factory = new ServiceFactory();
                int id = servicesResultSet.getInt("id");
                String name = servicesResultSet.getString("name");
                String type = servicesResultSet.getString("type");
                Service service = factory.createService(id, name, type);
                if (service != null)
                    services.add(service);
            }
            // Clean-up environment
            servicesResultSet.close();
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

    public void readWebduinoSystems() {
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
                boolean enabled = rs.getBoolean("enabled");
                WebduinoSystemFactory factory = new WebduinoSystemFactory();
                WebduinoSystem system = factory.createWebduinoSystem(id, name, type, enabled);
                if (system != null) {
                    system.readWebduinoSystemsZones(conn, id);
                    system.readWebduinoSystemsActuators(conn, id);
                    system.readWebduinoSystemsServices(conn, id);
                    system.readWebduinoSystemsScenarios(conn, id);
                    webduinoSystems.add(system);
                }
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
        LOGGER.info(" readWebduinoExits");

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

    public WebduinoSystemScenario saveScenario(JSONObject json) throws Exception {
        WebduinoSystemScenario scenario = new WebduinoSystemScenario(json);
        scenario.save();
        initScenarios();
        return scenario;
    }

    public JSONArray removeScenario(JSONObject json) throws Exception {

        WebduinoSystemScenario scenario = new WebduinoSystemScenario(json);
        scenario.remove();
        initScenarios();
        JSONArray jarray = Scenarios.getScenariosJSONArray();
        return jarray;
    }

    public ScenarioProgram saveScenarioProgram(JSONObject json) throws Exception {
        ScenarioProgram program = new ScenarioProgram(json);
        program.save();
        initScenarios();
        return program;
    }

    public WebduinoSystemScenario removeScenarioProgram(int programid) throws Exception {

        ScenarioProgram program = Scenarios.getScenarioProgramFromId(programid);
        if  (program != null) {
            int scenarioid = program.scenarioId;
            program.remove();
            initScenarios();
            WebduinoSystemScenario scenario = Scenarios.getScenarioFromId(scenarioid);
            return scenario;
        }
        return null;
    }

    public Trigger saveTrigger(JSONObject json) throws Exception {
        Trigger trigger = new Trigger(json);
        trigger.save();
        initScenarios();
        return trigger;
    }

    /*public void enableTrigger(int id, boolean enable) throws Exception {
        Trigger trigger = getTriggerFromId(id);
        if (trigger == null)
            throw new Exception("trigger id " + id + " not found");
        trigger.enable(enable);
        scenarios.initScenarios();
    }*/

    public Trigger removeTrigger(JSONObject json) throws Exception {

        Trigger trigger = new Trigger(json);
        trigger.remove();
        Core.readTriggers();
        initScenarios();
        return trigger;
    }

    public Triggers saveTriggers(JSONObject json) throws Exception {
        Triggers triggers = new Triggers(json);
        triggers.save();
        Core.readTriggers();
        initScenarios();
        return triggers;
    }

    public Triggers removeTriggers(JSONObject json) throws Exception {

        Triggers triggers = new Triggers(json);
        //int triggerid = trigger.id;
        triggers.remove();
        Core.readTriggers();
        initScenarios();
        return triggers;
    }

    public ScenarioTrigger saveScenarioTrigger(JSONObject json) throws Exception {
        ScenarioTrigger trigger = new ScenarioTrigger(json);
        trigger.save();
        initScenarios();
        return trigger;
    }

    public WebduinoSystemScenario removeScenarioTrigger(JSONObject json) throws Exception {

        ScenarioTrigger trigger = new ScenarioTrigger(json);
        int scenarioid = trigger.scenarioid;
        trigger.remove();
        initScenarios();
        WebduinoSystemScenario scenario = Scenarios.getScenarioFromId(scenarioid);
        return scenario;
    }

    public WebduinoSystem saveWebduinoSystem(JSONObject json) throws Exception {
        WebduinoSystem webduinoSystem = new WebduinoSystem(json);
        webduinoSystem.save();
        initScenarios();
        return webduinoSystem;
    }

    public WebduinoSystem removeWebduinoSystem(JSONObject json) throws Exception {

        WebduinoSystem webduinoSystem = new WebduinoSystem(json);
        webduinoSystem.remove();
        initScenarios();
        return webduinoSystem;
    }

    public WebduinoSystemService saveWebduinoSystemService(JSONObject json) throws Exception {
        WebduinoSystemService webduinoSystemService = new WebduinoSystemService(json);
        webduinoSystemService.save();
        initScenarios();
        return webduinoSystemService;
    }

    public WebduinoSystem removeWebduinoSystemService(JSONObject json) throws Exception {

        WebduinoSystemService webduinoSystemService = new WebduinoSystemService(json);
        int serviceid = webduinoSystemService.serviceid;
        webduinoSystemService.remove();
        initScenarios();
        WebduinoSystem webduinoSystem = getWebduinoSystemFromId(webduinoSystemService.webduinosystemid);
        return webduinoSystem;
    }

    public WebduinoSystemZone saveWebduinoSystemZone(JSONObject json) throws Exception {
        WebduinoSystemZone webduinoSystemZone = new WebduinoSystemZone(json);
        webduinoSystemZone.save();
        initScenarios();
        return webduinoSystemZone;
    }

    public WebduinoSystem removeWebduinoSystemZone(JSONObject json) throws Exception {

        WebduinoSystemZone webduinoSystemZone = new WebduinoSystemZone(json);
        int zoneid = webduinoSystemZone.zoneid;
        webduinoSystemZone.remove();
        initScenarios();
        WebduinoSystem webduinoSystem = getWebduinoSystemFromId(webduinoSystemZone.webduinosystemid);
        return webduinoSystem;
    }

    public WebduinoSystemActuator saveWebduinoSystemActuator(JSONObject json) throws Exception {
        WebduinoSystemActuator webduinoSystemActuator = new WebduinoSystemActuator(json);
        webduinoSystemActuator.save();
        initScenarios();
        return webduinoSystemActuator;
    }

    public WebduinoSystem removeWebduinoSystemActuator(JSONObject json) throws Exception {

        WebduinoSystemActuator webduinoSystemActuator = new WebduinoSystemActuator(json);
        int actuatorid = webduinoSystemActuator.actuatorid;
        webduinoSystemActuator.remove();
        initScenarios();
        WebduinoSystem webduinoSystem = getWebduinoSystemFromId(webduinoSystemActuator.webduinosystemid);
        return webduinoSystem;
    }


    public ScenarioTimeInterval saveScenarioTimeinterval(JSONObject json) throws Exception {
        ScenarioTimeInterval timeInterval = new ScenarioTimeInterval(json);
        timeInterval.save();
        initScenarios();
        return timeInterval;
    }

    public WebduinoSystemScenario removeScenarioTimeinterval(JSONObject json) throws Exception {

        ScenarioTimeInterval timeInterval = new ScenarioTimeInterval(json);
        int scenarioid = timeInterval.scenarioid;
        timeInterval.remove();
        initScenarios();
        WebduinoSystemScenario scenario = Scenarios.getScenarioFromId(scenarioid);
        return scenario;
    }

    public ScenarioProgramTimeRange saveScenarioProgramTimeRange(JSONObject json) throws Exception {
        ScenarioProgramTimeRange timerange = new ScenarioProgramTimeRange(json);
        timerange.save();
        initScenarios();
        return timerange;
    }

    public ScenarioProgram removeScenarioProgramTimeRange(JSONObject json) throws Exception {
        ScenarioProgramTimeRange timerange = new ScenarioProgramTimeRange(json);
        int programid = timerange.programid;
        timerange.remove();
        initScenarios();
        ScenarioProgram program = Scenarios.getScenarioProgramFromId(programid);
        return program;
    }

    public Zone saveZone(JSONObject json) throws Exception {
        Zone zone = new Zone(json);
        zone.save();
        readZones();
        initScenarios();
        return zone;
    }

    public Zone removeZone(JSONObject json) throws Exception {
        Zone zone = new Zone(json);
        zone.remove();
        readZones();
        initScenarios();
        return zone;
    }

    public NextTimeRangeAction getNextActuatorProgramTimeRange(int actuatorid) {
        return scenarios.getNextActuatorProgramTimeRangeAction(actuatorid);
    }

    public List<NextTimeRangeAction> getNextActuatorProgramTimeRangeActionList(int actuatorid) {
        return scenarios.getNextActuatorProgramTimeRangeActionList(actuatorid);
    }

    public List<DataLog> getSensorDataLogList(int actuatorid, Date startdate, Date enddate) {

        SensorBase sensor = getSensorFromId(actuatorid);
        if (sensor == null) return null;
        return sensor.datalog.getDataLog(actuatorid, startdate, enddate);
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

    static public boolean updateShieldStatus(int shieldid, JSONObject json) {
        return mShields.updateShieldStatus(shieldid, json);
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

    public static void requestShieldSensorsUpdate(int shieldid) {

        Shield shield = getShieldFromId(shieldid);
        if (shield != null)
            shield.requestAsyncAllSensorStatusUpdate();
    }

    public static SensorBase getSensorFromId(int id) {
        return mShields.getSensorFromId(id);
    }

    public static Trigger getTriggerFromId(int id) {
        return triggerClass.getFromId(id);
    }

    public JSONArray getShieldsJsonArray() {
        return mShields.getShieldsJsonArray();
    }

    public static Date getDate() {

        //LOGGER.info("getDate");
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

    public static JSONObject loadShieldSettings(String macAddress) {
        return mShields.loadShieldSettings(macAddress);
    }
}
