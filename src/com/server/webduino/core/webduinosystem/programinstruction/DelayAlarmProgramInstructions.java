package com.server.webduino.core.webduinosystem.programinstruction;

import com.server.webduino.core.WebduinoTrigger;

/**
 * Created by giaco on 17/05/2017.
 */
public class DelayAlarmProgramInstructions extends ProgramInstructions {
    public DelayAlarmProgramInstructions(int id, String type, int actuatorid, double targetValue, int zoneId) {
        super(id, type, actuatorid, targetValue, zoneId);
    }

    @Override
    public void onTrigger(WebduinoTrigger trigger) {
    }
}
