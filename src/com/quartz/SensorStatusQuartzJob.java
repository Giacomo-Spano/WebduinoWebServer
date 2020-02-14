/**
 * Created by Giacomo Spanò on 14/02/2016.
 */
package com.quartz;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.HeaterActuator;
import com.server.webduino.core.sensors.SensorBase;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.logging.Logger;

import static com.server.webduino.core.sensors.HeaterActuator.STATUS_KEEPTEMPERATURE;
import static com.server.webduino.core.sensors.HeaterActuator.STATUS_KEEPTEMPERATURE_RELEOFF;

public class SensorStatusQuartzJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(SensorStatusQuartzJob.class.getName());
    private long timeout = 0;

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        LOGGER.info("+SensorStatusQuartzJob");
        try {
            update(context);

        } catch (Exception e) {

            LOGGER.info("--- Error in job!");
            JobExecutionException e2 = new JobExecutionException(e);
            // this job will refire immediately
            e2.refireImmediately();
            throw e2;
        }
        LOGGER.info("-SensorStatusQuartzJob");
    }

    private volatile boolean flag_locked = false;
    private void update(JobExecutionContext context) {

        LOGGER.info("+SensorStatusQuartzJob:update");

        SensorBase sensor = (SensorBase) context.getMergedJobDataMap().get("sensor");
        timeout = (int)context.getMergedJobDataMap().get("timeout");
        Date last = sensor.getLastUpdate();
        boolean requestStausUpdate = false;
        if (last == null) {
            requestStausUpdate = true;
        } else {
            Date current = Core.getDate();
            long diff_seconds = (current.getTime() - last.getTime()) / 1000;
            if (diff_seconds > timeout)
                requestStausUpdate = true;
        }
        if (requestStausUpdate)
            sensor.requestAsyncSensorStatusUpdate();

        LOGGER.info("-SensorStatusQuartzJob:update");
    }
}