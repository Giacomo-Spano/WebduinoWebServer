package com.server.webduino.core;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.management.Sensor;


import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

//import static com.server.webduino.core.SensorBase.Status_Offline;

public class Shield extends httpClient {

    private static Logger LOGGER = Logger.getLogger(Shield.class.getName());

    protected int id;
    protected String MACAddress;
    protected String boardName;
    protected Date lastUpdate;
    List<SensorBase> sensors = new ArrayList<>();
    List<Actuator> actuators = new ArrayList<>();
    public URL url;
    public int port;

    /*public Shield(JSONObject jsonObj) {
        //FromJson(jsonObj);
    }*/

    public Shield() {
    }

    /*public boolean sensorsIsNotUpdated() {

        Date currentDate = Core.getDate();
        boolean res = false;
        for (SensorBase sensor : sensors) {
            //SensorBase s = Shields.getSensorFromId(id);
            if (sensor.lastUpdate == null || (currentDate.getTime() - sensor.lastUpdate.getTime()) > (30*1000) ) {
                sensor.onlinestatus = Status_Offline;
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
            if (json.has("boardname"))
                boardName = json.getString("boardname");
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
                        SensorBase sensor;
                        if (type.equals("temperature")) {
                            sensor = (SensorBase) new TemperatureSensor();
                        } else if (type.equals("currentsensor")) {
                            sensor = (SensorBase) new CurrentSensor();
                        } else if (type.equals("humiditysensor")) {
                            sensor = (SensorBase) new HumiditySensor();
                        } else if (type.equals("pressuresensor")) {
                            sensor = (SensorBase) new PressureSensor();
                        } else if (type.equals("pirsensor")) {
                            sensor = (SensorBase) new PIRSensor();
                        } else if (type.equals("doorsensor")) {
                            sensor = (SensorBase) new DoorSensor();
                        } else {
                            continue;
                        }
                        if (j.has("name"))
                            sensor.name = j.getString("name");
                        if (j.has("addr"))
                            sensor.subaddress = j.getString("addr");
                        if (j.has("type"))
                            sensor.type = j.getString("type");
                        sensors.add(sensor);
                    }
                }
            }
            if (json.has("actuators")) {
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
            }

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
                //SensorBase sensor = Shields.getSensorFromId(id);
                if (sensor != null)
                    jarray.put(sensor.getJson());
            }
            json.put("sensorIds", jarray);

            jarray = new JSONArray();
            for (SensorBase actuator : actuators) {
                //SensorBase actuator = Shields.getActuatorFromId(id);
                if (actuator != null)
                    jarray.put(actuator.getJson());
            }
            json.put("actuatorIds", jarray);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
