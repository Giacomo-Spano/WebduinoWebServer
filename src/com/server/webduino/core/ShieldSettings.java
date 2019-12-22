package com.server.webduino.core;

//import com.mysql.fabric.xmlrpc.base.Data;
import com.server.webduino.core.sensors.TemperatureSensor;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by giaco on 30/04/2017.
 */
public class ShieldSettings {
    public Date lastUpdate;
    public Date lastRestart;
    public String swversion;
    public String networkSSID;
    public String networkPassword;
    public int localPort;
    public String serverName;
    public int serverPort;
    public String shieldName;
    public String localIpAddress;
    public String MACAddrss;
    public String powerStatus;
    public int heapSize;
    public int shieldId;

    private static Logger LOGGER = Logger.getLogger(TemperatureSensor.class.getName());

    public boolean updateFromJson(Date date, JSONObject json) {

        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            lastUpdate = date;
            if (json.has("swversion"))
                swversion = json.getString("swversion");
            if (json.has("ssid"))
                networkSSID = json.getString("ssid");
            if (json.has("shieldname"))
                shieldName = json.getString("shieldname");
            if (json.has("password"))
                networkPassword = json.getString("password");
            if (json.has("serverport"))
                serverPort = json.getInt("serverport");
            if (json.has("servername"))
                serverName = json.getString("servername");
            if (json.has("localip"))
                localIpAddress = json.getString("localip");
            if (json.has("shieldid"))
                shieldId = json.getInt("shieldid");
            if (json.has("macaddress"))
                MACAddrss = json.getString("macaddress");
            if (json.has("power"))
                powerStatus = json.getString("power");
            if (json.has("heap"))
                heapSize = json.getInt("heap");

            if (json.has("localport"))
                localPort = json.getInt("localport");
            /*if (json.has("lastrestart"))
                lastRestart = json.getString("swversion");*/

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            return false;
        }
        return true;
    }

    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("swversion", swversion);
            json.put("ssid", networkSSID);
            json.put("shieldname", shieldName);
            json.put("password", networkPassword);
            json.put("serverport", serverPort);
            json.put("servername", serverName);
            json.put("localip", localIpAddress);
            json.put("shieldid", shieldId);
            json.put("macaddress", MACAddrss);
            json.put("power", powerStatus);
            json.put("heap", heapSize);
            json.put("localport", localPort);
            json.put("lastrestart", "");

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
