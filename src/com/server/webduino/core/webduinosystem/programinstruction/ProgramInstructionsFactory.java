package com.server.webduino.core.webduinosystem.programinstruction;

import com.server.webduino.core.webduinosystem.WebduinoSystem;
import com.server.webduino.core.webduinosystem.security.SecuritySystem;

/**
 * Created by giaco on 12/05/2017.
 */
public class ProgramInstructionsFactory {

    public ProgramInstructions createProgramInstructions(int id, String type, int actuatorid, double targetValue, int zoneId) {
        ProgramInstructions programInstructions = null;
        if (type.equals("delayalarm")) {
            programInstructions = new DelayAlarmProgramInstructions(id,type,actuatorid,targetValue,zoneId);
        } else if (type.equals("keeptemperature")) {
            programInstructions = new KeepTemperatureProgramInstructions(id,type,actuatorid,targetValue,zoneId);
        }
        if (programInstructions != null) {
            programInstructions.init();
        }
        return programInstructions;
    }
}
