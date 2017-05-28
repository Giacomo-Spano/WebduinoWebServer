package com.server.webduino.core.webduinosystem.programinstructions;

/**
 * Created by giaco on 12/05/2017.
 */
public class ProgramInstructionsFactory {

    public ProgramInstructions createProgramInstructions(int id, String name, String type, int actuatorid, double targetValue, int zoneId, int seconds) {
        ProgramInstructions programInstructions = null;
        if (type.equals("delayalarm")) {
            programInstructions = new DelayAlarmProgramInstructions(id,name,type,actuatorid,targetValue,zoneId,seconds);
        } else if (type.equals("keeptemperature")) {
            programInstructions = new KeepTemperatureProgramInstructions(id,name,type,actuatorid,targetValue,zoneId);
        }
        if (programInstructions != null) {
            programInstructions.init();
        }
        return programInstructions;
    }
}
