/**
 * Created by Giacomo Spanò on 14/02/2016.
 */
package com.quartz;

import com.server.webduino.core.webduinosystem.scenario.ScenarioProgram;
import com.server.webduino.core.webduinosystem.scenario.ScenarioProgramTimeRange;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;
import java.util.logging.Logger;

public class NextScenarioProgramTimeIntervalQuartzJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(NextScenarioProgramTimeIntervalQuartzJob.class.getName());

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        try {

            ScenarioProgram program = (ScenarioProgram) context.getMergedJobDataMap().get("program");
            ScenarioProgramTimeRange activetimerange = (ScenarioProgramTimeRange) context.getMergedJobDataMap().get("timerange");

            if (activetimerange != null)
                activetimerange.stop();
            /*// ferma tutti i timerange
            if (program != null && program.timeRanges != null) {
                for (ScenarioProgramTimeRange tr : program.timeRanges) {
                    tr.stop();
                }
            }*/

            LOGGER.info("NextScenarioProgramTimeIntervalQuartzJob START id="+program.id);
            program.triggerNextProgramTimeRange();
            LOGGER.info("NextScenarioProgramTimeIntervalQuartzJob END id="+program.id);

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
    }
}