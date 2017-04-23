package com.server.webduino.core;

import com.server.webduino.core.sensors.Actuator;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

//import static com.server.webduino.core.sensors.SensorBase.Status_Offline;

public class Shield extends httpClient {

    private static Logger LOGGER = Logger.getLogger(Shield.class.getName());

    protected int id;
    protected String MACAddress;
    protected String boardName;
    protected Date lastUpdate;
    protected List<SensorBase> sensors = new ArrayList<>();
    public URL url;
    public int port;

    protected String statusUpdatePath = "/sensorstatus";

    public Shield() {
    }

    public String requestStatusUpdate() { //

        LOGGER.info("requestStatusUpdate:" + statusUpdatePath);

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

    protected Result call(String method, String param, String path) {

        LOGGER.info("call: " + method + "," + param + "," + path);
        LOGGER.info("url: " + url.toString());

        Result result = null;
        if (method.equals("GET")) {
            result = callGet(param, path, url);
        } else if (method.equals("POST")) {
            result = callPost(param, path, url);
        }

        LOGGER.info("end call");
        return result;
    }

    /*public boolean sensorsIsNotUpdated() {

        Date currentDate = Core.getDate();
        boolean res = false;
        for (SensorBase sensors : sensors) {
            //SensorBase s = Shields.getSensorFromId(id);
            if (sensors.lastUpdate == null || (currentDate.getTime() - sensors.lastUpdate.getTime()) > (30*1000) ) {
                sensors.onlinestatus = Status_Offline;
                res = true;
            }
        }
        return res;
    }

    public boolean actuatorsIsNotUpdated() {

        Date currentDate = Core.getDate();
        boolean res = false;
        for (Actuator actuator : actuators) {
            //SensorBase s = Shields.getActuatorFromId(id);
            if (actuator.lastUpdate == null || (currentDate.getTime() - actuator.lastUpdate.getTime()) > (30*1000) ) {
                actuator.onlinestatus = Status_Offline;
                res = true;
            }
        }
        return res;
    }*/

    public boolean FromJson(JSONObject json) {

        try {
            Date date = Core.getDate();
            lastUpdate = date;
            if (json.has("MAC"))
                MACAddress = json.getString("MAC");
            if (json.has("shieldName"))
                boardName = json.getString("shieldName");
            if (json.has("localport"))
                port = json.getInt("localport");
            else
                port = 80;
            if (json.has("localIP")) {
                try {
                    url = new URL("http://" + json.getString("localIP"));
                    if (url.equals(new URL("http://0.0.0.0"))) {
                        LOGGER.info("url error: " + url.toString());
                        return false;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    LOGGER.info("url error: " + e.toString());
                    return false;
                }
            }
            if (json.has("sensors")) {
                JSONArray jsonArray = json.getJSONArray("sensors");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject j = jsonArray.getJSONObject(i);
                    if (j.has("type")) {
                        String type = j.getString("type");
                        String name = "";
                        String subaddress = "";
                        if (j.has("name"))
                            name = j.getString("name");
                        if (j.has("addr"))
                            subaddress = j.getString("addr");

                        SensorBase sensor = SensorFactory.createSensor(type, name, subaddress, 0, 0);
                        if (sensor == null) {
                            continue;
                        } else {

                            if (j.has("childsensors")) {
                                JSONArray tempSensorArray = j.getJSONArray("childsensors");
                                for (int k = 0; k < tempSensorArray.length(); k++) {

                                    String childSubaddress = "";
                                    if (j.has("addr"))
                                        childSubaddress = tempSensorArray.getJSONObject(k).getString("addr");

                                    String childName = "";
                                    if (j.has("name"))
                                        childName = tempSensorArray.getJSONObject(k).getString("name");

                                    int id = tempSensorArray.getJSONObject(k).getInt("id");

                                    SensorBase childSensor = SensorFactory.createSensor("temperature", childName, childSubaddress, id, 0);
                                    if (childSensor != null)
                                        sensor.addChildSensor(childSensor);
                                }
                            }
                        }
                        sensors.add(sensor);
                    }
                }
            }
            /*if (json.has("actuators")) {
                JSONArray jsonArray = json.getJSONArray("actuators");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject j = jsonArray.getJSONObject(i);
                    if (j.has("type")) {
                        String type = j.getString("type");
                        Actuator actuator;
                        if (type.equals("heater")) {
                            actuator = (Actuator) new HeaterActuator();
                        } else if (type.equals("current")) {
                            actuator = (Actuator) new ReleActuator();
                        } else {
                            continue;
                        }
                        if (j.has("name"))
                            actuator.name = j.getString("name");
                        if (j.has("addr"))
                            actuator.subaddress = j.getString("addr");
                        if (j.has("type"))
                            actuator.type = j.getString("type");
                        actuators.add(actuator);
                    }
                }
            }*/

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            return false;
        }
        return true;
    }


    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            if (boardName != null)
                json.put("boardname", boardName);
            if (MACAddress != null)
                json.put("macaddres", MACAddress);
            if (url != null)
                json.put("url", url);
            json.put("port", port);
            JSONArray jarray = new JSONArray();
            for (SensorBase sensor : sensors) {
                //SensorBase sensors = Shields.getSensorFromId(id);
                if (sensor != null)
                    jarray.put(sensor.getJson());
            }
            json.put("sensorIds", jarray);

            /*jarray = new JSONArray();
            for (SensorBase actuator : actuators) {
                //SensorBase actuator = Shields.getActuatorFromId(id);
                if (actuator != null)
                    jarray.put(actuator.getJson());
            }
            json.put("actuatorIds", jarray);*/

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
