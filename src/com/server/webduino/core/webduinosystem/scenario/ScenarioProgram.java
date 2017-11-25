package com.server.webduino.core.webduinosystem.scenario;

import com.quartz.NextScenarioProgramTimeIntervalQuartzJob;
import com.quartz.NextScenarioTimeIntervalQuartzJob;
import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.TimeRange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
//import java.Time;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class ScenarioProgram extends DBObject {

    private static final Logger LOGGER = Logger.getLogger(NextScenarioTimeIntervalQuartzJob.class.getName());

    public int id;
    public int scenarioId = 0;
    public String name = "";
    public String description = "";
    public boolean enabled = true;
    public boolean sunday = true;
    public boolean monday = true;
    public boolean tuesday = true;
    public boolean wednesday = true;
    public boolean thursday = true;
    public boolean friday = true;
    public boolean saturday = true;
    public int priority = 0;

    private JobKey jobKey;

    private boolean active = false;

    private Scheduler scheduler = null;
    private JobDetail nextProgramTimeRangeJob = null;
    private Date nextProgramTimeRangeJobDate = null;
    private Date startDate = null;
    private Date endDate = null;
    private Date programLastEndDate = null;

    public List<ScenarioProgramTimeRange> timeRanges = new ArrayList<>();

    public ScenarioProgram(Connection conn, ResultSet resultSet) throws Exception {
        fromResultSet(conn,resultSet);
    }


    public ScenarioProgram(JSONObject json) throws Exception {
        fromJson(json);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void startProgram() {

        boolean oldActiveStatus = active;
        if (!enabled) {
            active = false;
        } else {
                jobKey = JobKey.jobKey("ScenarioProgramJob"+id, "my-jobs"+id);
                //active = true;
                triggerNextProgramTimeRange();
        }
        if (oldActiveStatus != active) {
            if (active) {
               startDate = Core.getDate();
            } else {
                endDate = Core.getDate();
            }
        }
    }

    public void stopProgram() {
        active = false;
        deleteNextTimeRangeJob();

        for (ScenarioProgramTimeRange timeRange: timeRanges) {
            timeRange.stop();
        }
    }

    public void triggerNextProgramTimeRange() {

        Date currentDate = Core.getDate();
        Instant instant = Instant.ofEpochMilli(currentDate.getTime());
        LocalTime currentTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();

        // controlla se all'ora corrente c'è una TimeRange attiva e la avvia
        ScenarioProgramTimeRange activeTimeRange = getActiveTimeRangeFromDateTime(currentTime);
        if (activeTimeRange != null) {
            active = true;
            activeTimeRange.start();
        } else {
            active = false;
        }

        // schedula il prossimo ProgramTimeRangeJob, ciò schedula la prossima data e ora
        // in cui verrà fatto il prossimo controllo per vedere qual è il timerange attivo
        Date nexJobDate = null;

        Date activeTimeRangeEndDate = null;
        if (activeTimeRange != null) { // se esiste un time raneg attivo lo imposta alla sua fine
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, activeTimeRange.endTime.getHour());
            cal.set(Calendar.MINUTE, activeTimeRange.endTime.getMinute());
            cal.set(Calendar.SECOND, activeTimeRange.endTime.getSecond());
            activeTimeRangeEndDate = cal.getTime();
        }

        // controlla qual è il prossimo time range attivo e se esiste imposta
        // il prossimo ProgramTimeRange alla prima data tra la fine del timerange attivo
        // e l'inizio del prossimo
        Date nextTimeRangeStartDate = null;
        ScenarioProgramTimeRange nextTimeRange = getNextActiveTimeRangeFromDateTime(currentTime);
        if (nextTimeRange != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(Core.getDate());
            if (nextTimeRange.startTime.compareTo(currentTime) < 0) {
                cal.add(Calendar.HOUR, 24);
            }
            cal.set(Calendar.HOUR_OF_DAY, nextTimeRange.startTime.getHour());
            cal.set(Calendar.MINUTE, nextTimeRange.startTime.getMinute());
            cal.set(Calendar.SECOND, nextTimeRange.startTime.getSecond());

            if(!dayOfWeekActive(cal.get(Calendar.DAY_OF_WEEK))) {
                ScenarioProgramTimeRange tr = getFirstTimeRangeofDay();
                cal.set(Calendar.HOUR_OF_DAY, tr.startTime.getHour());
                cal.set(Calendar.MINUTE, tr.startTime.getMinute());
                cal.set(Calendar.SECOND, tr.startTime.getMinute());
                for (int i = 0;i < 7; i++) {
                    cal.add(Calendar.HOUR, 24);
                    if(dayOfWeekActive(cal.get(Calendar.DAY_OF_WEEK))) {
                        nextTimeRangeStartDate = cal.getTime();
                        break;
                    }
                }
            } else {
                nextTimeRangeStartDate = cal.getTime();
            }
        }

        if (activeTimeRangeEndDate != null) {
            if (nextTimeRangeStartDate.compareTo(activeTimeRangeEndDate) <= 0)
                nexJobDate = nextTimeRangeStartDate;
            else
                nexJobDate = activeTimeRangeEndDate;
        } else {
            if (nextTimeRangeStartDate != null)
                nexJobDate = nextTimeRangeStartDate;
        }
        if (nexJobDate != null) {
            LOGGER.info("triggerNextTimeInterval id=" + id + "nextActivationDate=" + nexJobDate.toString());
            scheduleNextProgramTimeRangeJob(nexJobDate, activeTimeRange); // activeTime range sarà automaticamente fermato dal job quando parte
        } else {
            LOGGER.info("no triggerNextTimeInterval id=" + id);
        }
    }

    private void deleteNextTimeRangeJob() {
        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            printJobsAndTriggers(scheduler);
            scheduler.deleteJob(jobKey);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private void scheduleNextProgramTimeRangeJob(Date date, ScenarioProgramTimeRange currentTimeRange) {

        deleteNextTimeRangeJob();

        //pass the servlet context to the job
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("program", this);
        if (currentTimeRange != null)
            jobDataMap.put("timerange", currentTimeRange);
        // define the job and tie it to our job's class

        nextProgramTimeRangeJob = newJob(NextScenarioProgramTimeIntervalQuartzJob.class)
                //.withIdentity("CronNextProgramTimeIntervalQuartzJob" + this.id, "Group")
                .withIdentity(jobKey)
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = newTrigger()
                .withIdentity("NextTimeRangeTrigger" + this.id, "Group")
                .startAt(date)
                .build();

        try {
            printJobsAndTriggers(scheduler);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        try {
            nextProgramTimeRangeJobDate = scheduler.scheduleJob(nextProgramTimeRangeJob, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private void printJobsAndTriggers(Scheduler scheduler) throws SchedulerException {
        LOGGER.info("Quartz Scheduler: {}" + scheduler.getSchedulerName());
        for (String group : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.<JobKey>groupEquals(group))) {
                LOGGER.info("Found job identified by {} " +  jobKey);
            }
        }
        for (String group : scheduler.getTriggerGroupNames()) {
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(group))) {
                LOGGER.info("Found trigger identified by {} " +  triggerKey);
            }
        }
    }

    // ritorna il Timerange attivo all'ora current time di oggi. Se il programma non è attivo oggi ritorna null
    public ScenarioProgramTimeRange getActiveTimeRangeFromDateTime(LocalTime currentTime) {

        Calendar c = Calendar.getInstance();
        if (!dayOfWeekActive(c.get(Calendar.DAY_OF_WEEK))) return null;

        if (timeRanges == null || timeRanges.size() == 0) return null;
        for (ScenarioProgramTimeRange timeRange : timeRanges) {
            if (timeRange.isActive(currentTime)) return timeRange;
        }
        return null;
    }

    // ritorna la fascia orari attiva alla current time ssenza tenere conto del giorno della settimana
    public ScenarioProgramTimeRange getNextActiveTimeRangeFromDateTime(LocalTime currentTime) {

        if (timeRanges == null || timeRanges.size() == 0) return null;

        // cerca la prossima fascia oraria che inizia dopo l'ora attuale
        for (ScenarioProgramTimeRange timeRange : timeRanges) {
            if (timeRange.startTime.compareTo(currentTime) > 0)
                return timeRange;
        }
        // se non c'è nessuna fascia che inizia dopop l'ora attuale
        // ritorna la prima fascia
        return timeRanges.get(0);
    }

    public ScenarioProgramTimeRange getFirstTimeRangeofDay() {

        if (timeRanges == null || timeRanges.size() == 0) return null;
        return timeRanges.get(0);
    }

    boolean dayOfWeekActive(int dayOfWeek) {

        switch (dayOfWeek) {
            case Calendar.SUNDAY:
                if (sunday)
                    return true;
                break;
            case Calendar.MONDAY:
                if (monday)
                    return true;
                break;
            case Calendar.TUESDAY:
                if (tuesday)
                    return true;
                break;
            case Calendar.WEDNESDAY:
                if (wednesday)
                    return true;
                break;
            case Calendar.THURSDAY:
                if (thursday)
                    return true;
                break;
            case Calendar.FRIDAY:
                if (friday)
                    return true;
                break;
            case Calendar.SATURDAY:
                if (saturday)
                    return true;
                break;
            default:
                break;
        }
        return false;
    }

    @Override
    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("scenarioid", scenarioId);
            json.put("name", name);
            json.put("description", description);
            json.put("enabled", enabled);
            json.put("sunday", sunday);
            json.put("monday", monday);
            json.put("tuesday", tuesday);
            json.put("wednesday", wednesday);
            json.put("thursday", thursday);
            json.put("friday", friday);
            json.put("saturday", saturday);
            json.put("priority", priority);
            JSONArray jarray = new JSONArray();
            if (timeRanges != null) {
                for (ScenarioProgramTimeRange timeRange : timeRanges) {
                    jarray.put(timeRange.toJson());
                }
                json.put("timeranges", jarray);
            }

            // dynamic values
            if (active)
                json.put("status", "Attivo");
            else
                json.put("status", "Non attivo");
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            if (nextProgramTimeRangeJobDate != null)
                json.put("nextjobdate", df.format(nextProgramTimeRangeJobDate));
            if (startDate != null)
                json.put("startdate", df.format(startDate));
            if (endDate != null)
                json.put("enddate", df.format(endDate));
            ScenarioProgramTimeRange activeTimeRange = getActiveTimeRangeFromDateTime(Core.getTime());
            if (activeTimeRange != null)
                json.put("activetimerange", activeTimeRange.toJson());

            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void fromJson(JSONObject json) throws Exception {

        if (json.has("id")) id = json.getInt("id");
        if (json.has("scenarioid")) scenarioId = json.getInt("scenarioid");
        if (json.has("name")) name = json.getString("name");
        if (json.has("description")) description = json.getString("description");
        if (json.has("enabled")) enabled = json.getBoolean("enabled");
        if (json.has("sunday")) sunday = json.getBoolean("sunday");
        if (json.has("monday")) monday = json.getBoolean("monday");
        if (json.has("tuesday")) tuesday = json.getBoolean("tuesday");
        if (json.has("wednesday")) wednesday = json.getBoolean("wednesday");
        if (json.has("thursday")) thursday = json.getBoolean("thursday");
        if (json.has("friday")) friday = json.getBoolean("friday");
        if (json.has("saturday")) saturday = json.getBoolean("saturday");
        if (json.has("priority")) priority = json.getInt("priority");
        if (json.has("timeranges")) {
            JSONArray jsonArray = json.getJSONArray("timeranges");
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                ScenarioProgramTimeRange timeRange = new ScenarioProgramTimeRange(jo);
                if (timeRange != null) {

                    if (i > 0 && timeRanges.get(timeRanges.size()-1).endTime.compareTo(timeRange.startTime) > 0)
                        throw new Exception("time range " + i + "cannot start before time range "+ (i-1));
                    timeRanges.add(timeRange);
                }
            }
        }
    }

    @Override
    public void write(Connection conn) throws SQLException {

        String sql = "INSERT INTO scenarios_programs (id, scenarioid, name, description, enabled, sunday, monday, tuesday,wednesday, thursday, friday, saturday, priority)" +
                " VALUES ("
                + id + ","
                + scenarioId + ","
                + "\"" + name + "\","
                + "\"" + description + "\","
                + Core.boolToString(enabled) + ","
                + "" + Core.boolToString(sunday) + ","
                + "" + Core.boolToString(monday) + ","
                + "" + Core.boolToString(tuesday) + ","
                + "" + Core.boolToString(wednesday) + ","
                + "" + Core.boolToString(thursday) + ","
                + "" + Core.boolToString(friday) + ","
                + "" + Core.boolToString(saturday) + "," +
                +priority
                + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "scenarioid=" + scenarioId + ","
                + "name=\"" + name + "\","
                + "description=\"" + description + "\","
                + "enabled=" + Core.boolToString(enabled) + ","
                + "sunday=" + Core.boolToString(sunday) + ","
                + "monday=" + Core.boolToString(monday) + ","
                + "tuesday=" + Core.boolToString(tuesday) + ","
                + "wednesday=" + Core.boolToString(wednesday) + ","
                + "thursday=" + Core.boolToString(thursday) + ","
                + "friday=" + Core.boolToString(friday) + ","
                + "saturday=" + Core.boolToString(saturday) + ","
                + "priority=" + priority + ";";
        //Statement stmt = conn.createStatement();
        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }

        for (ScenarioProgramTimeRange timeRange : timeRanges) {
            timeRange.write(conn);
        }
        stmt.close();
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM scenarios_programs WHERE id=" + id;
        stmt.executeUpdate(sql);
    }

    private void fromResultSet(Connection conn, ResultSet programsResultset) throws Exception {
        id = programsResultset.getInt("id");
        enabled = programsResultset.getBoolean("enabled");
        name = programsResultset.getString("name");
        description = programsResultset.getString("description");
        scenarioId = programsResultset.getInt("scenarioid");
        sunday = programsResultset.getBoolean("sunday");
        monday = programsResultset.getBoolean("monday");
        tuesday = programsResultset.getBoolean("tuesday");
        wednesday = programsResultset.getBoolean("wednesday");
        thursday = programsResultset.getBoolean("thursday");
        friday = programsResultset.getBoolean("friday");
        saturday = programsResultset.getBoolean("saturday");
        this.timeRanges = readProgramTimeRanges(conn, id);
    }

    private static List<ScenarioProgramTimeRange> readProgramTimeRanges(Connection conn, int programid) throws Exception {

        List<ScenarioProgramTimeRange> list = new ArrayList<>();
        String sql;
        Statement stmt = conn.createStatement();
        sql = "SELECT * FROM scenarios_programtimeranges WHERE programid=" + programid + " ORDER BY scenarios_programtimeranges.index ASC;";
        ResultSet resultSet = stmt.executeQuery(sql);
        while (resultSet.next()) {
            ScenarioProgramTimeRange timeRange = new ScenarioProgramTimeRange(conn, programid, resultSet);
            if (timeRange != null) {
                list.add(timeRange);
            }
        }
        resultSet.close();
        stmt.close();
        if (list.size() == 0)
            return null;
        return list;
    }
}
