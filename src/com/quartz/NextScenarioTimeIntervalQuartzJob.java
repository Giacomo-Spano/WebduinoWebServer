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

            Scenario scenario = (Scenario) context.getMergedJobDataMap().get("scenario");


            LOGGER.info("NextScenarioTimeIntervalQuartzJob START id="+scenario.id);

            Date currentDate = Core.getDate();
            scenario.triggerNextTimeInterval(currentDate);

            LOGGER.info("NextScenarioTimeIntervalQuartzJob END id="+scenario.id);

        } catch (Exception e) {

            //LOGGER.info("execute" + e.getStackTrace());
            //e.printStackTrace();

            LOGGER.info("--- Error in job!" + e.toString());
            JobExecutionException e2 =
                    new JobExecutionException(e);
            // this job will refire immediately
            e2.refireImmediately();
            throw e2;
        }
    }
}