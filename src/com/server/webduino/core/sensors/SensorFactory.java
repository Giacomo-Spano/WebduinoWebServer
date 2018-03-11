package com.server.webduino.core.sensors;

import com.server.webduino.core.sensors.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by giaco on 21/04/2017.
 */
public class SensorFactory {

    static public SensorBase createSensor(String type, String name, String description, String subaddress, int id, int shieldid, String pin, boolean enabled) {

        SensorBase sensor;
        if (type.equals("temperaturesensor")) {
            sensor = (SensorBase) new TemperatureSensor(id,name,description,subaddress,shieldid,pin,enabled);
        } else if (type.equals("onewiresensor")) {
            sensor = (SensorBase) new OnewireSensor(id,name,description,subaddress,shieldid,pin,enabled);
        } else if (type.equals("currentsensor")) {
            sensor = (SensorBase) new CurrentSensor(id,name,description,subaddress,shieldid,pin,enabled);
        } else if (type.equals("doorsensor")) {
            sensor = (SensorBase) new DoorSensor(id,name,description,subaddress,shieldid,pin,enabled);
        } else if (type.equals("heatersensor")) {
            sensor = (SensorBase) new HeaterActuator(id,name,description,subaddress,shieldid,pin,enabled);
            //sensor.startPrograms();
        } else if (type.equals("humiditysensor")) {
            sensor = (SensorBase) new HumiditySensor(id,name,description,subaddress,shieldid,pin,enabled);
        } else if (type.equals("pirsensor")) {
            sensor = (SensorBase) new PIRSensor(id,name,description,subaddress,shieldid,pin,enabled);
        } else if (type.equals("pressuresensor")) {
            sensor = (SensorBase) new PressureSensor(id,name,description,subaddress,shieldid,pin,enabled);
        } else if (type.equals("relesensor")) {
            sensor = (SensorBase) new ReleActuator(id,name,description,subaddress,shieldid,pin,enabled);
        } else if (type.equals("hornsensor")) {
            sensor = (SensorBase) new HornSensor(id,name,description,subaddress,shieldid,pin,enabled);
        } else {
            return null;
        }
        sensor.init();
        return sensor;
    }

    public SensorBase fromJson(JSONObject json) throws Exception {
        String type = "";
        if (json.has("type"))
            type = json.getString("type");
        else
            throw new JSONException("type key missing");
        int id = 0, shieldid = 0;
        String name = "", description = "", subaddress = "", pin = "";
        boolean enabled = true;

        if (json.has("id")) id = json.getInt("id");
        if (json.has("name")) name = json.getString("name");
        if (json.has("description")) description = json.getString("description");
        if (json.has("subaddress")) subaddress = json.getString("subaddress");
        if (json.has("shieldid")) shieldid = json.getInt("shieldid");
        if (json.has("pin")) pin = json.getString("pin");
        if (json.has("enabled")) enabled = json.getBoolean("enabled");
        SensorBase sensor = null;
        try {
            sensor = createSensor(type,name, description,subaddress,id,shieldid,pin,enabled);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return sensor;
    }

    public static JSONArray getSensorTypesJSONArray() {
        JSONArray jsonArray = new JSONArray();
        JSONObject json;
        try {
            json = new JSONObject();
            json.put("value", "temperaturesensor");
            json.put("description", "Temperaturesensor");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "onewiresensor");
            json.put("description", "Onewiresensor");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "currentsensor");
            json.put("description", "Currentsensor");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "doorsensor");
            json.put("description", "Doorsensor");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "heatersensor");
            json.put("description", "Heatersensor");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "humiditysensor");
            json.put("description", "Humiditysensor");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "pirsensor");
            json.put("description", "Pirsensor");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "pressuresensor");
            json.put("description", "Pressuresensor");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "relesensor");
            json.put("description", "Relesensor");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "hornsensor");
            json.put("description", "Hornsensor");
            jsonArray.put(json);


        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    public static JSONArray getPinJSONArray() {
        JSONArray jsonArray = new JSONArray();
        JSONObject json;
        try {
            json = new JSONObject();
            json.put("value", "D0");
            json.put("description", "D0");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "D1");
            json.put("description", "D1");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "D2");
            json.put("description", "D2");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "D3");
            json.put("description", "D3");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "D4");
            json.put("description", "D4");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "D5");
            json.put("description", "D5");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "D6");
            json.put("description", "D6");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "D7");
            json.put("description", "D7");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "D8");
            json.put("description", "D8");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "D9");
            json.put("description", "D9");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("value", "D10");
            json.put("description", "D10");
            jsonArray.put(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }


}
