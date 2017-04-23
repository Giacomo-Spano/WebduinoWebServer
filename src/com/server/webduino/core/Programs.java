package com.server.webduino.core;

import com.quartz.NextProgramQuartzJob;
import com.server.webduino.core.sensors.Actuator;
import com.server.webduino.core.sensors.HeaterActuator;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.TemperatureSensor;
import com.server.webduino.servlet.SendPushMessages;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
 * Created by Giacomo Span� on 07/11/2015.
 */
public class Programs {

    Scheduler scheduler = null;
    JobDetail mNextProgramJob = null;

    public Programs() {
        actuatorStatus = "";
    }

    public interface ProgramsListener {
        void programChanged(ActiveProgram newProgram);
    }

    protected List<Programs.ProgramsListener> listeners = new ArrayList<>();

    public void addListener(Programs.ProgramsListener toAdd) {
        listeners.add(toAdd);
    }

    protected int actuatorId;

    protected String actuatorStatus;


    protected int sensorId;
    private static final Logger LOGGER = Logger.getLogger(Programs.class.getName());
    private ArrayList<Program> mProgramList;
    protected ArrayList<ActiveProgram> mNextProgramList;
    private Date mLastActiveProgramUpdate;
    protected ActiveProgram mOldActiveProgram = null;
    private double mActiveSensorTemperature;
    private Date lastTemperatureSent;

    protected double oldTemperature = 0.0;


