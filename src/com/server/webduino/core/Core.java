package com.server.webduino.core;

import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Core implements SampleAsyncCallBack.SampleAsyncCallBackListener, SimpleMqttClient.SimpleMqttClientListener {

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

    public static Shields mShields;
    public static Programs mPrograms;

    public static Devices mDevices = new Devices();

    //private MQTTThread mqttThreadReceive;
    static SimpleMqttClient smc;

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

    public void init() {

        LOGGER.info("init");


        //File scratchFile = new File(System.getProperty("java.io.tmpdir") + "WCCTempFile.tmp");
        /*String dir = System.getenv("tmp");
        String file = dir + "prova.txt";
        File scratchFile = new File(file);
        if (!scratchFile.exists())
            try {
                scratchFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        smc = new SimpleMqttClient("CoreClient");
        smc.runClient();
        smc.subscribe("toServer/#");
        smc.addListener(this);

        mShields = new Shields();
        mShields.init();

        // DA ELIMINARE, non più usato
        Settings settings = new Settings();

        mDevices.read();



        //mqttThreadReceive = new MQTTThread(this, "CoreClientReceive", "toServer/#");
        //mqttThreadReceive.start();

        //mqttThreadSend = new MQTTThread(this,false);
        //mqttThreadSend.start();
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
        //return mShields.updateSensors(shieldid, jsonArray);
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


    public ArrayList<Program> getPrograms() {
        return mPrograms.getProgramList();
    }

    public static int registerShield(Shield shield) {
        return mShields.register(shield);
    }

    public ArrayList<ActiveProgram> getNextActiveProgramlist() {
        return mPrograms.getActiveProgramList();
    }

    public static Program getProgramFromId(int id) {
        return mPrograms.getProgramFromId(id);
    }

    public ActiveProgram getActiveProgram(int id) {
        SensorBase sensor = getSensorFromId(id);
        return sensor.getActiveProgram();
        //return mPrograms.getActiveProgram(id);
    }

    public Date getLastActiveProgramUpdate() {
        return mPrograms.getLastActiveProgramUpdate();
    }

    public static SensorBase getSensorFromId(int id) {
        return mShields.getSensorFromId(id);
    }

    public int deleteProgram(int id) {
        return mPrograms.delete(id);
    }

    public int updatePrograms(Program program) {
        return mPrograms.insert(program);
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

    public static boolean postCommand(int shieldid, ShieldCommand command) {
        return mShields.postCommand(shieldid,command);
    }


}
