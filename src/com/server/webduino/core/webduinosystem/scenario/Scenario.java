package com.server.webduino.core.webduinosystem.scenario;

import com.quartz.NextScenarioTimeIntervalQuartzJob;
import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramAction;
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
import java.util.logging.Logger;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

/**
 * Created by giaco on 18/05/2017.
 */
public class Scenario extends DBObject implements ScenarioTimeInterval.ScenarioTimeIntervalListener/*, ScenarioProgramTimeRange.ActionListener*/ {

    private static final Logger LOGGER = Logger.getLogger(NextScenarioTimeIntervalQuartzJob.class.getName());

    public int id;
    public int webduinosystemid;
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
    //private Date startDate = null;
    //private Date endDate = null;

    private ScenarioTimeInterval.ScenarioTimeIntervalListener timeIntervalListener = new ScenarioTimeInterval.ScenarioTimeIntervalListener() {
        @Override
        public void onChangeStatus(boolean active) {
            updateStatus();
        }
    };

    private ScenarioTrigger.ScenarioTriggerListener triggerListener = new ScenarioTrigger.ScenarioTriggerListener() {
        @Override
        public void onChangeStatus(boolean active) {
            updateStatus();
        }
    };

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

        for (ScenarioTrigger trigger : triggers) {
            trigger.addListener(triggerListener);
        }

        updateStatus();

        jobKey = JobKey.jobKey("ScenarioJob" + id, "my-jobs" + id);

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

