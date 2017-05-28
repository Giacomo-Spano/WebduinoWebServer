package com.server.webduino.core.webduinosystem.scenario;

import com.quartz.NextScenarioTimeIntervalQuartzJob;
import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.programinstructions.ProgramInstructions;
import com.server.webduino.core.webduinosystem.scenario.ScenarioTimeInterval;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

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
    public int priority;

    private ScenarioTimeInterval activeTimeInterval = null;
    private JobDetail nextTimeIntervalJob = null;
    private Scheduler scheduler = null;

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
                "CronNextTimeIntervalQuartzJob", "Group")
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = newTrigger()
                .withIdentity("NextTimeIntervalTriggerName", "Group")
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
            json.put("active", active);
            json.put("calendar", calendar.toJson());
            json.put("priority", priority);

            JSONArray jsonArray = new JSONArray();
            for (ScenarioTimeInterval timeInterval: calendar.timeIntervals) {
                for (ProgramInstructions instruction : timeInterval.programInstructionsList) {
                    jsonArray.put(instruction);
                }
            }
            json.put("programinstructions", jsonArray);

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    public class ScenarioCalendar {
        public boolean dateEnabled;

        public Date startDate;
        public Date endDate;
        public ArrayList<ScenarioTimeInterval> timeIntervals = new ArrayList<>();
        private int priority;

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

            Iterator<ScenarioTimeInterval> iterator = timeIntervals.iterator();
            ScenarioTimeInterval activeTimeInterval = null;
            Time startTime;
            ScenarioTimeInterval currentTimeInterval = null;
            while (iterator.hasNext()) {
                if (currentTimeInterval == null) { //primo giro
                    startTime = Time.valueOf("00:00:00");
                } else {
                    startTime = currentTimeInterval.endTime;
                }
                currentTimeInterval = iterator.next();
                if (!currentTimeInterval.endTime.equals(Time.valueOf("00:00:00"))) {

                    if (currentTime.after(currentTimeInterval.endTime) || currentTime.equals(currentTimeInterval.endTime)) {
                        //startTime = currentTimeRange.endTime;
                        continue;
                    }
                    if (currentTime.before(startTime)) {
                        //startTime = currentTimeRange.endTime;
                        continue;
                    }

                    Calendar c = Calendar.getInstance();
                    c.setTime(currentDate);
                    int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                    switch (dayOfWeek) {
                        case Calendar.SUNDAY:
                            if (!currentTimeInterval.sunday) continue;
                            break;
                        case Calendar.MONDAY:
                            if (!currentTimeInterval.monday) continue;
                            break;
                        case Calendar.TUESDAY:
                            if (!currentTimeInterval.tuesday) continue;
                            break;
                        case Calendar.WEDNESDAY:
                            if (!currentTimeInterval.wednesday) continue;
                            break;
                        case Calendar.THURSDAY:
                            if (!currentTimeInterval.thursday) continue;
                            break;
                        case Calendar.FRIDAY:
                            if (!currentTimeInterval.friday) continue;
                            break;
                        case Calendar.SATURDAY:
                            if (!currentTimeInterval.saturday) continue;
                            break;
                        default:
                            break;
                    }
                }
                activeTimeInterval = currentTimeInterval;
                return activeTimeInterval;
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
                for (ScenarioTimeInterval timeInterval:timeIntervals) {

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

                    JSONInterval.put("priority", timeInterval.priority);
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
