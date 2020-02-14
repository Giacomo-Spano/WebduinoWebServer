package com.server.webduino.core.sensors;

import com.quartz.SensorStatusQuartzJob;
import com.server.webduino.core.Core;
import com.server.webduino.core.datalog.PingSensorDataLog;
import com.server.webduino.core.webduinosystem.Status;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Logger;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;


public class PingSensor extends SensorBase {

    private static Logger LOGGER = Logger.getLogger(PingSensor.class.getName());

    //private boolean open;
    public static final String STATUS_REACHABLE = "reacheable";
    public static final String STATUS_NOT_REACHABLE = "notreacheable";

    public static final String STATUS_DESCRIPTION_REACHABLE = "Raggiungibile";
    public static final String STATUS_DESCRIPTION_NOT_REACHABLE = "Non raggiungibile";

    private JobKey statusUpdatejobKey;
    //private Scheduler scheduler = null;


    public PingSensor(int id, String name, String description, String subaddress, int shieldid, String pin, boolean enabled) {
        super(id, name, description, subaddress, shieldid, pin, enabled);
        type = "pingsensor";
        datalog = new PingSensorDataLog(id);

        start();
    }

    @Override
    protected void createStatusList() {
        super.createStatusList();

        Status status = new Status(STATUS_REACHABLE,STATUS_DESCRIPTION_REACHABLE);
        statusList.add(status);
        status = new Status(STATUS_NOT_REACHABLE,STATUS_DESCRIPTION_NOT_REACHABLE);
        statusList.add(status);
    }

    @Override
    public void writeDataLog(String event) {
        datalog.writelog(event, this);
    }

    @Override
    public void updateFromJson(Date date, JSONObject json) {

        super.updateFromJson(date,json);
        LOGGER.info("updateFromJson json=" + json.toString());
        try {

            String message;
            if (getStatus().status.equals(STATUS_REACHABLE))
                message = "ON";
            else
                message = "OFF";
            Core.updateHomeAssistant("homeassistant/sensor/"+ id , message);

            JSONObject jsonattributes = new JSONObject();
            try {
                jsonattributes.put("sensorid", id);
                jsonattributes.put("shieldid", shieldid);
                jsonattributes.put("name", name);
                jsonattributes.put("description", description);
                jsonattributes.put("date", Core.getDate());
                jsonattributes.put("status", getStatus().status);
                jsonattributes.put("lastUpdate", lastUpdate);
                jsonattributes.put("type", type);
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            String attr_message = "{\"Attributes\":" + jsonattributes.toString() + "}";
            Core.updateHomeAssistant("homeassistant/sensor/" + id + "/attributes", attr_message);

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.info("json error: " + e.toString());
            writeDataLog("updateFromJson error");
        }
    }

    @Override
    public void getJSONField(JSONObject json) {
        /*try {
            //json.put("openstatus", open);

        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    public boolean requestStatusUpdate() {

        Date date =  Core.getDate();

        JSONObject jsonstatus = new JSONObject();
        try {
            if(ping()) {
                jsonstatus.put("status", STATUS_REACHABLE);
            } else {
                jsonstatus.put("status", STATUS_NOT_REACHABLE);
            }
            updateFromJson(date,jsonstatus);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public void requestAsyncSensorStatusUpdate() { // async sensor zonesensorstatus request
        /*SensorCommand cmd = new SensorCommand(SensorCommand.Command_RequestSensorStatusUpdate, shieldid, id);
        updating = true;

        SendCommandThread sendCommandThread = new SendCommandThread(cmd);
        Thread thread = new Thread(sendCommandThread, "sendCommandThread");
        thread.start();*/
        requestStatusUpdate();

    }

    public boolean ping() {
        String ipAddress = pin;
        InetAddress inet = null;

        try {
            inet = InetAddress.getByName(ipAddress);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }

        System.out.println("Sending Ping Request to " + ipAddress);

        try {
            if (inet.isReachable(5000)) {
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void start() {
        try {
            scheduleStatusUpdateRequestJob(60);
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        System.out.println("interrupt -  stop timer");
        deleteStatusUpdateRequestJob();
        //super.interrupt();
    }

    private void scheduleStatusUpdateRequestJob(int seconds) throws SchedulerException {

        Scheduler scheduler = new StdSchedulerFactory().getScheduler();

        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("sensor", this);
        jobDataMap.put("timeout", seconds);
        // define the job and tie it to our job's class
        statusUpdatejobKey = JobKey.jobKey("SensorStatusQuartzJob" + getId(), "Group");
        JobDetail job = newJob(SensorStatusQuartzJob.class)
                .withIdentity(statusUpdatejobKey)
                .usingJobData(jobDataMap)
                .build();
        // Trigger the job to run now, and then every seconds
        Trigger trigger = newTrigger()
                .withIdentity("SensorStatusTriggerName" + getId(), "Group")
                //.startNow()
                .withSchedule(simpleSchedule()
                        .withIntervalInSeconds(seconds)   // interroga ogni 1 minuti
                        .repeatForever())
                .build();
        // Setup the Job and WebduinoTrigger with Scheduler & schedule jobs
        scheduler.scheduleJob(job, trigger);

        printJobsAndTriggers(scheduler);
    }

    private void deleteStatusUpdateRequestJob() {
        try {
            Scheduler scheduler = new StdSchedulerFactory().getScheduler();
            printJobsAndTriggers(scheduler);
            scheduler.deleteJob(statusUpdatejobKey);
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
}
