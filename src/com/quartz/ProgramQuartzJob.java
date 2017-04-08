/**
 * Created by Giacomo Spanò on 14/02/2016.
 */
package com.quartz;

import com.server.webduino.core.Core;
import org.json.JSONException;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import javax.servlet.ServletContext;
import java.util.Date;
import java.util.logging.Logger;

public class ProgramQuartzJob implements Job {

    private static final Logger LOGGER = Logger.getLogger(ProgramQuartzJob.class.getName());

    public void execute(JobExecutionContext context)
            throws JobExecutionException {

        try {
            LOGGER.info("ProgramQuartzJob START");
            Date date = Core.getDate();
            LOGGER.info("" + date.toString() + " ProgramQuartzJob");

            ServletContext servletContext = (ServletContext) context.getMergedJobDataMap().get("servletContext");
            Core core = (Core)servletContext.getAttribute(QuartzListener.CoreClass);
            //core.mPrograms.checkProgram();

        } catch (Exception e) {


            LOGGER.info("--- Error in job!");
            JobExecutionException e2 =
                    new JobExecutionException(e);
            // this job will refire immediately
            e2.refireImmediately();
            throw e2;
        }
        LOGGER.info("ProgramQuartzJob END");
    }
}