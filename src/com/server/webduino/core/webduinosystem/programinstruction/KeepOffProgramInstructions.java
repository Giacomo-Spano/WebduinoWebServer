package com.server.webduino.core.webduinosystem.programinstruction;

import com.server.webduino.core.WebduinoTrigger;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepOffProgramInstructions extends ProgramInstructions {
    public KeepOffProgramInstructions(int id, String type, int actuatorid, double targetValue, int zoneId) {
        super(id, type, actuatorid, targetValue, zoneId);
    }

    @Override
    public void onTrigger(WebduinoTrigger trigger) {
    }
}
