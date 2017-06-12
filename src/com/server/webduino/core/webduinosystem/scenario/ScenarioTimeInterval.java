package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.webduinosystem.programinstructions.ProgramInstructions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
//import java.Time;
/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class ScenarioTimeInterval {

    public int id;
    public int scenarioId = 0;
    boolean active;
    public String name;
    public Date startTime;
    public Date endTime;

    public boolean sunday;
    public boolean monday;
    public boolean tuesday;
    public boolean wednesday;
    public boolean thursday;
    public boolean friday;
    public boolean saturday;

    //public List<ProgramInstructions> programInstructionsList = new ArrayList<>();

    //public int priority;

    public ScenarioTimeInterval() {
    }

    public ScenarioTimeInterval(JSONObject json) {
        fromJson(json);
    }

    public boolean isActive() {
        return active;
    }

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

    /*public void setPriority(int priority) {
        this.priority = priority;
    }*/

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
            //json.put("priority", priority);

            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean fromJson(JSONObject json) {

        try {
            if (json.has("id")) {
                id = json.getInt("id");
            }
            if (json.has("name")) {
                name = json.getString("name");
            }
            if (json.has("starttime")) {
                String time = json.getString("starttime");
                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                try {
                    startTime = df.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (json.has("endtime")) {
                String time = json.getString("endtime");
                SimpleDateFormat df = new SimpleDateFormat("HH:mm");
                try {
                    endTime = df.parse(time);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

}
