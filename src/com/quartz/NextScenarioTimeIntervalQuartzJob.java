/**
 * Created by Giacomo Spanò on 14/02/2016.
 */
package com.quartz;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.scenario.Scenario;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.logging.Logger;

public class NextScenarioTimeIntervalQuartzJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(NextScenarioTimeIntervalQuartzJob.class.getName());

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        try {
            LOGGER.info("NextScenarioTimeIntervalQuartzJob START");
            Date date = Core.getDate();
            LOGGER.info("" + date.toString() + " NextScenarioTimeIntervalQuartzJob");

            Scenario scenario = (Scenario) context.getMergedJobDataMap().get("scenario");
            scenario.triggerNextTimeInterval();


            /*ServletContext servletContext = (ServletContext) context.getMergedJobDataMap().get("servletContext");
            Core core = (Core)servletContext.getAttribute(QuartzListener.CoreClass);
            core.mSchedule.checkProgram();*/

        } catch (Exception e) {

            //LOGGER.info("execute" + e.getStackTrace());
            //e.printStackTrace();

            LOGGER.info("--- Error in job!");
            JobExecutionException e2 =
                    new JobExecutionException(e);
            // this job will refire immediately
            e2.refireImmediately();
            throw e2;
        }
        LOGGER.info("NextScenarioTimeIntervalQuartzJob END");

    }
}