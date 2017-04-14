package com.server.webduino.core;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class OnewireSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(OnewireSensor.class.getName());

    private double temperature;
    private double avTemperature;


    public interface TemperatureSensorListener {
        void changeTemperature(int sensorId, double temperature);
        void changeAvTemperature(int sensorId, double avTemperature);
    }

    public class TempSensor {
        String name;
        String id;
    }

    public List<TempSensor> temperatureSensors = new ArrayList<TempSensor>();

    public void addTemperatureSensor(String id, String name) {
        TempSensor tempSensor = new TempSensor();
        tempSensor.name = name;
        tempSensor.id = id;
        temperatureSensors.add(tempSensor);
    }

    private List<TemperatureSensorListener> listeners = new ArrayList<TemperatureSensorListener>();

    public void addListener(TemperatureSensorListener toAdd) {
        listeners.add(toAdd);
    }

    public OnewireSensor() {
        //type = "temperature";
        statusUpdatePath = "/temperaturesensorstatus";
    }

    public void setTemperature(double temperature) {

        LOGGER.info("setTemperature");

        double oldtemperature = this.temperature;
        this.temperature = temperature;

        if (temperature != oldtemperature) {
            TemperatureSensorDataLog dl = new TemperatureSensorDataLog();
            dl.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (TemperatureSensorListener hl : listeners)
                hl.changeTemperature(id, temperature);
        }
    }

    public void setAvTemperature(double avTemperature) {

        LOGGER.info("setAvTemperature: " + avTemperature);
        LOGGER.info("oldAvTemperature= " + avTemperature);

        double oldAvtemperature = this.avTemperature;
        this.avTemperature = avTemperature;
        // Notify everybody that may be interested.
        for (TemperatureSensorListener hl : listeners)
            hl.changeAvTemperature(id, avTemperature);
    }

    @Override
    public void writeDataLog(String event) {
        TemperatureSensorDataLog dl = new TemperatureSensorDataLog();
        dl.writelog(event, this);
    }

    public double getAvTemperature() {
        return avTemperature;
    }



    public double getTemperature() {
        return temperature;
    }

    @Override
    void updateFromJson(Date date, JSONObject json) {

        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            lastUpdate = date;
            online = true;
            if (json.has("avtemperature"))
                setAvTemperature(json.getDouble("avtemperature"));
            if (json.has("temperature"))
                setTemperature(json.getDouble("temperature"));

            if (json.has("name"))
                name = json.getString("name");
            //setData(shieldid, subaddress, name, date, temperature, avTemperature);
            // ma questo a cosa serve??? Aggiorna solo la data e il nome, non gli altri campi.
            // Forse per la gestione dell'offline??
            super.setData(shieldid, subaddress, name, date);

            //TemperatureSensorDataLog dl = new TemperatureSensorDataLog();
            //dl.writelog("updateFromJson",this);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }
    }

    @Override
    public JSONObject getJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("id", getId());
            json.put("shieldid", shieldid);
            json.put("online", online);
            json.put("subaddress", subaddress);
            json.put("temperature", getTemperature());
            json.put("avtemperature", getAvTemperature());
            json.put("name", getName());
            json.put("lastupdate", getStrLastUpdate());
            json.put("type", type);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
