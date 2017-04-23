package com.server.webduino.core.sensors;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.logging.Logger;

import static com.server.webduino.core.sensors.SensorBase.SensorListener.SensorEvents;
import static com.server.webduino.core.sensors.TemperatureSensor.TemperatureSensorListener.TemperatureEvents;

public class TemperatureSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(TemperatureSensor.class.getName());

    private double temperature;
    private double avTemperature;


    public interface TemperatureSensorListener extends SensorBase.SensorListener {
        public String TemperatureEvents = "temperature event";
        void changeTemperature(int sensorId, double temperature);
        void changeAvTemperature(int sensorId, double avTemperature);
    }

    //private List<TemperatureSensorListener> listeners = new ArrayList<TemperatureSensorListener>();

    /*public void addListener(TemperatureSensorListener toAdd) {
        listeners.add(toAdd);
    }*/

    public boolean sendEvent(String eventtype) {
        if (super.sendEvent(eventtype) || eventtype == TemperatureEvents)
            return true;
        return false;
    }



    public TemperatureSensor(int id, String name, String subaddress, int shieldid) {

        super(id, name, subaddress, shieldid);
        type = "temperature";
    }

    public void setTemperature(double temperature) {

        LOGGER.info("setTemperature");

        double oldtemperature = this.temperature;
        this.temperature = temperature;

        if (temperature != oldtemperature) {
            TemperatureSensorDataLog dl = new TemperatureSensorDataLog();
            dl.writelog("updateFromJson",this);
            // Notify everybody that may be interested.
            for (SensorListener listener : listeners) {
                if (listener instanceof TemperatureSensorListener) {
                    TemperatureSensorListener l = (TemperatureSensorListener) listener;
                    l.changeTemperature(id, temperature);
                }
            }
        }
    }

    public void setAvTemperature(double avTemperature) {

        LOGGER.info("setAvTemperature: " + avTemperature);
        LOGGER.info("oldAvTemperature= " + avTemperature);

        double oldAvtemperature = this.avTemperature;
        this.avTemperature = avTemperature;
        // Notify everybody that may be interested.
        for (SensorListener listener : listeners) {
            if (listener instanceof TemperatureSensorListener) {
                TemperatureSensorListener l = (TemperatureSensorListener) listener;
                l.changeAvTemperature(id, avTemperature);
            }
        }
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
    public void updateFromJson(Date date, JSONObject json) {

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
