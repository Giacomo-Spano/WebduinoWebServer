package com.server.webduino.core.webduinosystem.scenario;

import com.quartz.NextScenarioTimeIntervalQuartzJob;
import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by giaco on 18/05/2017.
 */
public class Scenario extends DBObject implements ScenarioTimeInterval.ScenarioTimeIntervalListener {

    private static final Logger LOGGER = Logger.getLogger(NextScenarioTimeIntervalQuartzJob.class.getName());

    public int id;
    public boolean active = false;
    public String name = "";
    public String description = "";
    public ScenarioCalendar calendar = new ScenarioCalendar();
    public List<ScenarioTrigger> triggers = new ArrayList<>();
    public List<ScenarioProgram> programs = new ArrayList<>();
    public int priority = 0;
    public boolean dateEnabled = true;
    public boolean enabled = false;

    private JobKey jobKey;
    private JobDetail nextTimeIntervalJob = null;
    private Scheduler scheduler = null;
    private Date nextJobDate = null;
    private Date startDate = null;
    private Date endDate = null;

    public Scenario() {
    }

    public Scenario(JSONObject json) throws Exception {
        fromJson(json);
    }

    public void start() {

        if (!enabled) {
            active = false;
            return;
        }

        for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
            timeInterval.addListener(this);
        }

        checkStatus();

        jobKey = JobKey.jobKey("ScenarioJob"+id, "my-jobs"+id);

