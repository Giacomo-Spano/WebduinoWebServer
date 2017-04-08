package com.server.webduino.core;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class Program {
    public int id;
    public boolean active;
    public boolean dateEnabled;
    public String name;
    public boolean Sunday;
    public boolean Monday;
    public boolean Tuesday;
    public boolean Wednesday;
    public boolean Thursday;
    public boolean Friday;
    public boolean Saturday;
    public Date startDate;
    public Date endDate;
    public Time startTime;
    public Time endTime;
    public ArrayList<TimeRange> mTimeRanges = new ArrayList<TimeRange>();
    public int priority;

    //private TimeRange mActiveTimeRange = null;

    /*public TimeRange getmActiveTimeRange() {
        TimeRange tr = new TimeRange(mActiveTimeRange);
        return tr;
    }*/
    TimeRange getTimeRangeFromId(int id) {

        Iterator<TimeRange> iterator = mTimeRanges.iterator();
        TimeRange currentTimeRange = null;
        while (iterator.hasNext()) {

            currentTimeRange = iterator.next();

            if (currentTimeRange.ID == id) {

                return currentTimeRange;
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

    public TimeRange getActiveTimeRange(Date currentDate) {

        DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
        String timeStr = timeFormat.format(currentDate);
        Time currentTime = Time.valueOf(timeStr);

        Date currentDay = removeTime(currentDate);

        if (!active)
            return null;

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
                if (!Sunday) return null;
                break;
            case Calendar.MONDAY:
                if (!Monday) return null;
                break;
            case Calendar.TUESDAY:
                if (!Tuesday) return null;
                break;
            case Calendar.WEDNESDAY:
                if (!Wednesday) return null;
                break;
            case Calendar.THURSDAY:
                if (!Thursday) return null;
                break;
            case Calendar.FRIDAY:
                if (!Friday) return null;
                break;
            case Calendar.SATURDAY:
                if (!Saturday) return null;
                break;
            default:
                break;
        }

        Iterator<TimeRange> iterator = mTimeRanges.iterator();

        TimeRange activeTimeRange = null;
        Time startTime;
        TimeRange currentTimeRange = null;
        while (iterator.hasNext()) {

            if (currentTimeRange == null) { //primo giro
                startTime = Time.valueOf("00:00:00");
            } else {
                startTime = currentTimeRange.endTime;
            }
            currentTimeRange = iterator.next();

            if (!currentTimeRange.endTime.equals(Time.valueOf("00:00:00"))) {

                if (currentTime.after(currentTimeRange.endTime) || currentTime.equals(currentTimeRange.endTime)) {
                    //startTime = currentTimeRange.endTime;
                    continue;
                }

                if (currentTime.before(startTime)) {
                    //startTime = currentTimeRange.endTime;
                    continue;
                }
            }


            activeTimeRange = currentTimeRange;
            return activeTimeRange;
        }
        return null;
    }


}
