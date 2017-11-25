package com.server.webduino.core;

import com.quartz.QuartzListener;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 15/02/2016.
 */
public class UpdateShieldStatusThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(UpdateShieldStatusThread.class.getName());
    private final JSONObject json;

    private int shieldid;
    //private String subaddress;
    Date lastupdate;
    private ServletContext context;

    public UpdateShieldStatusThread(ServletContext context, int shieldid, Date lastupdate, JSONObject json) {
        super("str");

        this.context = context;
        this.shieldid = shieldid;
        this.lastupdate = lastupdate;
        this.json = json;
    }

    public void run() {

        LOGGER.info("UpdateShieldStatusThread -START");
        Core core = (Core)context.getAttribute(QuartzListener.CoreClass);
        core.updateShieldStatus(shieldid,json);
        LOGGER.info("UpdateSensorsThread - END");
    }
}

