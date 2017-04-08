package com.quartz;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.SimpleScheduleBuilder.*;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.server.webduino.core.Core;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;

import java.util.Date;
import java.util.logging.Logger;

import javax.servlet.ServletContext;


public class QuartzListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(QuartzListener.class.getName());
    public static final String CoreClass = "core";

    Scheduler scheduler = null;

    static Core core;// = new Core();

    @Override
    public void contextInitialized(ServletContextEvent servletContext) {

        core = new Core();
        core.init();

        ServletContext cntxt = servletContext.getServletContext();
        cntxt.setAttribute(CoreClass, core);


        System.out.println("Context Initialized");

        try {
            scheduler = new StdSchedulerFactory().getScheduler();
            scheduler.start();

            //pass the servlet context to the job
            JobDataMap jobDataMap = new JobDataMap();
            jobDataMap.put("servletContext", servletContext.getServletContext());

            // Job di interrogazione periodica
            // define the job and tie it to our job's class
            JobDetail sensorJob = newJob(ShieldsQuartzJob.class).withIdentity(
                    "CronSensorQuartzJob", "Group")
                    .usingJobData(jobDataMap)
                    .build();
            // Trigger the job to run now, and then every 40 seconds
            Trigger sensorTrigger = newTrigger()
                    .withIdentity("SensorTriggerName", "Group")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(60*15)   // interroga ogni 15 minuti
                            .repeatForever())
                    .build();
            // Setup the Job and Trigger with Scheduler & schedule jobs
            scheduler.scheduleJob(sensorJob, sensorTrigger);

            // Job di controllo periodico del programma attivo
            // questo job non parte subito ma 10 secondi dopo quello di interrogazione
            // per avere tempo di ricevere la risposta
            // Setup the Job class and the Job group
            /*JobDetail programJob = newJob(ProgramQuartzJob.class).withIdentity(
                    "CronProgramQuartzJob", "Group")
                    .usingJobData(jobDataMap)
                    .build();
            // Trigger the job to run now, and then every 40 seconds
            Trigger trigger = newTrigger()
                    .withIdentity("ProgramTriggerName", "Group")
                    //.startNow()
                    .startAt(new Date(System.currentTimeMillis() + 15000))
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(60)
                            .repeatForever())
                    .build();*/


        }
        catch (SchedulerException e) {
            LOGGER.info("QuartzListener exception" + e.getStackTrace());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContext) {
        System.out.println("Context Destroyed");
        try
        {
            scheduler.shutdown();
        }
        catch (SchedulerException e)
        {
            LOGGER.info("execute" + e.getStackTrace());
            e.printStackTrace();
        }
    }
}
