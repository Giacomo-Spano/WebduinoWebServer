package com.server.webduino.core.webduinosystem;

import com.quartz.NextTimeIntervalQuartzJob;
import com.server.webduino.core.Core;
import com.server.webduino.core.Program;
import com.server.webduino.core.TimeRange;
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


        Date currentDate = Core.getDate();
        activeTimeInterval = calendar.getActiveTimeIntervalFromDateTime(currentDate);
        if (activeTimeInterval != null) {
            Date endDate = activeTimeInterval.endTime;

            Calendar cal = Calendar.getInstance();
            cal.setTime(currentDate);
            cal.add(Calendar.SECOND, 3); //minus number would decrement the days
            endDate = cal.getTime();


            scheduleJob(endDate);
        }
    }

    private void scheduleJob(Date date) {

        try {
            if (nextTimeIntervalJob != null)
                scheduler.deleteJob(nextTimeIntervalJob.getKey());
        } catch (SchedulerException e) {
            e.printStackTrace();
        }

        //pass the servlet context to the job
        JobDataMap jobDataMap = new JobDataMap();
        jobDataMap.put("timeintervals", this);
        // define the job and tie it to our job's class
        nextTimeIntervalJob = newJob(NextTimeIntervalQuartzJob.class).withIdentity(
                "CronNextTimeIntervalQuartzJob", "Group")
                .usingJobData(jobDataMap)
                .build();

        Trigger trigger = newTrigger()
                .withIdentity("NextTimeIntervalTriggerName", "Group")
                .startAt(date)
                .build();

        Date dd;
        try {
            dd = scheduler.scheduleJob(nextTimeIntervalJob, trigger);
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
            json.put("id",id);
            json.put("name",name);
            json.put("active",active);
            json.put("calendar",calendar.toJson());
            json.put("priority",priority);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;

    }

    public class ScenarioCalendar {
        public boolean dateEnabled;
        private boolean sunday;
        private boolean monday;
        private boolean tuesday;
        private boolean wednesday;
        private boolean thursday;
        private boolean friday;
        private boolean saturday;
        public Date startDate;
        public Date endDate;
        public Time startTime;
        public Time endTime;
        public ArrayList<ScenarioTimeInterval> timeIntervals = new ArrayList<>();

        private int tpriority;

        public ScenarioTimeInterval getActiveTimeIntervalFromDateTime(Date currentDate) {

            DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String timeStr = timeFormat.format(currentDate);
            Time currentTime = Time.valueOf(timeStr);

            Date currentDay = removeTime(currentDate);

            if (dateEnabled) {
                if (currentDay.after(endDate))
                    return null;

                if (currentDay.equals(endDate) && currentTime.after(endTime))
                    return null;

                if (currentDay.before(startDate))
                    return null;

                if (currentDay.equals(startDate) && currentTime.before(startTime))
                    return null;
            }

            Calendar c = Calendar.getInstance();
            c.setTime(currentDate);
            int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

            switch (dayOfWeek) {
                case Calendar.SUNDAY:
                    if (!sunday) return null;
                    break;
                case Calendar.MONDAY:
                    if (!monday) return null;
                    break;
                case Calendar.TUESDAY:
                    if (!tuesday) return null;
                    break;
                case Calendar.WEDNESDAY:
                    if (!wednesday) return null;
                    break;
                case Calendar.THURSDAY:
                    if (!thursday) return null;
                    break;
                case Calendar.FRIDAY:
                    if (!friday) return null;
                    break;
                case Calendar.SATURDAY:
                    if (!saturday) return null;
                    break;
                default:
                    break;
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

        public void setStartTime(Time startTime) {
            this.startTime = startTime;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public void setEndTime(Time endTime) {
            this.endTime = endTime;
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

        public void settpriority(int tpriority) {
            this.tpriority = tpriority;
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
                if (startTime != null)
                    json.put("starttime", df.format(startTime));
                json.put("enddate", endDate);
                if (endTime != null)
                    json.put("endtime", df.format(endTime));
                json.put("sunday", sunday);
                json.put("monday", monday);
                json.put("tuesday", tuesday);
                json.put("wednesday", wednesday);
                json.put("thursday", thursday);
                json.put("friday", friday);
                json.put("saturday", saturday);
                json.put("priority", priority);

                JSONArray timeIntervalsJArray = new JSONArray();
                Iterator<ScenarioTimeInterval> timeiterator = this.timeIntervals.iterator();
                while (timeiterator.hasNext()) {
                    ScenarioTimeInterval tr = timeiterator.next();

                    JSONObject JSONInterval = new JSONObject();
                    JSONInterval.put("id", tr.id);
                    JSONInterval.put("name", tr.name);
                    if (tr.endTime != null)
                        JSONInterval.put("endtime", df.format(tr.endTime));
                    JSONInterval.put("priority", tr.priority);
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
