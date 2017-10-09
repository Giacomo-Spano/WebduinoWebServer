package com.server.webduino.core.webduinosystem.scenario.programinstructions;

import java.util.Date;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepOffProgramInstructions extends ProgramInstructions {
    public KeepOffProgramInstructions(int id, int programtimerangeid, String name, String type, int actuatorid, double targetValue, int zoneId, int seconds, boolean schedule,
                                      boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, int priority) {
        super(id, programtimerangeid, name, type, actuatorid, targetValue, zoneId,0, schedule,
                sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority);
    }
}
