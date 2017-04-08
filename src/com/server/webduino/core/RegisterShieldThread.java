package com.server.webduino.core;

import com.quartz.QuartzListener;

import javax.servlet.ServletContext;
import java.net.URL;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 15/02/2016.
 */
public class RegisterShieldThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(RegisterShieldThread.class.getName());

    private Shield shield;
    public RegisterShieldThread(Shield shield) {
        super("str");

        this.shield = shield;
    }

    public void run() {

        /*LOGGER.info("UpdateSensorsThread -START");

        Shields shields = new Shields();
        shields.register(shield);

        LOGGER.info("UpdateSensorsThread - END");*/
    }
}

