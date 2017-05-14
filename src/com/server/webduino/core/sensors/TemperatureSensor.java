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

    public boolean sendEvent(String eventtype) {
        if (super.sendEvent(eventtype) || eventtype == TemperatureEvents)
            return true;
        return false;
    }

    public TemperatureSensor(int id, String name, String subaddress, int shieldid, String pin, boolean enabled) {

        super(id, name, subaddress, shieldid, pin, enabled);
        type = "temperaturesensor";
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

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {
            if (json.has("avtemperature"))
                setAvTemperature(json.getDouble("avtemperature"));
            if (json.has("temperature"))
                setTemperature(json.getDouble("temperature"));

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }
    }

    @Override
    public void getJSONField() {
        try {
            json.put("temperature", getTemperature());
            json.put("avtemperature", getAvTemperature());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
