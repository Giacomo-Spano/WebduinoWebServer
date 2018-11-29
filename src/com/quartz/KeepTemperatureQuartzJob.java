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
import java.util.Date;
import java.util.logging.Logger;

import static com.server.webduino.core.sensors.HeaterActuator.STATUS_KEEPTEMPERATURE;

public class KeepTemperatureQuartzJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(KeepTemperatureQuartzJob.class.getName());

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        LOGGER.info("+KeepTemperatureQuartzJob");
        try {
            update(context);

        } catch (Exception e) {

            LOGGER.info("--- Error in job!");
            JobExecutionException e2 = new JobExecutionException(e);
            // this job will refire immediately
            e2.refireImmediately();
            throw e2;
        }
        LOGGER.info("-KeepTemperatureQuartzJob");
    }

    private volatile boolean flag_locked = false;
    private void update(JobExecutionContext context) {

        LOGGER.info("+KeepTemperatureQuartzJob:update");
        HeaterActuator heater = (HeaterActuator) context.getMergedJobDataMap().get("heater");
        Date endtime = (Date) context.getMergedJobDataMap().get("endtime");
        double target = (double)context.getMergedJobDataMap().get("target");
        int commandremotesensorid = (int)context.getMergedJobDataMap().get("commandremotesensorid");
        int actionid = (int)context.getMergedJobDataMap().get("actionid");

        // il thread viene chiamato poeriodicamente (ogni minuto) e
        // se lo statoo non è keeptemperature (per esempio perchè il sensore è ripartito)
        // manda nuovamente il comando
        System.out.println("CommandThread keeptemperature timer Executed... " + Core.getDate().toString());
        //double remotetemp = getRemoteSensorTemperature();
        if (heater.getStatus().status.equals(STATUS_KEEPTEMPERATURE) && heater.getTemperature() <= 0) {
            System.out.println("error: remote temperatur = " + heater.getTemperature());
            boolean res = heater.sendStopKeepTemperature();
            return;
        }
        System.out.println("status = " + heater.getStatus().status);
        if (!heater.getStatus().status.equals(STATUS_KEEPTEMPERATURE)) {

            long diffInMillies = Math.abs(endtime.getTime() - Core.getDate().getTime());
            long duration = diffInMillies / 1000;
            boolean res = heater.sendKeepTemperature(target, duration, commandremotesensorid,actionid);
            if (res)
                System.out.println("sendKeepTemperature sent");
            else
                System.out.println("sendKeepTemperature failed");
        }
        LOGGER.info("-KeepTemperatureQuartzJob:update");
    }
}