        for (ScenarioProgram program : programs) {
            program.stopProgram();
        }

    }

    private void removeListeners() {
        for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
            timeInterval.deleteListener(this);
        }
    }

    public void triggerNextTimeInterval(Date currentDate) {

        // questa funzione non la capisco. Dovrebbe mettere active a true o false quando viene chiamata ????
        // sembra che non faccia nulla a parte impostare la prossima chiamata al termine dell'activetimeintervalfrromdate
        // oppure alla nextstartdate.
        // Secondo me non funziona. Funziona all'inizio ma se si pèasssa da un calendar ad un altro non funziona. Dovrebbe
        // chiamare la checkStatus()
        //
        // forse ho capito. Non serve a nulla questa chiamata, si potrebbe eliminare. La checkStatus è chiamata dagli eventi dei timeinterval
        // onChangeStatus

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
            Date nextEndDate = activeTimeInterval.nextEndDate(nextTriggerDate);
            if (nextEndDate != null) {
                scheduleNextTimeIntervalJob(nextEndDate);
            } else {
                LOGGER.info("nextTime interval is null");
            }
        } else {
            LOGGER.info("no triggerNextTimeInterval id=" + id);
        }
    }

    // schedula la prossima chiamata al job NextScenarioTimeIntervalQuartzJob e
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
        String sql = "INSERT INTO scenarios (id, webduinosystemid, name, description, dateenabled, enabled, priority)" +
                " VALUES ("
                + id + ","
                + webduinosystemid + ","
                + "\"" + name + "\","
                + "\"" + description + "\","
                + Core.boolToString(dateEnabled) + ","
                + Core.boolToString(enabled) + ","
                + priority + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "webduinosystemid=" + webduinosystemid + ","
                + "name=\"" + name + "\","
                + "description=\"" + description + "\","
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
            if (timeInterval.scenarioid == 0) timeInterval.scenarioid = id;
            timeInterval.write(conn);
        }
        for (ScenarioTrigger trigger : triggers) {
            if (trigger.scenarioid == 0) trigger.scenarioid = id;
            trigger.write(conn);
        }
        for (ScenarioProgram program : programs) {
            if (program.scenarioId == 0) program.scenarioId = id;
            program.write(conn);
        }
    }

    public void setActionListener(ProgramAction.ActionListener listener) {
        if (programs != null)
            for (ScenarioProgram program : programs) {
                program.setActionListener(listener);
            }
    }

    public void fromResulSet(Connection conn, ResultSet scenariosResultSet) throws Exception {

        //Scenario scenario = new Scenario();
        this.id = scenariosResultSet.getInt("id");
        this.webduinosystemid = scenariosResultSet.getInt("webduinosystemid");
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
            json.put("webduinosystemid", webduinosystemid);
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
            /*if (startDate != null)
                json.put("startdate", df.format(startDate));
            if (endDate != null)
                json.put("enddate", df.format(endDate));*/
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    public void fromJson(JSONObject json) throws Exception {

        if (json.has("id"))
            id = json.getInt("id");
        if (json.has("webduinosystemid"))
            webduinosystemid = json.getInt("webduinosystemid");
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
        if (json.has("triggers")) {
            JSONArray jArray = json.getJSONArray("triggers");
            for (int k = 0; k < jArray.length(); k++) {
                ScenarioTrigger trigger = new ScenarioTrigger(jArray.getJSONObject(k));
                triggers.add(trigger);
            }
        }
        if (json.has("priority"))
            priority = json.getInt("priority");
        if (json.has("programs")) {
            JSONArray jArray = json.getJSONArray("programs");
            for (int k = 0; k < jArray.length(); k++) {
                ScenarioProgram program = new ScenarioProgram(jArray.getJSONObject(k));
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
        updateStatus();
    }

    // ritorna lo stato dello scenario ad una certa data e ora
    public boolean statusActiveAtDate(Date date) {
        //boolean oldActiveStatus = active;
        //active = false;

        // controlla se c'è almeno un timeinterval attivo
        for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {
            if (timeInterval.isActive(date)) {
                //active = true;
                return true;
            }
        }
        return false;
    }

    // controlla se lo stato è attivo e avvia o ferma i programmi
    // è chiamata all'inizio e poi dall'evento onChangeStatus dei timeInterval del calendario dello scenario
    public void updateStatus() {

        boolean oldActiveStatus = active;

        if (calendar.timeIntervals.size() == 0) {
            active = true;
        } else {
            // controlla se c'è almeno un timeinterval attivo
            active = statusActiveAtDate(Core.getDate());
        }

        if (active && triggers.size() > 0) {
            active = false;
            // controlla se c'è almeno un ttrigger attivo
            for (ScenarioTrigger trigger : triggers) {
                if (trigger.getStatus()) {
                    active = true;
                    break;
                }
            }
        }


        // se lo scenario è attivo avvia i programmi
        for (ScenarioProgram program : programs) {
            if (active) {
                program.startProgram();
            } else {
                program.stopProgram();
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

    private class Interval {
        Date start;
        Date end;
        int timeintervalid;

        Interval(Date start, Date end, int timeintervalid) {
            this.start = start;
            this.end = end;
            this.timeintervalid = timeintervalid;
        }
    }

    ;

    public List<NextTimeRangeAction> getNextTimeRangeActions(Date date) {

        if (triggers != null && triggers.size() > 0) {
            boolean triggerActive = false;
            for (ScenarioTrigger trigger : triggers) {
                if (trigger.getStatus()) {
                    triggerActive = true;
                }
            }
            if (!triggerActive)
                return null;
        }

        List<NextTimeRangeAction> scenarioNextTimeRangeActions = new ArrayList<>();

        List<Interval> intervals = new ArrayList<>();
        for (ScenarioTimeInterval timeInterval : calendar.timeIntervals) {

            if (timeInterval.endDateTime.before(date))
                continue;

            if (intervals.size() == 0) {
                intervals.add(new Interval(timeInterval.startDateTime, timeInterval.endDateTime,timeInterval.id));
                continue;
            }

            for (Interval interval : intervals) {
                // inizia prima e finisce dopo
                if (timeInterval.startDateTime.before(interval.start) && timeInterval.endDateTime.before(interval.start)) {
                    intervals.add(new Interval(timeInterval.startDateTime, timeInterval.endDateTime,timeInterval.id));
                    break;
                }
                // tutto dopo
                if (timeInterval.endDateTime.after(interval.end) && timeInterval.endDateTime.after(interval.end)) {
                    intervals.add(new Interval(timeInterval.startDateTime, timeInterval.endDateTime,timeInterval.id));
                    break;
                }
                // tutto dentro
                if (timeInterval.startDateTime.after(interval.end) && timeInterval.endDateTime.before(interval.end)) {
                    continue;
                }
                // inizia prima e finisce dentro
                if (timeInterval.startDateTime.before(interval.end)) {
                    interval.start = timeInterval.startDateTime;
                    interval.timeintervalid = timeInterval.id;
                    break;
                }
                // inizia dentro e finisce dopo
                if (timeInterval.endDateTime.before(interval.end)) {
                    interval.end = timeInterval.endDateTime;
                    interval.timeintervalid = timeInterval.id;
                }
            }
        }

        if (intervals.size() == 0)
            intervals.add(new Interval(date, null,0));

        for (Interval interval:intervals) {

            if (interval.start.before(date))
                interval.start = date;

            for (ScenarioProgram program : programs) {
                if (!program.enabled)
                    continue;
                List<NextTimeRangeAction> list = program.getNextTimeRangeActions(interval.start, interval.end);  /// qui va ionserito controllo giorni della settimana
                if (list != null) {
                    for (NextTimeRangeAction timeRangeAction : list) {
                        timeRangeAction.program = program;
                        timeRangeAction.timeintervalid = interval.timeintervalid;

                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.YEAR,timeRangeAction.date.getYear());
                        cal.set(Calendar.MONTH,timeRangeAction.date.getMonthValue()-1);
                        cal.set(Calendar.DAY_OF_MONTH,timeRangeAction.date.getDayOfMonth());
                        if (program.dayOfWeekActive(cal.get(Calendar.DAY_OF_WEEK)))
                            scenarioNextTimeRangeActions.add(timeRangeAction);
                    }
                }
            }
        }
        return scenarioNextTimeRangeActions;
    }
}


