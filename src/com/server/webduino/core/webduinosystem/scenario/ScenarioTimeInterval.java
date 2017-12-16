package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONException;
import org.json.JSONObject;


import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
//import java.Time;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class ScenarioTimeInterval extends DBObject {

    public int id;
    public int scenarioid = 0;
    public boolean enabled = false;
    public int priority = 0;
    public String name = "";
    public String description = "";
    public Date startDateTime = new Date();
    public Date endDateTime = new Date();

    public boolean sunday = false;
    public boolean monday = false;
    public boolean tuesday = false;
    public boolean wednesday = false;
    public boolean thursday = false;
    public boolean friday = false;
    public boolean saturday = false;

    public interface ScenarioTimeIntervalListener {
        void onChangeStatus(boolean active);
    }

    protected List<ScenarioTimeIntervalListener> listeners = new ArrayList<ScenarioTimeIntervalListener>();

    public void addListener(ScenarioTimeIntervalListener toAdd) {
        listeners.add(toAdd);
    }

    public void deleteListener(ScenarioTimeIntervalListener toRemove) {
        listeners.remove(toRemove);
    }

    public ScenarioTimeInterval() {
    }

    public ScenarioTimeInterval(JSONObject json) throws JSONException {
        fromJson(json);
    }

    public boolean isActive(Date datetime) {

        if (datetime == null)
            return false;

        if (!enabled) return false;

        if (datetime.before(startDateTime)) return false;

        if (datetime.equals(endDateTime) || datetime.after(endDateTime)) return false;

        Calendar c = Calendar.getInstance();
        c.setTime(datetime);
        if (dayOfWeekActive(c.get(Calendar.DAY_OF_WEEK)))
            return true;
        return false;
    }

    // calcola la prossima data e orà in cui teminerà (se non è attivo torna null)
    public Date nextEndDate(Date datetime) {

        if (datetime == null)
            return null;

        if (!isActive(datetime))
            return null;

        Calendar calendarEndDateTime = Calendar.getInstance();
        calendarEndDateTime.setTime(endDateTime);

        Calendar c = Calendar.getInstance();
        c.setTime(datetime);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);

        if (c.get(Calendar.DAY_OF_WEEK_IN_MONTH) == calendarEndDateTime.get(Calendar.DAY_OF_WEEK_IN_MONTH) &&
                c.get(Calendar.MONTH) == calendarEndDateTime.get(Calendar.MONTH) &&
                c.get(Calendar.YEAR) == calendarEndDateTime.get(Calendar.YEAR))
            return endDateTime;

        int n = 0;
        while (n < 6) {
            c.add(Calendar.DATE, 1); // Adding 1 day
            if (!dayOfWeekActive(c.get(Calendar.DAY_OF_WEEK))) {
                c.add(Calendar.DATE, -1);
                return c.getTime();
            }
            n++;
        }
        return endDateTime;
    }

    // calcola la prossima data in cui sarà attivo
    public Date nextStartDate(Date datetime) {

        if (datetime == null)
            return null;

        if (isActive(datetime))
            return null;

        Calendar calendarStartDateTime = Calendar.getInstance();
        calendarStartDateTime.setTime(endDateTime);

        Calendar c = Calendar.getInstance();
        c.setTime(datetime);

        if (c.get(Calendar.DAY_OF_WEEK_IN_MONTH) == calendarStartDateTime.get(Calendar.DAY_OF_WEEK_IN_MONTH) &&
                c.get(Calendar.MONTH) == calendarStartDateTime.get(Calendar.MONTH) &&
                c.get(Calendar.YEAR) == calendarStartDateTime.get(Calendar.YEAR) &&
                dayOfWeekActive(c.get(Calendar.DAY_OF_WEEK))) {
            // se il giono è lo stesso e il giorno della settimana è attivo
            if (datetime.after(startDateTime))
                return datetime;
            else
                return startDateTime;
        }

        int n = 0;
        while (n < 6) {
            c.add(Calendar.DATE, 1); // Adding 1 day
            if (dayOfWeekActive(c.get(Calendar.DAY_OF_WEEK))) {
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                return c.getTime();
            }
            n++;
        }
        return null;
    }

    boolean dayOfWeekActive(int dayOfWeek) {

        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                if (sunday)
                    return true;
            case Calendar.MONDAY:
                if (monday)
                    return true;
            case Calendar.TUESDAY:
                if (tuesday)
                    return true;
            case Calendar.WEDNESDAY:
                if (wednesday)
                    return true;
            case Calendar.THURSDAY:
                if (thursday)
                    return true;
            case Calendar.FRIDAY:
                if (friday)
                    return true;
            case Calendar.SATURDAY:
                if (saturday)
                    return true;
            default:
                return false;
        }
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

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");

        try {
            json.put("id", id);
            json.put("scenarioid", scenarioid);
            json.put("name", name);
            json.put("description", description);
            json.put("enabled", enabled);
            if (startDateTime != null)
                json.put("startdatetime", df.format(startDateTime));
            if (endDateTime != null)
                json.put("enddatetime", df.format(endDateTime));
            json.put("sunday", sunday);
            json.put("monday", monday);
            json.put("tuesday", tuesday);
            json.put("wednesday", wednesday);
            json.put("thursday", thursday);
            json.put("friday", friday);
            json.put("saturday", saturday);
            json.put("priority", priority);

            if (isActive(Core.getDate()))
                json.put("status", "on");
            else
                json.put("status", "off");

            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void fromJson(JSONObject json) throws JSONException {

        if (json.has("id")) id = json.getInt("id");
        if (json.has("scenarioid")) scenarioid = json.getInt("scenarioid");
        if (json.has("name")) name = json.getString("name");
        if (json.has("description")) description = json.getString("description");
        if (json.has("enabled")) enabled = json.getBoolean("enabled");
        if (json.has("priority")) priority = json.getInt("priority");
        if (json.has("startdatetime")) {
            String time = json.getString("startdatetime");
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            try {
                startDateTime = df.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (json.has("enddatetime")) {
            String time = json.getString("enddatetime");
            SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            try {
                endDateTime = df.parse(time);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (json.has("sunday")) sunday = json.getBoolean("sunday");
        if (json.has("monday")) monday = json.getBoolean("monday");
        if (json.has("tuesday")) tuesday = json.getBoolean("tuesday");
        if (json.has("wednesday")) wednesday = json.getBoolean("wednesday");
        if (json.has("thursday")) thursday = json.getBoolean("thursday");
        if (json.has("friday")) friday = json.getBoolean("friday");
        if (json.has("saturday")) saturday = json.getBoolean("saturday");
    }

    @Override
    protected void write(Connection conn) throws SQLException {

        DateFormat tf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "INSERT INTO scenarios_timeintervals (id, scenarioid, name, description, priority, enabled, startdatetime, enddatetime, sunday, monday, tuesday,wednesday, thursday, friday, saturday)" +
                " VALUES ("
                + id + "," // scenarioid
                + scenarioid + "," // scenarioid
                + "\"" + name + "\","
                + "\"" + description + "\","
                + priority + ","
                + "" + enabled + ","
                + "'" + tf.format(startDateTime) + "',"
                + "'" + tf.format(endDateTime) + "',"
                + "" + Core.boolToString(sunday) + ","
                + "" + Core.boolToString(monday) + ","
                + "" + Core.boolToString(tuesday) + ","
                + "" + Core.boolToString(wednesday) + ","
                + "" + Core.boolToString(thursday) + ","
                + "" + Core.boolToString(friday) + ","
                + "" + Core.boolToString(saturday) + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "scenarioid=" + scenarioid + ","
                + "name=\"" + name + "\","
                + "description=\"" + description + "\","
                + "priority=" + priority + ","
                + "enabled='" + enabled + "',"
                + "startdatetime='" + tf.format(startDateTime) + "',"
                + "enddatetime='" + tf.format(endDateTime) + "',"
                + "sunday=" + Core.boolToString(sunday) + ","
                + "monday=" + Core.boolToString(monday) + ","
                + "tuesday=" + Core.boolToString(tuesday) + ","
                + "wednesday=" + Core.boolToString(wednesday) + ","
                + "thursday=" + Core.boolToString(thursday) + ","
                + "friday=" + Core.boolToString(friday) + ","
                + "saturday=" + Core.boolToString(saturday) + ";";
        Statement stmt = conn.createStatement();
        stmt.executeUpdate(sql);
    }

    @Override
    protected void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM scenarios_timeintervals WHERE id=" + id;
        stmt.executeUpdate(sql);
    }
}
