package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.webduinosystem.scenario.Conflict;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepOffScenarioProgramInstruction extends ScenarioProgramInstruction {
    public KeepOffScenarioProgramInstruction(int id, int programtimerangeid, String type, String name, String description, int priority, int actuatorid, double targevalue, double thresholdvalue,
                                             int zoneId, int seconds, boolean enabled) {
        super(id, programtimerangeid, name, description, priority, enabled);
    }

    /*@Override
    public void addConflict(Conflict newconflict) {

        // controlla che non ci sia già nella lista altrimenti
        for (Conflict conflict: conflictList) {
            if (conflict.action.id == newconflict.action.id) {
                return;
            }
        }

        // se la action ha lo stesso actuator aggiunge il conflitto
        // DA RIFARE
        if (newconflict.action.sensorid == this.sensorid) {
            if (newconflict.action instanceof KeepTemperatureScenarioProgramInstruction || newconflict.action instanceof KeepOffScenarioProgramInstruction) {
                conflictList.add(newconflict);
            }
        }
    }*/
}
