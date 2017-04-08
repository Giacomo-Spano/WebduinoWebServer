package com.server.webduino.core;

import com.quartz.QuartzListener;
import org.json.JSONArray;

import javax.servlet.ServletContext;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 15/02/2016.
 */
public class UpdateSensorsThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(UpdateSensorsThread.class.getName());
    private final JSONArray jsonArray;

    private int shieldid;
    private String subaddress;
    Date lastupdate;
    private ServletContext context;

    public UpdateSensorsThread(ServletContext context, int shieldid, Date lastupdate, JSONArray jsonArray) {
        super("str");

        this.context = context;
        this.shieldid = shieldid;
        //this.subaddress = subaddress;
        this.lastupdate = lastupdate;
        this.jsonArray = jsonArray;
    }

    public void run() {

        LOGGER.info("UpdateSensorsThread -START");
        Core core = (Core)context.getAttribute(QuartzListener.CoreClass);
        core.updateSensors(shieldid,jsonArray);
        //SensorBase sensor = core.getSensorFromShieldIdAndSubadress(shieldid,subaddress);
        //sensor.updateFromJson(json);
        LOGGER.info("UpdateSensorsThread - END");
    }
}

