/**
 * Created by Giacomo Spanò on 14/02/2016.
 */
package com.quartz;

import com.server.webduino.core.Core;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.servlet.ServletContext;
import java.util.logging.Logger;

public class ShieldsQuartzJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(ShieldsQuartzJob.class.getName());

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

        LOGGER.info("ShieldsQuartzJob:update start");
        ServletContext servletContext = (ServletContext) context.getMergedJobDataMap().get("servletContext");
        Core core = (Core)servletContext.getAttribute(QuartzListener.CoreClass);
        LOGGER.info("ShieldsQuartzJob:update  start");

        core.mShields.requestActuatorsUpdate();

        LOGGER.info("ShieldsQuartzJob:update  end");
    }
}