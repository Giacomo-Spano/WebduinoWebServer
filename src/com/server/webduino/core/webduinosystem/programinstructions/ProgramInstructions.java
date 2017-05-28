package com.server.webduino.core.webduinosystem.programinstructions;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.WebduinoTrigger;
import com.server.webduino.core.webduinosystem.scenario.ScenarioTimeInterval;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created by giaco on 17/05/2017.
 */
public class ProgramInstructions implements Zone.WebduinoZoneListener {

    private int id;
    private String type;
    private String name;
    protected int actuatorid;
    private double targetValue;
    private int zoneId;
    private int seconds;

    public ProgramInstructions(int id, String name, String type, int actuatorid, double targetValue, int zoneId, int seconds) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.actuatorid = actuatorid;
        this.targetValue = targetValue;
        this.zoneId = zoneId;
        this.seconds = seconds;
    }

    @Override
    public void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature) {

    }

    @Override
    public void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus) {

    }

    public void init() {

    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("type", type);
            json.put("name", name);
            json.put("actuatorid", actuatorid);
            SensorBase actuator = Core.getSensorFromId(actuatorid);
            if (actuator != null)
                json.put("actuatorname", actuator.getName());
            json.put("targetvalue", targetValue);
            json.put("zoneid", zoneId);
            Zone zone = Core.getZoneFromId(zoneId);
            if (zone != null)
                json.put("zonename", zone.getName());
            json.put("seconds", seconds);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
