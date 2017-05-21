package com.server.webduino.core.webduinosystem;
import com.server.webduino.core.webduinosystem.programinstruction.ProgramInstructions;

import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class ScenarioTimeInterval {

    public int id;
    public int scenarioId = 0;
    boolean active;
    public String name;
    //Time startTime;
    public Time endTime;
    public List<ProgramInstructions> programInstructionsList = new ArrayList<>();

    public int priority;

    public ScenarioTimeInterval() {
    }

    public boolean isActive() {
        return active;
    }

    /*public ScenarioTimeInterval(int id, String name, Time endTime, int programInstructions, boolean active) {
        this.id = id;
        this.name = name;
        //this.startTime = startTime;
        this.endTime = endTime;
        this.programInstructions = programInstructions;
        this.active = active;
    }

    public ScenarioTimeInterval(ScenarioTimeInterval tr) {
        //this.ID = tr.ID;
        this.name = tr.name;
        this.endTime = tr.endTime;
        this.programInstructions = tr.programInstructions;
        //this. = tr.sensorId;
        this.active = active;
    }*/
}
