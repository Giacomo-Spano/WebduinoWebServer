package com.server.webduino.core.webduinosystem.scenario;

import com.quartz.NextScenarioProgramTimeIntervalQuartzJob;
import com.quartz.NextScenarioTimeIntervalQuartzJob;
import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.scenario.actions.Action;
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

    public void setActionListener(Action.ActionListener toAdd) {

        if (timeRanges != null) {
            for (ScenarioProgramTimeRange timeRange : timeRanges) {
                timeRange.setActionListener(toAdd);
            }
        }
    }


    public ScenarioProgram(Connection conn, ResultSet resultSet) throws Exception {
        fromResultSet(conn, resultSet);
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
            jobKey = JobKey.jobKey("ScenarioProgramJob" + id, "my-jobs" + id);
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

        if (timeRanges != null)
            for (ScenarioProgramTimeRange timeRange : timeRanges) {
                timeRange.stop();
            }
    }

    public void triggerNextProgramTimeRange() {

        Date currentDate = Core.getDate();
        Instant instant = Instant.ofEpochMilli(currentDate.getTime());
        LocalTime currentTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();

        // controlla se all'ora corrente di oggi c'è una TimeRange attiva e la avvia
        Calendar c = Calendar.getInstance();
        ScenarioProgramTimeRange activeTimeRange = getActiveTimeRangeFromTimeAndDay(currentTime, c.get(Calendar.DAY_OF_WEEK));

        if (activeTimeRange != null && dayOfWeekActive(c.get(Calendar.DAY_OF_WEEK))) {
            active = true;
            activeTimeRange.start();
            // se esiste un time raneg attivo imposta la data di fine
        } else {
            active = false;
        }

        // schedula il prossimo ProgramTimeRangeJob, cioè schedula la prossima data e ora
        // in cui verrà fatto il prossimo controllo per vedere qual è il timerange attivo
        Date nexJobDate = getNextJobDate(currentDate, activeTimeRange);

        if (nexJobDate != null) {
            LOGGER.info("triggerNextTimeInterval id=" + id + "nextActivationDate=" + nexJobDate.toString());
            scheduleNextProgramTimeRangeJob(nexJobDate, activeTimeRange); // activeTime range sarà automaticamente fermato dal job quando parte
        } else {
            LOGGER.info("no triggerNextTimeInterval id=" + id);
        }
    }

    public Date getNextJobDate(Date date, ScenarioProgramTimeRange activeTimeRange) {
        // valuta la data della prossima fine del timerange corrente attivo oppure
        // quella dell'inizio del prossimo se non c'è nessun time range attivo

        Instant instant = Instant.ofEpochMilli(date.getTime());
        LocalTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();

        Date nexJobDate = null;
        Date activeTimeRangeEndDate = null;
        if (activeTimeRange != null) {
            // se esiste un time range attivo imposta la data di fine
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, activeTimeRange.endTime.getHour());
            cal.set(Calendar.MINUTE, activeTimeRange.endTime.getMinute());
            cal.set(Calendar.SECOND, activeTimeRange.endTime.getSecond());
            //cal.add(Calendar.MINUTE,1);
            activeTimeRangeEndDate = cal.getTime();
        }

        // controlla qual è il prossimo time range attivo e se esiste imposta
        // il prossimo nextTimeRangeStartDate alla prima data tra la fine del timerange attivo
        // e l'inizio del prossimo. Alla data nextTimeRangeStartDate sarà chiamato il prossimo Job che
        // chiamerà lo Stop del time range (
        Date nextTimeRangeStartDate = null;
        ScenarioProgramTimeRange nextTimeRange = getNextActiveTimeRangeFromDateTime(time);
        if (nextTimeRange != null) {
            Calendar nextTimeRangeStartDateCal = Calendar.getInstance();
            // imposta nextTimeRangeStartDateCal alla data corrente 'date'
            nextTimeRangeStartDateCal.setTime(date);
            if (nextTimeRange.startTime.compareTo(time) <= 0) {
                // se lo starttime è precedente porta avanti nextTimeRangeStartDateCal
                //  di un giorno
                nextTimeRangeStartDateCal.add(Calendar.HOUR, 24);
            }
            // imposta nextTimeRangeStartDateCal all'ora, minuti e secondi di nextTimeRange.startTime
            nextTimeRangeStartDateCal.set(Calendar.HOUR_OF_DAY, nextTimeRange.startTime.getHour());
            nextTimeRangeStartDateCal.set(Calendar.MINUTE, nextTimeRange.startTime.getMinute());
            nextTimeRangeStartDateCal.set(Calendar.SECOND, nextTimeRange.startTime.getSecond());

            if (!dayOfWeekActive(nextTimeRangeStartDateCal.get(Calendar.DAY_OF_WEEK))) {
                // Se il prossimo timerange non è attivo nel giorno della settimana corrente
                // aggiungi un giorno fino a che non ne trova uno attivo partento
                // dal primo Timerange del prossimo giorno
                ScenarioProgramTimeRange tr = getFirstTimeRangeofDay();
                nextTimeRangeStartDateCal.set(Calendar.HOUR_OF_DAY, tr.startTime.getHour());
                nextTimeRangeStartDateCal.set(Calendar.MINUTE, tr.startTime.getMinute());
                nextTimeRangeStartDateCal.set(Calendar.SECOND, tr.startTime.getMinute());
                // imposta nextTimeRangeStartDate a null prima di fare un loop sui prossimi
                // giorni per trovare il primo attivo
                nextTimeRangeStartDate = null;
                for (int i = 0; i < 7; i++) {
                    nextTimeRangeStartDateCal.add(Calendar.HOUR, 24);
                    if (dayOfWeekActive(nextTimeRangeStartDateCal.get(Calendar.DAY_OF_WEEK))) {
                        nextTimeRangeStartDate = nextTimeRangeStartDateCal.getTime();
                        break;
                    }
                }
                // se dopo aver fatto il loop di tutti i giorni della settimana
                // non trova nessun giorno nextTimeRangeStartDate rimarrà a null

            } else {
                // se il prossimo timerange è attivo nel giorno della settimana corrente
                // sta nextTimeRangeStartDate al nextTimeRangeStartDateCal
                nextTimeRangeStartDate = nextTimeRangeStartDateCal.getTime();
            }
        }

        // imposta il prossimo job alla fine dell'activeTimeRange oppure alla start date del prossimo
        // se la data è precedente oppure se l'active non esiste
        if (activeTimeRangeEndDate != null) {
            // esiste un TimeRange attivo alla data corrente
            if ((nextTimeRangeStartDate != null && nextTimeRangeStartDate.compareTo(activeTimeRangeEndDate) <= 0) ||
                    activeTimeRangeEndDate.compareTo(date) == 0) {
                // la data di inizio del prossimo timerange è precedente
                // alla data di fine di quello corrente oppure
                // la data corrente coincide con la fine del time range corrente
                Calendar c = Calendar.getInstance();
                c.setTime(nextTimeRangeStartDate);
                nexJobDate = c.getTime();
            } else {
                // la data di inizio del prossimo timerange è successiva
                // alla data di fine di quello corrente e
                // la data corrente non coincide con la fine del time range corrente
                Calendar c = Calendar.getInstance();
                c.setTime(activeTimeRangeEndDate);
                // imposta la prossima data ad un secondo dopo la fine del'activeTimerange
                c.add(Calendar.SECOND, 1);
                nexJobDate = c.getTime();
            }
        } else {
            // non esiste un TimeRange attivo alla data corrente
            if (nextTimeRangeStartDate != null) {
                Calendar c = Calendar.getInstance();
                c.setTime(nextTimeRangeStartDate);
                // imposta la prossima data ad un secondo dopo la fine del'activeTimerange
                c.add(Calendar.SECOND, 1);
                nexJobDate = c.getTime();
            }
        }
        return nexJobDate;
    }

    public List<NextTimeRangeAction> getNextTimeRangeActions(Date date, Date maxDate) {
        // ritorna la lista dei prossimi NextTimeRangeAction a partire da 'date' fino alla
        // massima data.
        // Più avanti controlla comunque che la data di fine non sia oltre 7 giorni
        // e che la lista non abbiam più di 20 elementi

        if (date == null)
            return null;

        List<NextTimeRangeAction> list = new ArrayList<>();
        Date nextDate = date;
        long diffDays = (nextDate.getTime() - date.getTime()) / (1000 * 60 * 60 * 24);

        // esegue un loop e aggiunge tutti i prossimi nextTimeRange
        // fino a che la data non supera 7 giorni oppure i time range sono più di 100
        // oppure il while ha fatto più di 1000 loop
        int counter = 0;
        while (diffDays < 7 && list.size() < 100 && counter++ < 1000) {

            Instant instant = Instant.ofEpochMilli(nextDate.getTime());
            LocalTime time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
            ScenarioProgramTimeRange timeRange;

            Calendar nextDateCalendar = Calendar.getInstance();
            nextDateCalendar.setTime(nextDate);
            timeRange = getActiveTimeRangeFromTimeAndDay(time, nextDateCalendar.get(Calendar.DAY_OF_WEEK));

            Date startDate = nextDate;
            nextDate = getNextJobDate(startDate, timeRange);

            if (nextDate == null)
                break;

            if (maxDate != null && nextDate.after(maxDate))
                nextDate = maxDate;

            if (timeRange != null && timeRange.actions != null) {
                // aggiunge un time range se esiste e se c'è almeno una action
                for (Action action : timeRange.actions) {
                        NextTimeRangeAction range = new NextTimeRangeAction();
                        range.date = Instant.ofEpochMilli(startDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                        range.start = time;
                        range.timeRange = timeRange;
                        range.action = action;

                        instant = Instant.ofEpochMilli(nextDate.getTime());
                        time = LocalDateTime.ofInstant(instant, ZoneId.systemDefault()).toLocalTime();
                        // mette la data di fine del range alla prima data tra la fine
                        // del time range e l'inizio del successivo
                        if (time.isBefore(timeRange.endTime)) {
                            range.end = time;
                        } else {
                            range.end = timeRange.endTime;
                        }
                        range.action = action;
                        list.add(range);

                        range = new NextTimeRangeAction();
                        range.date = Instant.ofEpochMilli(startDate.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
                        range.start = time;
                }
            }
            diffDays = (nextDate.getTime() - date.getTime()) / (1000 * 60 * 60 * 24);
        }
        return list;
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
                LOGGER.info("Found job identified by {} " + jobKey);
            }
        }
        for (String group : scheduler.getTriggerGroupNames()) {
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(GroupMatcher.<TriggerKey>groupEquals(group))) {
                LOGGER.info("Found trigger identified by {} " + triggerKey);
            }
        }
    }

    // ritorna il Timerange attivo all'ora 'time' e giorno 'day'. Se il programma non è attivo oggi ritorna null
    public ScenarioProgramTimeRange getActiveTimeRangeFromTimeAndDay(LocalTime time, int day) {

        if (timeRanges == null || timeRanges.size() == 0) return null;
        for (ScenarioProgramTimeRange timeRange : timeRanges) {
            if (timeRange.isActive(time)) return timeRange;
        }
        return null;
    }

    // ritorna la fascia orari attiva all'ora 'time' ssenza tenere conto del giorno della settimana
    public ScenarioProgramTimeRange getNextActiveTimeRangeFromDateTime(LocalTime time) {

        if (timeRanges == null || timeRanges.size() == 0) return null;

        // cerca la prossima fascia oraria che inizia dopo l'ora attuale
        for (ScenarioProgramTimeRange timeRange : timeRanges) {
            if (timeRange.startTime.compareTo(time) > 0)
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

    public boolean dayOfWeekActive(int dayOfWeek) {

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
            Calendar c = Calendar.getInstance();
            ScenarioProgramTimeRange activeTimeRange = getActiveTimeRangeFromTimeAndDay(Core.getTime(), c.get(Calendar.DAY_OF_WEEK));
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

                    /*if (i > 0 && timeRanges.get(timeRanges.size() - 1).endTime.compareTo(timeRange.startTime) > 0)
                        throw new Exception("time range " + i + "cannot start before time range " + (i - 1));*/
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
        priority = programsResultset.getInt("priority");
        this.timeRanges = readProgramTimeRanges(conn, id);
    }

    private List<ScenarioProgramTimeRange> readProgramTimeRanges(Connection conn, int programid) throws Exception {

        List<ScenarioProgramTimeRange> list = new ArrayList<>();
        String sql;
        Statement stmt = conn.createStatement();
        //sql = "SELECT * FROM scenarios_programtimeranges WHERE programid=" + programid + " ORDER BY scenarios_programtimeranges.index ASC;";
        sql = "SELECT * FROM scenarios_programtimeranges WHERE programid=" + programid + " ORDER BY scenarios_programtimeranges.starttime ASC;";
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
