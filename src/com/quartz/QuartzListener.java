package com.quartz;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.SimpleScheduleBuilder.*;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import com.server.webduino.core.Core;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.util.logging.Logger;
import javax.servlet.ServletContext;

import javax.servlet.annotation.WebListener;

@WebListener
public class QuartzListener implements ServletContextListener {

    private static final Logger LOGGER = Logger.getLogger(QuartzListener.class.getName());
    public static final String CoreClass = "core";

    Scheduler scheduler = null;

    private Core core;// = new Core();

    @Override
    public void contextInitialized(ServletContextEvent servletContext) {

        core = new Core();

        core.initMQTT();

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
            // WebduinoTrigger the job to run now, and then every 40 seconds
            Trigger sensorTrigger = newTrigger()
                    .withIdentity("SensorTriggerName", "Group")
                    .startNow()
                    .withSchedule(simpleSchedule()
                            .withIntervalInSeconds(5*60)   // interroga ogni 1 minuti
                            .repeatForever())
                    .build();
            // Setup the Job and WebduinoTrigger with Scheduler & schedule jobs
            scheduler.scheduleJob(sensorJob, sensorTrigger);


        }
        catch (SchedulerException e) {
            LOGGER.info("QuartzListener exception" + e.getStackTrace());
            e.printStackTrace();
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContext) {
        System.out.println("Context Destroyed");

        core.mqttDisconnect();
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
