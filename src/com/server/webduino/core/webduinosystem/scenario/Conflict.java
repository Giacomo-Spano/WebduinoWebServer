package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.webduinosystem.scenario.actions.ProgramAction;

/**
 * Created by giaco on 23/12/2017.
 */
public class Conflict {
    public ProgramAction action;
    /*public int timerangeIndex;
    public int programPriority;
    public int scenarioPriority;*/

    public ScenarioProgramTimeRange timerange;
    public ScenarioProgram program;
    public Scenario scenario;

    /*public Conflict(ProgramAction action, int timerangeIndex, int programPriority, int scenarioPriority) {
        this.action = action;
        this.timerangeIndex = timerangeIndex;
        this.programPriority = programPriority;
        this.scenarioPriority = scenarioPriority;
    }*/

    public Conflict(ProgramAction action, ScenarioProgramTimeRange timerange, ScenarioProgram program, Scenario scenario) {
        this.action = action;
        this.timerange = timerange;
        this.program = program;
        this.scenario = scenario;
    }
}
