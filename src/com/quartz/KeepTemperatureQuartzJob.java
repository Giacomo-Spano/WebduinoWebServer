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
import java.util.logging.Logger;

public class KeepTemperatureQuartzJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(KeepTemperatureQuartzJob.class.getName());

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        try {
            LOGGER.info("ShieldsQuartzJob START");
            update(context);

        } catch (Exception e) {

            LOGGER.info("--- Error in job!");
            JobExecutionException e2 =
                    new JobExecutionException(e);
            // this job will refire immediately
            e2.refireImmediately();
            throw e2;
        }
        LOGGER.info("ShieldsQuartzJob END");
    }

    private volatile boolean flag_locked = false;
    private void update(JobExecutionContext context) {

        HeaterActuator heater = (HeaterActuator) context.getMergedJobDataMap().get("heater");

        //core.mShields.requestSensorsStatusUpdate();
        /*MyThread commandThread = new MyThread();
        Thread thread = new Thread(commandThread, "commandThread");
        thread.start();*/


        //core.mShields.requestSensorsStatusUpdate();

        LOGGER.info("ShieldsQuartzJob:update  end");
    }


    class MyThread implements Runnable {

        private volatile boolean execute; // variabile di sincronizzazione
        public MyThread() {
        }

        @Override
        public void run() {
            Thread t = Thread.currentThread();
            System.out.println("Thread started: " + t.getName());
        }
    }
}