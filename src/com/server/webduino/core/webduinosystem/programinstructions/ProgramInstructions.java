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
import java.util.Date;

/**
 * Created by giaco on 17/05/2017.
 */
public class ProgramInstructions implements Zone.WebduinoZoneListener {

    private int id;
    private int scenarioid;
    private String type;
    private String name;
    protected int actuatorid;
    private double targetValue;
    private int zoneId;
    private int seconds;
    public boolean schedule;
    public Date startTime;
    public Date endTime;
    public boolean sunday;
    public boolean monday;
    public boolean tuesday;
    public boolean wednesday;
    public boolean thursday;
    public boolean friday;
    public boolean saturday;
    public int priority;

    public ProgramInstructions(int id, int scenarioid, String name, String type, int actuatorid, double targetValue, int zoneId, int seconds, boolean schedule, Date startTime, Date endTime,
                               boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, int priority) {
        this.id = id;
        this.scenarioid = scenarioid;
        this.type = type;
        this.name = name;
        this.actuatorid = actuatorid;
        this.targetValue = targetValue;
        this.zoneId = zoneId;
        this.seconds = seconds;
        this.schedule = schedule;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sunday = sunday;
        this.monday = monday;
        this.tuesday = tuesday;
        this.wednesday = wednesday;
        this.thursday = thursday;
        this.friday = friday;
        this.saturday = saturday;
        this.priority = priority;
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
            json.put("scenarioid", scenarioid);
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
            json.put("schedule", schedule);
            json.put("starttime", startTime);
            json.put("endtime", endTime);
            json.put("sunday", sunday);
            json.put("monday", monday);
            json.put("tuesday", tuesday);
            json.put("wednesday", wednesday);
            json.put("thursday", thursday);
            json.put("friday", friday);
            json.put("saturday", saturday);
            json.put("priority", priority);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }
}
