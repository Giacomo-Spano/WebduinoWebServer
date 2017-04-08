package com.server.webduino.core;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by giaco on 06/01/2016.
 */
public class LinearInterpolator {

    private static final Logger LOGGER = Logger.getLogger(LinearInterpolator.class.getName());

    /*public ArrayList<DataLog> getInterpolatedData(ArrayList<DataLog> data, Date xi, Date xf, Duration step) {

        ArrayList<DataLog> series = new ArrayList<DataLog>();

        // find start element of data serie
        int start = 0, end = data.size() - 1;
        while (start < end && data.get(start).getDatetime().before(xi)) {
            start++;
        }
        // find end element of data serie
        while (end > start && data.get(end).getDatetime().after(xf)) {
            end--;
        }

        int current = start;
        for (Date t = xi; t.before(xf) || t.equals(xf); t = addSecondsToDate(step.getSeconds(), t)) {

            DataLog point = new DataLog();
            SimpleDateFormat dfDate = new SimpleDateFormat("yyyyy-MM-dd");
            SimpleDateFormat dfTime = new SimpleDateFormat("hh:mm:ss");
            try {
                point.date = dfDate.parse(dfDate.format(t));
                point.time = dfTime.parse(dfTime.format(t));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (current > data.size() - 2) {
                break;
            }

            // find nearest left
            Date tNext = data.get(current + 1).getDatetime();
            DataLog dataLogA = data.get(current);
            while ((tNext.before(t) || tNext.equals(t)) && (current < data.size() - 2)) {
                tNext = data.get(current + 1).getDatetime();
                dataLogA = data.get(++current);
            }
            if (current > data.size() - 2) {
                break;
            }

            if (dataLogA.getDatetime().equals(t)) {
                point = dataLogA;
            } else {

                // find nearest right
                tNext = data.get(current + 1).getDatetime();
                DataLog dataLogB = dataLogA;
                while ((tNext.before(t) || tNext.equals(t)) && (current < data.size() - 2)) {
                    dataLogB = data.get(++current);
                    tNext = data.get(current + 1).getDatetime();
                }

                point = dataLogA.getInterpolatedDataLog(t, dataLogA, dataLogB);

            }
            HeaterDataLog hdl = (HeaterDataLog) point;
            LOGGER.info("point: " + point.date + " " + point.time + ", " + hdl.localTemperature
                    + ", " + hdl.remoteTemperature
                    + ", " + hdl.targetTemperature);
            series.add(point);

        }
        return series;
    }*/

    /*
*  Convenience method to add a specified number of minutes to a Date object
*  From: http://stackoverflow.com/questions/9043981/how-to-add-minutes-to-my-date
*  @param  minutes  The number of minutes to add
*  @param  beforeTime  The time that will have minutes added to it
*  @return  A date object with the specified number of minutes added to it
*/
    private static Date addSecondsToDate(long seconds, Date beforeTime) {
        final long ONE_SECOND_IN_MILLIS = 1000;//millisecs

        long curTimeInMs = beforeTime.getTime();
        Date afterAddingMins = new Date(curTimeInMs + (seconds * ONE_SECOND_IN_MILLIS));
        return afterAddingMins;
    }

}
