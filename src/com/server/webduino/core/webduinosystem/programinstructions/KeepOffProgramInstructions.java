package com.server.webduino.core.webduinosystem.programinstructions;

import com.server.webduino.core.webduinosystem.WebduinoTrigger;

import java.util.Date;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepOffProgramInstructions extends ProgramInstructions {
    public KeepOffProgramInstructions(int id, int scenarioid, String name, String type, int actuatorid, double targetValue, int zoneId, int seconds, boolean schedule, Date startTime, Date endTime,
                                      boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, int priority) {
        super(id, scenarioid, name, type, actuatorid, targetValue, zoneId,0, schedule, startTime, endTime,
                sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority);
    }
}
