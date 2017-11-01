package com.server.webduino.core.webduinosystem.scenario.programinstructions;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepOffProgramActions extends ProgramAction {
    public KeepOffProgramActions(int id, int programtimerangeid, String type, String name, String description, int priority, int actuatorid, double targevalue, double thresholdvalue,
                                 int zoneId, int seconds, boolean enabled) {
        super(id, programtimerangeid, type, name, description, priority, actuatorid, targevalue, thresholdvalue,
                zoneId, seconds, enabled);
    }
}
