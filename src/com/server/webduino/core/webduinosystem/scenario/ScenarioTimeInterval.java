package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.webduinosystem.programinstructions.ProgramInstructions;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class ScenarioTimeInterval {

    public int id;
    public int scenarioId = 0;
    boolean active;
    public String name;
    public Time startTime;
    public Time endTime;

    public boolean sunday;
    public boolean monday;
    public boolean tuesday;
    public boolean wednesday;
    public boolean thursday;
    public boolean friday;
    public boolean saturday;

    public List<ProgramInstructions> programInstructionsList = new ArrayList<>();

    public int priority;

    public ScenarioTimeInterval() {
    }

    public boolean isActive() {
        return active;
    }

    /*public ScenarioTimeInterval(int id, String name, Time endTime, int programInstructions, boolean active) {
        this.id = id;
        this.name = name;
        //this.startTime = startTime;
        this.endTime = endTime;
        this.programInstructions = programInstructions;
        this.active = active;
    }

    public ScenarioTimeInterval(ScenarioTimeInterval tr) {
        //this.ID = tr.ID;
        this.name = tr.name;
        this.endTime = tr.endTime;
        this.programInstructions = tr.programInstructions;
        //this. = tr.sensorId;
        this.active = active;
    }*/
    public void setSunday(boolean sunday) {
        this.sunday = sunday;
    }

    public void setMonday(boolean monday) {
        this.monday = monday;
    }

    public void setTuesday(boolean tuesday) {
        this.tuesday = tuesday;
    }

    public void setWednesday(boolean wednesday) {
        this.wednesday = wednesday;
    }

    public void setThursday(boolean thursday) {
        this.thursday = thursday;
    }

    public void setFriday(boolean friday) {
        this.friday = friday;
    }

    public void setSaturday(boolean saturday) {
        this.saturday = saturday;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");

        try {
            json.put("id", id);
            json.put("scenarioid", scenarioId);
            json.put("name", name);
            if (startTime != null)
                json.put("starttime", df.format(endTime));
            if (endTime != null)
                json.put("endtime", df.format(endTime));
            json.put("sunday", sunday);
            json.put("monday", monday);
            json.put("tuesday", tuesday);
            json.put("wednesday", wednesday);
            json.put("thursday", thursday);
            json.put("friday", friday);
            json.put("saturday", saturday);
            json.put("priority", priority);

            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
