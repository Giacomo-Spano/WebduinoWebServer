package com.server.webduino.core.webduinosystem.scenario;

import com.quartz.NextScenarioTimeIntervalQuartzJob;
import com.server.webduino.core.Core;
import com.server.webduino.core.TimeRange;
import com.server.webduino.core.webduinosystem.scenario.programinstructions.ProgramInstructions;
import com.server.webduino.core.webduinosystem.scenario.programtimeranges.ProgramTimeRange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.Date;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by giaco on 18/05/2017.
 */
public class Scenario {
    public int id;
    public boolean active;
    public String name;
    public ScenarioCalendar calendar = new ScenarioCalendar();
    //public List<ProgramInstructions> programInstructionsList = new ArrayList<>();
    public List<ScenarioProgram> programs = new ArrayList<>();
    public int priority;

    private ScenarioTimeInterval activeTimeInterval = null;
    private JobDetail nextTimeIntervalJob = null;
    private Scheduler scheduler = null;

    public Scenario() {
    }

    public Scenario(JSONObject json) {
        fromJson(json);
    }

    public void init() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            //scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        //triggerNextTimeInterval();
        Date currentDate = Core.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.SECOND, 5);
        Date date = cal.getTime();
        scheduleNextTimeIntervalJob(date);
    }

    public void triggerNextTimeInterval() {
        Date currentDate = Core.getDate();
        activeTimeInterval = calendar.getActiveTimeIntervalFromDateTime(currentDate);
        if (activeTimeInterval != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.set(Calendar.HOUR_OF_DAY, activeTimeInterval.endTime.getHours());
            cal.set(Calendar.MINUTE, activeTimeInterval.endTime.getMinutes());
            Date endDate = cal.getTime();
            scheduleNextTimeIntervalJob(endDate);
        }
    }

    public boolean write() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = null;

            stmt = conn.createStatement();
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String sql = "INSERT INTO scenarios (id, name, dateenabled, startdate, enddate, priority)" +
                    " VALUES ("
                    + id + ","
                    + "\"" + name + "\","
                    + Core.boolToString(calendar.dateEnabled) + ","
                    + "'" + df.format(calendar.startDate) + "',"
                    + "'" + df.format(calendar.endDate) + "',"
                    + priority + ") " +
                    "ON DUPLICATE KEY UPDATE "
                    + "name=\"" + name + "\","
                    + "dateenabled=" + Core.boolToString(calendar.dateEnabled) + ","
                    + "startdate='" + df.format(calendar.startDate) + "',"
                    + "enddate='" + df.format(calendar.endDate) + "',"
                    + "priority=" + priority + ";";
            stmt.executeUpdate(sql);
            stmt.close();

            DateFormat tf = new SimpleDateFormat("HH:mm:ss");
            for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
                stmt = conn.createStatement();
                sql = "INSERT INTO scenarios_timeintervals (id, scenarioid, name, starttime, endtime, sunday, monday, tuesday,wednesday, thursday, friday, saturday)" +
                        " VALUES ("
                        + timeInterval.id + ","
                        + id + "," // scenarioid
                        + "\"" + timeInterval.name + "\","
                        + "'" + tf.format(timeInterval.startTime) + "',"
                        + "'" + tf.format(timeInterval.endTime) + "',"
                        + "" + Core.boolToString(timeInterval.sunday) + ","
                        + "" + Core.boolToString(timeInterval.monday) + ","
                        + "" + Core.boolToString(timeInterval.tuesday) + ","
                        + "" + Core.boolToString(timeInterval.wednesday) + ","
                        + "" + Core.boolToString(timeInterval.thursday) + ","
                        + "" + Core.boolToString(timeInterval.friday) + ","
                        + "" + Core.boolToString(timeInterval.saturday) + ") " +
                        "ON DUPLICATE KEY UPDATE "
                        + "scenarioid=" + timeInterval.scenarioId + ","
                        + "name=\"" + timeInterval.name + "\","
                        + "starttime='" + tf.format(timeInterval.startTime) + "',"
                        + "endtime='" + tf.format(timeInterval.endTime) + "',"
                        + "sunday=" + Core.boolToString(timeInterval.sunday) + ","
                        + "monday=" + Core.boolToString(timeInterval.monday) + ","
                        + "tuesday=" + Core.boolToString(timeInterval.tuesday) + ","
                        + "wednesday=" + Core.boolToString(timeInterval.wednesday) + ","
                        + "thursday=" + Core.boolToString(timeInterval.thursday) + ","
                        + "friday=" + Core.boolToString(timeInterval.friday) + ","
                        + "saturday=" + Core.boolToString(timeInterval.saturday) + ";";

                stmt.executeUpdate(sql);
                stmt.close();
            }
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void scheduleNextTimeIntervalJob(Date date) {

        try {
            if (nextTimeIntervalJob != null)
                scheduler.deleteJob(nextTimeIntervalJob.getKey());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        //pass the servlet context to the job
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("scenario", this);
        // define the job and tie it to our job's class
        nextTimeIntervalJob = newJob(NextScenarioTimeIntervalQuartzJob.class).withIdentity(
                "CronNextTimeIntervalQuartzJob" + this.id, "Group")
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = newTrigger()
                .withIdentity("NextTimeIntervalTriggerName" + this.id, "Group")
                .startAt(date)
                .build();

        try {
            Date dd = scheduler.scheduleJob(nextTimeIntervalJob, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public ScenarioTimeInterval getTimeRangeFromId(int id) {

        for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
            if (timeInterval.id == id) {
                return timeInterval;
            }
        }
        return null;
    }

    public static Date removeTime(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    public JSONObject toJSON() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("enabled", active);
            json.put("calendar", calendar.toJson());
            json.put("priority", priority);

            //JSONArray jArray = new JSONArray();
            //for (ScenarioProgram program : programs) {

                JSONArray jarray = new JSONArray();
                if (programs != null) {
                    for (ScenarioProgram program : programs) {
                        jarray.put(program.toJson());
                    }
                    json.put("programs", jarray);
                }

                json.put("priority", priority);


                /*JSONObject jo = new JSONObject();
                jo.put("id", program.id);
                jo.put("scenarioid", program.scenarioId);
                jo.put("enabled", program.enabled);
                jo.put("name", program.name);*/

                /*JSONArray ja = new JSONArray();
                if (program.timeRanges != null) {
                    for (ProgramTimeRange timeRange : program.timeRanges) {
                        ja.put(timeRange.toJson());
                    }
                    jo.put("timeranges", ja);
                    jArray.put(jo);
                }*/

                /*JSONArray jaProgramInstructions = new JSONArray();
                if (program.programInstructionsList != null) {
                    for (ProgramInstructions instruction : program.programInstructionsList) {
                        jaProgramInstructions.put(instruction.toJson());
                    }
                    jo.put("programinstructions", jaProgramInstructions);
                    jArray.put(jo);
                }*/
            //}
            //json.put("programs", jArray);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    public boolean fromJson(JSONObject json) {

        try {
            if (json.has("id"))
                id = json.getInt("id");
            if (json.has("name"))
                name = json.getString("name");
            if (json.has("calendar"))
                calendar = new ScenarioCalendar(json.getJSONObject("calendar"));
            if (json.has("priority"))
                priority = json.getInt("priority");
            if (json.has("programs")) {
                JSONArray jArray = json.getJSONArray("programs");
                for (int k = 0; k < jArray.length(); k++) {
                    ScenarioProgram program = new ScenarioProgram(jArray.getJSONObject(k));
                    if (jArray.getJSONObject(k).has("programtimeranges")) {
                        JSONArray jsonArray = jArray.getJSONObject(k).getJSONArray("programtimeranges");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject j = jsonArray.getJSONObject(i);
                            ProgramTimeRange timeRange = new ProgramTimeRange(j);
                            if (timeRange != null)
                                program.timeRanges.add(timeRange);
                        }

                    }
                }
            }
            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public ScenarioProgram getProgramFromId(int programid) {
        for (ScenarioProgram program : programs) {
            if (program.id == programid) {
                return program;
            }
        }
        return null;
    }

    public class ScenarioCalendar {
        public boolean dateEnabled;

        public Date startDate;
        public Date endDate;
        public List<ScenarioTimeInterval> timeIntervals = new ArrayList<>();
        private int priority;

        public ScenarioCalendar(JSONObject jsonObject) {
            fromJson(jsonObject);
        }

        public ScenarioCalendar() {
        }

        public boolean fromJson(JSONObject json) {

            try {
                SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                if (json.has("startdate")) {
                    String date = json.getString("startdate");

                    try {
                        startDate = df.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                if (json.has("enddate")) {
                    String date = json.getString("enddate");
                    try {
                        endDate = df.parse(date);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                if (json.has("timeintervals")) {
                    JSONArray tintervals = json.getJSONArray("timeintervals");
                    for (int i = 0; i < tintervals.length(); i++) {
                        JSONObject timeinterval = tintervals.getJSONObject(i);
                        ScenarioTimeInterval scenarioTimeInterval = new ScenarioTimeInterval(timeinterval);
                        timeIntervals.add(scenarioTimeInterval);
                    }
                }
                return true;

            } catch (JSONException e) {
                e.printStackTrace();
                return false;
            }
        }

        public ScenarioTimeInterval getActiveTimeIntervalFromDateTime(Date currentDate) {

            DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String timeStr = timeFormat.format(currentDate);
            Time currentTime = Time.valueOf(timeStr);
            Date currentDay = removeTime(currentDate);

            if (dateEnabled) {
                if (currentDate.after(endDate))
                    return null;
                if (currentDate.before(startDate))
                    return null;
            }

            for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
                if (currentTime.before(timeInterval.startTime)) {
                    continue;
                }
                if (currentTime.equals(timeInterval.endTime) || currentTime.after(timeInterval.endTime)) {
                    continue;
                }
                Calendar c = Calendar.getInstance();
                c.setTime(currentDate);
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                switch (dayOfWeek) {
                    case Calendar.SUNDAY:
                        if (!timeInterval.sunday) continue;
                        break;
                    case Calendar.MONDAY:
                        if (!timeInterval.monday) continue;
                        break;
                    case Calendar.TUESDAY:
                        if (!timeInterval.tuesday) continue;
                        break;
                    case Calendar.WEDNESDAY:
                        if (!timeInterval.wednesday) continue;
                        break;
                    case Calendar.THURSDAY:
                        if (!timeInterval.thursday) continue;
                        break;
                    case Calendar.FRIDAY:
                        if (!timeInterval.friday) continue;
                        break;
                    case Calendar.SATURDAY:
                        if (!timeInterval.saturday) continue;
                        break;
                    default:
                        break;
                }
                return timeInterval;
            }
            return null;
        }

        public void setDateEnabled(boolean dateEnabled) {
            this.dateEnabled = dateEnabled;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public void setPriorityriority(int tpriority) {
            this.priority = tpriority;
        }

        public void addTimeIntervals(ScenarioTimeInterval timeInterval) {
            timeIntervals.add(timeInterval);
        }

        private JSONObject toJson() {

            JSONObject json = new JSONObject();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            try {
                json.put("dateenabled", dateEnabled);
                json.put("startdate", startDate);
                json.put("enddate", endDate);
                json.put("priority", priority);

                JSONArray timeIntervalsJArray = new JSONArray();
                for (ScenarioTimeInterval timeInterval : timeIntervals) {

                    JSONObject JSONInterval = new JSONObject();
                    JSONInterval.put("id", timeInterval.id);
                    JSONInterval.put("name", timeInterval.name);
                    if (timeInterval.startTime != null)
                        JSONInterval.put("starttime", df.format(timeInterval.startTime));
                    if (timeInterval.endTime != null)
                        JSONInterval.put("endtime", df.format(timeInterval.endTime));

                    JSONInterval.put("sunday", timeInterval.sunday);
                    JSONInterval.put("monday", timeInterval.monday);
                    JSONInterval.put("tuesday", timeInterval.tuesday);
                    JSONInterval.put("wednesday", timeInterval.wednesday);
                    JSONInterval.put("thursday", timeInterval.thursday);
                    JSONInterval.put("friday", timeInterval.friday);
                    JSONInterval.put("saturday", timeInterval.saturday);

                    //JSONInterval.put("priority", timeInterval.priority);
                    timeIntervalsJArray.put(JSONInterval);
                }
                json.put("timeintervals", timeIntervalsJArray);

            } catch (JSONException e) {
                e.printStackTrace();
            }
            return json;
        }


    }
}
