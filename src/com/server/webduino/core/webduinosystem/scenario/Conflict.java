package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.webduinosystem.scenario.actions.Action;
import com.server.webduino.core.webduinosystem.scenario.actions.ScenarioProgramInstruction;

/**
 * Created by giaco on 23/12/2017.
 */
public class Conflict {
    public Action action;
    public ScenarioProgramInstruction programInstruction;
    public ScenarioProgramTimeRange timerange;
    public ScenarioProgram program;
    public Scenario scenario;


    public Conflict(Action action, ScenarioProgramInstruction programInstruction, ScenarioProgramTimeRange timerange, ScenarioProgram program, Scenario scenario) {
        this.action = action;
        this.programInstruction = programInstruction;
        this.timerange = timerange;
        this.program = program;
        this.scenario = scenario;
    }
}
