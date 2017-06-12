package com.server.webduino.core.webduinosystem.programinstructions;

import java.util.Date;

/**
 * Created by giaco on 12/05/2017.
 */
public class ProgramInstructionsFactory {

    public ProgramInstructions createProgramInstructions(int id, int scenarioid, String name, String type, int actuatorid, double targetValue, int zoneId, int seconds, boolean schedule, Date startTime, Date endTime,
                                                         boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, int priority) {
        ProgramInstructions programInstructions = null;
        if (type.equals("delayalarm")) {
            programInstructions = new DelayAlarmProgramInstructions(id,scenarioid,name,type,actuatorid,targetValue,zoneId,seconds, schedule, startTime, endTime,
                    sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority);
        } else if (type.equals("keeptemperature")) {
            programInstructions = new KeepTemperatureProgramInstructions(id,scenarioid,name,type,actuatorid,targetValue,zoneId, seconds, schedule, startTime, endTime,
                    sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority);
        } else if (type.equals("keepoff")) {
            programInstructions = new KeepOffProgramInstructions(id,scenarioid,name,type,actuatorid,targetValue,zoneId, seconds, schedule, startTime, endTime,
                    sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority);
        }
        if (programInstructions != null) {
            programInstructions.init();
        }
        return programInstructions;
    }
}
