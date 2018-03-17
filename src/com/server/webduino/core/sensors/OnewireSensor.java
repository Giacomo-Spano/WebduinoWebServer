package com.server.webduino.core.sensors;

import com.server.webduino.core.datalog.DataLog;
import com.server.webduino.core.datalog.TemperatureSensorDataLog;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

public class OnewireSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(OnewireSensor.class.getName());

    public interface TemperatureSensorListener {
        void changeTemperature(int sensorId, double temperature);
        void changeAvTemperature(int sensorId, double avTemperature);
    }

    private List<TemperatureSensorListener> listeners = new ArrayList<TemperatureSensorListener>();

    public void addListener(TemperatureSensorListener toAdd) {
        listeners.add(toAdd);
    }

    public OnewireSensor(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        //statusUpdatePath = "/temperaturesensorstatus";
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "onewiresensor";
        datalog = new DataLog();
    }

    @Override
    public void writeDataLog(String event) {
        datalog.writelog(event, this);
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
    }

    public void getJSONField() {

    }

}