    public void init(SensorBase sensor) {

        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            //scheduler.start();
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void read(int sensorid) { // reload all program data from db

        mProgramList = new ArrayList<Program>();
        sensorId = sensorid;
        String sql;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();

            //sql = "SELECT id, active, name, dateenabled, starttime, startdate, endtime, enddate, sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority FROM programs ORDER BY priority ASC";
            sql = "SELECT * FROM programs WHERE sensorid=" + sensorid + " ORDER BY priority ASC" + ";";
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            while (rs.next()) {
                Program program = new Program();
                //Retrieve by column name
                program.id = rs.getInt("id");
                program.sensorId = rs.getInt("sensorid");
                program.active = (boolean) rs.getObject("active");
                program.dateEnabled = (boolean) rs.getObject("dateenabled");
                program.name = rs.getString("name");
                program.startDate = rs.getDate("startdate");
                program.startTime = rs.getTime("starttime");
                program.endDate = rs.getDate("enddate");
                program.endTime = rs.getTime("endtime");
                program.Sunday = rs.getBoolean("sunday");
                program.Monday = rs.getBoolean("monday");
                program.Tuesday = rs.getBoolean("tuesday");
                program.Wednesday = rs.getBoolean("wednesday");
                program.Thursday = rs.getBoolean("thursday");
                program.Friday = rs.getBoolean("friday");
                program.Saturday = rs.getBoolean("saturday");
                program.priority = rs.getInt("priority");

                mProgramList.add(program);
            }
            // Clean-up environment
            rs.close();

            for (int i = 0; i < mProgramList.size(); i++) {
                stmt = conn.createStatement();
                sql = "SELECT * FROM timeranges WHERE programid=" + mProgramList.get(i).id + " ORDER BY priority ASC";
                rs = stmt.executeQuery(sql);

                while (rs.next()) {
                    TimeRange range = new TimeRange();
                    //Retrieve by column name
                    range.ID = rs.getInt("id");
                    range.programID = rs.getInt("programid");
                    range.name = rs.getString("name");
                    range.sensorId = rs.getInt("sensorid");
                    range.endTime = rs.getTime("endtime");
                    range.temperature = rs.getDouble("temperature");
                    range.priority = rs.getInt("priority");
                    //range.subAddress = rs.getString("subaddress");

                    mProgramList.get(i).mTimeRanges.add(range);
                }
                rs.close();
                stmt.close();
            }
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        //checkProgram();
    }

    public void checkProgram() {

        LOGGER.info("checkProgram");
        mLastActiveProgramUpdate = Core.getDate();

        if (actuatorStatus.equals(HeaterActuator.STATUS_MANUAL) || actuatorStatus.equals(HeaterActuator.STATUS_MANUALOFF)) {
            LOGGER.info("->actuator in manual mode");
            mOldActiveProgram = null;
            return;
        }

        LOGGER.info("->Check active program");
        Date currentDate = Core.getDate();

        // calcola il programma attivo ora
        ActiveProgram activeProgram = getActiveProgram(currentDate);

        // se non esiste nessun  programma attivo riprocva tra un mionuto
        if (activeProgram == null) {
            LOGGER.info("->No active program");
            Core.sendPushNotification(SendPushMessages.notification_error, "errore", "no active program", "0", 0);
            mOldActiveProgram = null;

            // schedula controllo programmi tra un minuto
            Date date = Core.getDate();
            Calendar now = Calendar.getInstance();
            now.setTime(date);
            now.add(Calendar.MINUTE, 1);
            Date oneMinuteFromNow = now.getTime();
            scheduleJob(oneMinuteFromNow);

            mOldActiveProgram = activeProgram;
            return;
        }
        // aggiorna la lista dei prossimo programmi e schedula il successivo
        mNextProgramList = nextPrograms(currentDate);
        LOGGER.info("found " + mNextProgramList.size() + "active program");
        // se esiste almeno un programma schedula la prossima chiamata a checkprogranm alla fine di quello corrente e l'inizio del successivo
        // All'enddate del primo sarà chiamata la check program dal job
        if (mNextProgramList == null) {
            LOGGER.severe("mNextProgramList NULL");
        } else if (mNextProgramList.get(0) != null && mNextProgramList.get(0).endDate != null) {
            Date date = mNextProgramList.get(0).endDate;
            scheduleJob(date);
        } else {
            LOGGER.severe("CANNOT schedule next job");
        }


        if (activeProgram == null)
            return;

        boolean programChanged = false;
        if (mOldActiveProgram == null || activeProgram.program.id != mOldActiveProgram.program.id
                || activeProgram.timeRange.ID != mOldActiveProgram.timeRange.ID) {
            programChanged = true;
        }
        mOldActiveProgram = activeProgram;

        if (programChanged) {
            for (ProgramsListener listener : listeners) {
                listener.programChanged(activeProgram);
            }
        }
    }

    private void scheduleJob(Date date) {

        try {
            if (mNextProgramJob != null)
                scheduler.deleteJob(mNextProgramJob.getKey());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        //pass the servlet context to the job
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("programs", this);
        // define the job and tie it to our job's class
        mNextProgramJob = newJob(NextProgramQuartzJob.class).withIdentity(
                "CronNextProgramQuartzJob", "Group")
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = newTrigger()
                .withIdentity("NextProgramTriggerName", "Group")
                .startAt(date)
                .build();

        Date dd;
        try {
            dd = scheduler.scheduleJob(mNextProgramJob, trigger);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<ActiveProgram> nextPrograms(Date currentDate) {

        //LOGGER.info("nextPrograms");

        ActiveProgram currentProgram = getActiveProgram(currentDate);//mActiveProgram;

        Calendar nextDateTimeCalendar = Calendar.getInstance();
        nextDateTimeCalendar.setTime(currentDate);

        Calendar nextDateCalendar = Calendar.getInstance();
        nextDateCalendar.setTime(nextDateTimeCalendar.getTime());
        nextDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
        nextDateCalendar.set(Calendar.MINUTE, 0);
        nextDateCalendar.set(Calendar.SECOND, 0);


        ArrayList<ActiveProgram> nextProgramList = new ArrayList<ActiveProgram>();

        // calcola i prossimi programmi attivi per i prossimi sette giorni
        for (int i = 0; i < 7; i++) {

            LOGGER.info("i=" + i);

            String t = "23:59:00";
            Time eodtime = Time.valueOf(t);
            while (currentProgram != null && currentProgram.timeRange.endTime.before(eodtime)) {

                ActiveProgram nextActive = null;
                Calendar endtime = Calendar.getInstance();
                // imposta la data fine time range alla fine del time range oppure
                // all'ora di fine del programmma
                if (currentProgram.program.dateEnabled && currentProgram.program.endDate.equals(nextDateCalendar.getTime()) && currentProgram.program.endTime.before(currentProgram.timeRange.endTime)) {
                    endtime.setTime(currentProgram.program.endTime);
                } else {
                    endtime.setTime(currentProgram.timeRange.endTime);
                }
                Date date = endtime.getTime();
                LOGGER.info("endtime=" + date.toString());

                // cerca un programma con data e ora programma precedente alla fine dell'ore fi fine del timerange corrente
                Iterator<Program> iterator = mProgramList.iterator();
                while (iterator.hasNext()) {

                    Program program = iterator.next();

                    LOGGER.info("program.id=" + program.id);

                    if (program.dateEnabled && program.startDate.equals(nextDateCalendar.getTime())
                            && program.startTime.before(currentProgram.timeRange.endTime)) {

                        LOGGER.info("program.dateEnabled=" + program.dateEnabled);
                        LOGGER.info("program.startDate=" + program.startDate);
                        LOGGER.info("program.startTime=" + program.startTime);
                        LOGGER.info("currentProgram.timeRange=" + currentProgram.timeRange.endTime);
                        nextActive = new ActiveProgram();
                        nextActive.program = program;
                        Iterator<TimeRange> timerangeIterator = program.mTimeRanges.iterator();
                        nextActive.timeRange = (TimeRange) timerangeIterator.next();
                        while (timerangeIterator.hasNext()) {
                            if (nextActive.timeRange.endTime.before(endtime.getTime())) {
                                nextActive.timeRange = timerangeIterator.next();
                                endtime.setTime(program.startTime);
                            }
                        }
                    }
                }

                // avanza alla timerange successiva
                nextDateTimeCalendar.set(Calendar.HOUR_OF_DAY, endtime.get(Calendar.HOUR_OF_DAY));
                nextDateTimeCalendar.set(Calendar.MINUTE, endtime.get(Calendar.MINUTE));
                nextDateTimeCalendar.set(Calendar.SECOND, 0);
                nextDateTimeCalendar.set(Calendar.MILLISECOND, 0);
                //nextDateTimeCalendar.add(Calendar.MINUTE, 1);
                date = nextDateTimeCalendar.getTime();
                LOGGER.info("nextDateTimeCalendar=" + date.toString());

                Date next = nextDateTimeCalendar.getTime();
                if (currentProgram != null) {
                    currentProgram.endDate = next;
                    LOGGER.info("add program: " + currentProgram);
                    nextProgramList.add(currentProgram);
                }

                currentProgram = getActiveProgram(next);
            }

            // avanza alla mezzanote del giorno successivo
            nextDateTimeCalendar.set(Calendar.HOUR_OF_DAY, 0);
            nextDateTimeCalendar.set(Calendar.MINUTE, 0);
            nextDateTimeCalendar.set(Calendar.SECOND, 0);
            nextDateTimeCalendar.set(Calendar.MILLISECOND, 0);
            nextDateTimeCalendar.add(Calendar.DATE, 1);
            Date next = nextDateTimeCalendar.getTime();
            if (currentProgram != null) {
                currentProgram.endDate = next;
                LOGGER.info("add program: " + currentProgram);
                nextProgramList.add(currentProgram);
            }

            nextDateCalendar.setTime(nextDateTimeCalendar.getTime());
            currentProgram = getActiveProgram(nextDateTimeCalendar.getTime());
        }

        if (nextProgramList != null)
            LOGGER.info("nextProgramList: " + nextProgramList);
        else
            LOGGER.info("nextProgramList: null");
        return nextProgramList;
    }

    protected ActiveProgram getActiveProgram(Date currentDate) {
        //LOGGER.info("getActiveProgram " + currentDate.toString());
        // loop di tutti i programmi per trovare quello attivo alla data corrente
        ActiveProgram activeProgram = null;

        Iterator<Program> iterator = mProgramList.iterator();
        while (iterator.hasNext()) {
            //LOGGER.info("activeProgram " + activeProgram.program.id);
            Program program = iterator.next();
            //LOGGER.info("program " + program.id);
            TimeRange timerange = program.getActiveTimeRange(currentDate);

            if (timerange != null) {
                //LOGGER.info("timerange " + timerange.ID);
                if (activeProgram != null/*mActiveProgram != null && activeTimerange != null*/) {
                    //LOGGER.info("activeProgram == null");
                    //if (program.priority > mActiveProgram.priority || (program.dateEnabled && !mActiveProgram.dateEnabled))
                    if (program.priority > activeProgram.program.priority || (program.dateEnabled && !activeProgram.program.dateEnabled)) {
                        activeProgram = new ActiveProgram();
                        activeProgram.program = program;
                        activeProgram.timeRange = timerange;
                        activeProgram.startDate = currentDate;
                    }
                } else {
                    //LOGGER.info("activeProgram == null");
                    activeProgram = new ActiveProgram();
                    activeProgram.program = program;
                    activeProgram.timeRange = timerange;
                    activeProgram.startDate = currentDate;
                }
            }
        }
        if (activeProgram != null) {
            //LOGGER.info("return activeProgram " + activeProgram.program.id);
        } else {
            //LOGGER.info("return activeProgram null");
        }
        return activeProgram;
    }

    public ArrayList<Program> getProgramList() {

        return mProgramList;
    }

    public Date getLastActiveProgramUpdate() {

        return mLastActiveProgramUpdate;
    }

    public void writelog(java.util.Date date, double currentTemperature, double targetTemperature, double localTemperature, boolean releStatus, int activeProgramId, int activeTimeRangeId, int activeSensorId) {

        try {

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strdate = dateFormat.format(date);
            DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String strtime = timeFormat.format(date);

            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "INSERT INTO datalog (date, time, currenttemperature, targettemperature, localtemperature, activeprogram, activetimerange, relestatus, activesensor) VALUES" +
                    " (" + "'" + strdate + "','" + strtime + "'," + currentTemperature + "," + targetTemperature + "," + localTemperature + "," + activeProgramId + "," + activeTimeRangeId + "," + releStatus + "," + activeSensorId + ");";
            stmt.executeUpdate(sql);

            // Extract data from result set
            // Clean-up environment
            //rs.close();
            stmt.close();

            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }

    public int delete(int id) {

        try {

            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            String sql;
            sql = "DELETE FROM programs WHERE id=" + id;
            Statement stmt = conn.createStatement();
            int numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
/// questa sezione sembra duplicata???? perchèmcancella due volte?????? DA CORREGGERE e eliminare
            sql = "DELETE FROM programs WHERE id=" + id;
            stmt = conn.createStatement();
            numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);

            sql = "DELETE FROM timeranges WHERE programid=" + id;
            numero = stmt.executeUpdate(sql);

            stmt.close();
            conn.close();

            read(sensorId);

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return 0;
        }
        return id;
    }

    public int insert(Program prgm) {

        LOGGER.info("update prgm=" + prgm.id + " " + prgm.name + " sensorid=" + prgm.sensorId);

        Connection conn = null;
        int lastid;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            /*Connection */
            conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());

            conn.setAutoCommit(false);
            // Execute SQL query
            //Statement stmt = conn.createStatement();
            String sql;

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat tf = new SimpleDateFormat("HH:mm:ss");

            String startDate = "NULL";
            if (prgm.startDate != null)
                startDate = "'" + df.format((prgm.startDate)) + "'";
            String startTime = "NULL";
            if (prgm.startTime != null)
                startTime = "'" + tf.format((prgm.startTime)) + "'";
            String endDate = "NULL";
            if (prgm.endDate != null)
                endDate = "'" + df.format((prgm.endDate)) + "'";
            String endTime = "NULL";
            if (prgm.endTime != null)
                endTime = "'" + tf.format((prgm.endTime)) + "'";


            sql = "INSERT INTO programs (id, sensorid, priority, active,name,dateenabled, startdate, starttime, enddate, endtime, sunday, monday, tuesday, wednesday, thursday, friday, saturday)" +
                    " VALUES (" + prgm.id + "," + prgm.sensorId + "," + prgm.priority + "," + prgm.active + "," + "'" + prgm.name + "'" + "," + prgm.dateEnabled + "," + startDate + "," + startTime + "," + endDate + "," + endTime + ","
                    + Core.boolToString(prgm.Sunday) + ","
                    + Core.boolToString(prgm.Monday) + ","
                    + Core.boolToString(prgm.Tuesday) + ","
                    + Core.boolToString(prgm.Wednesday) + ","
                    + Core.boolToString(prgm.Thursday) + ","
                    + Core.boolToString(prgm.Friday) + ","
                    + Core.boolToString(prgm.Saturday) + ") ON DUPLICATE KEY UPDATE " +
                    "sensorid=" + prgm.sensorId + "," +
                    "priority=" + prgm.priority + "," +
                    "active=" + prgm.active + "," +
                    "name=" + "'" + prgm.name + "'" + "," +
                    "dateenabled=" + prgm.dateEnabled + "," +
                    "startdate=" + startDate + "," +
                    "starttime=" + startTime + "," +
                    "enddate=" + endDate + "," +
                    "endtime=" + endTime + "," +
                    "sunday=" + prgm.Sunday + "," +
                    "monday=" + prgm.Monday + "," +
                    "tuesday=" + prgm.Tuesday + "," +
                    "wednesday=" + prgm.Wednesday + "," +
                    "thursday=" + prgm.Thursday + "," +
                    "friday=" + prgm.Friday + "," +
                    "saturday=" + prgm.Saturday;// ";//id=LAST_INSERT_ID(id)";

            Statement stmt = conn.createStatement();
            Integer numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = prgm.id;
            }

            sql = "DELETE FROM timeranges WHERE programid=" + prgm.id;
            numero = stmt.executeUpdate(sql);


            for (int i = 0; i < prgm.mTimeRanges.size(); i++) {

                TimeRange tr = prgm.mTimeRanges.get(i);
                tr.programID = lastid;//prgm.id;


                if (tr.endTime != null)
                    endTime = "'" + tf.format((tr.endTime)) + "'";
                else
                    endTime = "NULL";

                sql = "INSERT INTO timeranges (id, programid, name, priority, endtime, sensorid, temperature)" +
                        " VALUES (" + tr.ID + "," + tr.programID + ",'" + tr.name + "'," + tr.priority + "," + endTime + "," + tr.sensorId + "," + tr.temperature + ")";

                numero = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            }


            stmt.close();
            conn.commit();


        } catch (SQLException se) {
            //Handle errors for JDBC
            System.out.println(se.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;

        } catch (Exception e) {
            //Handle errors for Class.forName
            System.out.println(e.getMessage());
            try {
                conn.rollback();
            } catch (SQLException e2) {
                e2.printStackTrace();
            }
            return 0;
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        read(prgm.sensorId); // reload data
        return lastid;
    }

    public Program getProgramFromId(int id) {
        Iterator<Program> iterator = mProgramList.iterator();
        while (iterator.hasNext()) {
            Program program = iterator.next();
            if (program.id == id)
                return program;
        }
        return null;
    }

    public ArrayList<ActiveProgram> getActiveProgramList() {

        return mNextProgramList;
    }

    public void setHeaterStatus(String status) {
        actuatorStatus = status;
    }

    public void heaterProgramChange(HeaterActuator heater, int newProgram, int oldProgram, int newTimerange, int oldTimerange) {

        /*String activeSensor = "no active sensors";

        if (heater != null)
            activeSensor = "" + heater.getActiveSensorID();

        Program p = getProgramFromId(heater.activeProgramID);
        String program = "no program";
        String timerange = "no timerange";
        if (p != null) {
            program = "" + p.id + "." + p.name;
            TimeRange tr = p.getTimeRangeFromId(heater.activeTimeRangeID);
            if (tr != null) {
                timerange = "" + tr.ID + "." + tr.name;
            }
        }*/
    }

    public void setSensorTemperature(double temperature) {
        // chiamato quando cambia la temperatura delsensore di temperatura attivo
        LOGGER.info("changeTemperature sensorId=" + sensorId + ", temperature = " + temperature);

        double roundedTemperature;
        BigDecimal bd = new BigDecimal(temperature).setScale(1, RoundingMode.HALF_EVEN);
        roundedTemperature = bd.doubleValue();

        if (oldTemperature == temperature) {
            return;
        }
        mActiveSensorTemperature = roundedTemperature;
    }
}
