package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.webduinosystem.WebduinoSystemScenario;
import com.server.webduino.core.webduinosystem.scenario.actions.Action;

/**
 * Created by giaco on 23/12/2017.
 */
public class Conflict {
    public Action action;
    public ScenarioProgramTimeRange timerange;
    public ScenarioProgram program;
    public WebduinoSystemScenario scenario;


    public Conflict(Action action, ScenarioProgramTimeRange timerange, ScenarioProgram program, WebduinoSystemScenario scenario) {
        this.action = action;
        this.timerange = timerange;
        this.program = program;
        this.scenario = scenario;
    }
}
