package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.webduinosystem.scenario.Conflict;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepOffProgramActions extends ProgramAction {
    public KeepOffProgramActions(int id, int programtimerangeid, String type, String name, String description, int priority, int actuatorid, double targevalue, double thresholdvalue,
                                 int zoneId, int seconds, boolean enabled) {
        super(id, programtimerangeid, type, name, description, priority, actuatorid, targevalue, thresholdvalue,
                zoneId, seconds, enabled);
    }

    @Override
    public void addConflict(Conflict newconflict) {

        // controlla che non ci sia già nella lista altrimenti
        for (Conflict conflict: conflictList) {
            if (conflict.action.id == newconflict.action.id) {
                return;
            }
        }

        // se la action ha lo stesso actuator aggiunge il conflitto
        if (newconflict.action.actuatorid == this.actuatorid) {
            if (newconflict.action instanceof KeepTemperatureProgramAction || newconflict.action instanceof KeepOffProgramActions ) {
                conflictList.add(newconflict);
            }
        }
    }
}
