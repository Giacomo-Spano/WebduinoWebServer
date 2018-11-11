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

import javax.servlet.ServletContext;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class KeepTemperatureQuartzJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(KeepTemperatureQuartzJob.class.getName());

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        try {
            LOGGER.info("KeepTemperatureQuartzJob START");
            update(context);

        } catch (Exception e) {

            LOGGER.info("--- Error in job!");
            JobExecutionException e2 = new JobExecutionException(e);
            // this job will refire immediately
            e2.refireImmediately();
            throw e2;
        }
        LOGGER.info("KeepTemperatureQuartzJob END");
    }

    private volatile boolean flag_locked = false;
    private void update(JobExecutionContext context) {

        /*HeaterActuator heater = (HeaterActuator) context.getMergedJobDataMap().get("heater");
        heater.executeJob();*/
        Method method = (Method) context.getMergedJobDataMap().get("method");
        Object object = new Object();
        String message = "messgae";
        Object[] parameters = new Object[1];
        parameters[0] = message;
        try {
            method.invoke(object, parameters);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        //core.mShields.requestSensorsStatusUpdate();

        LOGGER.info("ShieldsQuartzJob:update  end");
    }
}