        Date currentDate = Core.getDate();
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        cal.add(Calendar.SECOND, 5);
        Date date = cal.getTime();
        scheduleNextTimeIntervalJob(date);
    }

    public void stop() {
        removeListeners();

        deleteNextTimeRangeJob();

        for (ScenarioProgram program:programs) {
            program.stopProgram();
        }

    }

    private void removeListeners() {
        for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
            timeInterval.deleteListener(this);
        }
    }

    public void triggerNextTimeInterval(Date currentDate) {
        ScenarioTimeInterval activeTimeInterval = null;
        Date nextTriggerDate = null;
        activeTimeInterval = calendar.getActiveTimeIntervalFromDateTime(currentDate);
        if (activeTimeInterval != null) {
            nextTriggerDate = activeTimeInterval.nextEndDate(currentDate);
            for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
                Date date = timeInterval.nextStartDate(currentDate);
                if (nextTriggerDate == null)
                    nextTriggerDate = date;
                else if (date != null && date.before(nextTriggerDate))
                    nextTriggerDate = date;
            }
        } else {
            for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
                Date date = timeInterval.nextStartDate(currentDate);
                if (nextTriggerDate == null)
                    nextTriggerDate = date;
                else if (date != null && date.before(nextTriggerDate))
                    nextTriggerDate = date;
            }
        }
        if (nextTriggerDate != null) {
            LOGGER.info("triggerNextTimeInterval id=" + id + "nextActivationDate=" + nextTriggerDate.toString());
            scheduleNextTimeIntervalJob(activeTimeInterval.nextEndDate(nextTriggerDate));
        } else {
            LOGGER.info("no triggerNextTimeInterval id=" + id);
        }
    }

    // schedula la prossima chiamata al joj NextScenarioTimeIntervalQuartzJob e
    // memorizza la data in nextJobDate
    // Se esiste già lo cancella e lo sostituisce
    private void scheduleNextTimeIntervalJob(Date date) {

        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            deleteNextTimeRangeJob();
            /*if (nextTimeIntervalJob != null)
                scheduler.deleteJob(nextTimeIntervalJob.getKey());*/
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        //pass the servlet context to the job
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("scenario", this);
        // define the job and tie it to our job's class
        nextTimeIntervalJob = newJob(NextScenarioTimeIntervalQuartzJob.class)
                //.withIdentity("CronNextTimeIntervalQuartzJob" + this.id, "Group")
                .withIdentity(jobKey)
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = newTrigger()
                .withIdentity("NextTimeIntervalTriggerName" + this.id, "Group")
                .startAt(date)
                .build();

        try {
            nextJobDate = scheduler.scheduleJob(nextTimeIntervalJob, trigger);
        } catch (SchedulerException e) {
            nextJobDate = null;
            e.printStackTrace();
        }
    }

    private void deleteNextTimeRangeJob() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            //printJobsAndTriggers(scheduler);
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM scenarios WHERE id=" + id;
        stmt.executeUpdate(sql);
    }

    @Override
    public void write(Connection conn) throws SQLException {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "INSERT INTO scenarios (id, name, description, dateenabled, enabled, priority)" +
                " VALUES ("
                + id + ","
                + "\"" + name + "\","
                + "\"" + description + "\","
                + Core.boolToString(dateEnabled) + ","
                + Core.boolToString(enabled) + ","
                + priority + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "name=\"" + name + "\","
                + "name=\"" + description + "\","
                + "dateenabled=" + Core.boolToString(dateEnabled) + ","
                + "enabled=" + Core.boolToString(enabled) + ","
                + "priority=" + priority + ";";
        Statement stmt = conn.createStatement();
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }
        for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
            timeInterval.write(conn);
        }
        for (ScenarioTrigger trigger : triggers) {
            trigger.write(conn);
        }
        for (ScenarioProgram program : programs) {
            program.write(conn);
        }
    }

    public void fromResulSet(Connection conn, ResultSet scenariosResultSet) throws Exception {

        //Scenario scenario = new Scenario();
        this.id = scenariosResultSet.getInt("id");
        this.name = scenariosResultSet.getString("name");
        this.description = scenariosResultSet.getString("description");
        this.dateEnabled = scenariosResultSet.getBoolean("dateenabled");
        this.enabled = scenariosResultSet.getBoolean("enabled");
        this.priority = scenariosResultSet.getInt("priority");

        String sql = "SELECT * FROM scenarios_timeintervals WHERE scenarioid=" + this.id;

        Statement stmt = conn.createStatement();
        ResultSet triggersResultSet = stmt.executeQuery(sql);
        while (triggersResultSet.next()) {
            ScenarioTimeInterval timeInterval = new ScenarioTimeInterval();
            timeInterval.id = triggersResultSet.getInt("id");
            timeInterval.scenarioid = triggersResultSet.getInt("scenarioid");
            timeInterval.name = triggersResultSet.getString("name");
            timeInterval.description = triggersResultSet.getString("description");
            timeInterval.priority = triggersResultSet.getInt("priority");
            timeInterval.enabled = triggersResultSet.getBoolean("enabled");
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.");
            timeInterval.startDateTime = df.parse(String.valueOf(triggersResultSet.getTimestamp("startdatetime")));
            timeInterval.endDateTime = df.parse(String.valueOf(triggersResultSet.getTimestamp("enddatetime")));
            timeInterval.setSunday(triggersResultSet.getBoolean("sunday"));
            timeInterval.setMonday(triggersResultSet.getBoolean("monday"));
            timeInterval.setTuesday(triggersResultSet.getBoolean("tuesday"));
            timeInterval.setWednesday(triggersResultSet.getBoolean("wednesday"));
            timeInterval.setThursday(triggersResultSet.getBoolean("thursday"));
            timeInterval.setFriday(triggersResultSet.getBoolean("friday"));
            timeInterval.setSaturday(triggersResultSet.getBoolean("saturday"));
            this.calendar.addTimeIntervals(timeInterval);
        }
        triggersResultSet.close();

        sql = "SELECT * FROM scenarios_triggers WHERE scenarioid=" + this.id;
        ResultSet timeintervalsResultSet = stmt.executeQuery(sql);
        while (timeintervalsResultSet.next()) {
            ScenarioTrigger trigger = new ScenarioTrigger(timeintervalsResultSet);
            this.triggers.add(trigger);
        }
        timeintervalsResultSet.close();

        sql = "SELECT * FROM scenarios_programs WHERE scenarioid=" + this.id + " ;";
        ResultSet programsResultset = stmt.executeQuery(sql);
        while (programsResultset.next()) {
            ScenarioProgram program = new ScenarioProgram(conn, programsResultset);
            this.programs.add(program);
        }
        programsResultset.close();
        stmt.close();
        //return this;
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

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("name", name);
            json.put("description", description);
            json.put("dateenabled", dateEnabled);
            json.put("enabled", enabled);
            json.put("calendar", calendar.toJson());
            json.put("priority", priority);
            JSONArray jarray = new JSONArray();
            if (triggers != null) {
                for (ScenarioTrigger trigger : triggers) {
                    jarray.put(trigger.toJson());
                }
                json.put("triggers", jarray);
            }
            jarray = new JSONArray();
            if (programs != null) {
                for (ScenarioProgram program : programs) {
                    jarray.put(program.toJson());
                }
                json.put("programs", jarray);
            }
            json.put("priority", priority);
            // dynamic values
            if (active)
                json.put("status", "Attivo");
            else
                json.put("status", "Non attivo");
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            if (nextJobDate != null)
                json.put("nextjobdate", df.format(nextJobDate));
            if (startDate != null)
                json.put("startdate", df.format(startDate));
            if (endDate != null)
                json.put("enddate", df.format(endDate));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    public void fromJson(JSONObject json) throws Exception {

        if (json.has("id"))
            id = json.getInt("id");
        if (json.has("name"))
            name = json.getString("name");
        if (json.has("description"))
            description = json.getString("description");
        if (json.has("enabled"))
            enabled = json.getBoolean("enabled");
        if (json.has("dateenabled"))
            dateEnabled = json.getBoolean("dateenabled");
        if (json.has("calendar"))
            calendar = new ScenarioCalendar(json.getJSONObject("calendar"));
        if (json.has("priority"))
            priority = json.getInt("priority");
        if (json.has("programs")) {
            JSONArray jArray = json.getJSONArray("programs");
            for (int k = 0; k < jArray.length(); k++) {
                ScenarioProgram program = new ScenarioProgram(jArray.getJSONObject(k));
                if (jArray.getJSONObject(k).has("timeranges")) {
                    JSONArray jsonArray = jArray.getJSONObject(k).getJSONArray("timeranges");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject j = jsonArray.getJSONObject(i);
                        ScenarioProgramTimeRange timeRange = new ScenarioProgramTimeRange(j);
                        if (timeRange != null)
                            program.timeRanges.add(timeRange);
                    }
                }
                programs.add(program);
            }
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

    @Override
    public void onChangeStatus(boolean active) {
        checkStatus();
    }

    public void checkStatus() {
        boolean oldActiveStatus = active;
        active = false;

        for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
            if (timeInterval.isActive(Core.getDate())) {
                active = true;
            }
        }

        for (ScenarioProgram program : programs) {
            if (active) {
                program.startProgram();
            } else {
                program.stopProgram();
            }
        }
        if (oldActiveStatus != active) {
            if (active) {
                startDate = Core.getDate();
            } else {
                endDate = Core.getDate();
            }
        }
    }

    public class ScenarioCalendar {
        //public boolean dateEnabled;
        public List<ScenarioTimeInterval> timeIntervals = new ArrayList<>();
        private int priority;

        public ScenarioCalendar(JSONObject jsonObject) {
            fromJson(jsonObject);
        }

        public ScenarioCalendar() {
        }

        public boolean fromJson(JSONObject json) {

            try {
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

            if (!dateEnabled) {
                return null;
            }

            ScenarioTimeInterval activeTimeInterval = null;
            for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
                if (timeInterval.isActive(currentDate)) {
                    if (activeTimeInterval == null) {
                        activeTimeInterval = timeInterval;
                        continue;
                    } else if (activeTimeInterval.priority > timeInterval.priority) {
                        activeTimeInterval = timeInterval;
                    }
                }
            }
            return activeTimeInterval;
        }

        public void addTimeIntervals(ScenarioTimeInterval timeInterval) {
            timeIntervals.add(timeInterval);
        }

        private JSONObject toJson() {

            JSONObject json = new JSONObject();
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            try {
                //json.put("dateenabled", dateEnabled);
                json.put("priority", priority);
                JSONArray timeIntervalsJArray = new JSONArray();
                for (ScenarioTimeInterval timeInterval : timeIntervals) {
                    JSONObject JSONInterval = timeInterval.toJson();
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
