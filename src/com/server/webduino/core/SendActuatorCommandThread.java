package com.server.webduino.core;

import com.server.webduino.core.sensors.Actuator;
import com.server.webduino.core.sensors.commands.ActuatorCommand;

import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 15/02/2016.
 */
public class SendActuatorCommandThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(SendActuatorCommandThread.class.getName());

    private ActuatorCommand command;
    private int actuatorId;
    private String subaddress;

    public SendActuatorCommandThread(int actuatorId, ActuatorCommand command) {
        super("str");

        this.command = command;
        this.actuatorId = actuatorId;
        //this.subaddress = subaddress;
    }
    public void run() {

        LOGGER.info("SendActuatorCommandThread ");

        Actuator actuator = (Actuator) Core.getSensorFromId(actuatorId);
        boolean res;
        if (actuator != null)
            res = actuator.sendCommand(command);
    }
}

