package com.server.webduino.core.webduinosystem.programinstructions;

import com.server.webduino.core.webduinosystem.WebduinoTrigger;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepOffProgramInstructions extends ProgramInstructions {
    public KeepOffProgramInstructions(int id, String type, int actuatorid, double targetValue, int zoneId) {
        super(id, type, actuatorid, targetValue, zoneId,0);
    }
}
