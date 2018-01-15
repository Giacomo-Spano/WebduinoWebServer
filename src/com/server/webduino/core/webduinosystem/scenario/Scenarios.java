package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.Core;
import com.server.webduino.core.Program;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramAction;
import org.json.JSONArray;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/10/2017.
 */
public class Scenarios {

    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());
    private static List<Scenario> scenarioList = new ArrayList<>();
    public ArrayList<NextTimeRangeAction> nextTimeRangeActions;

    private void readScenarios() {
        LOGGER.info("readScenarios");
        try {
            //Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            String sql;
            sql = "SELECT * FROM scenarios" + " ORDER BY priority ASC;";
            ;
            ResultSet scenariosResultSet = stmt.executeQuery(sql);
            scenarioList = new ArrayList<>();
            while (scenariosResultSet.next()) {
                Scenario scenario = new Scenario();
                scenario.fromResulSet(conn, scenariosResultSet);
                scenario.setActionListener(new ProgramAction.ActionListener() {

                    @Override
                    public void onStart(ProgramAction action) {
                        checkConflict(action);
                    }

                    @Override
                    public void onStop(ProgramAction action) {
                        removeConflict(action);
                    }
                });
                scenarioList.add(scenario);
            }
            scenariosResultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Conflict hasConflict(Scenario scenario, ScenarioProgram program, ScenarioProgramTimeRange timeRange, ProgramAction programAction, ProgramAction action) {

        if (scenario == null || program == null || timeRange == null || programAction == null || action == null) return null;

        // se è la stessa action non c'è conflitto
        if (action.id == programAction.id)
            return null;

        Conflict conflict = getActionConfictDataFromActionId(action.id);

        // Se il timerange finisce prima dell'inizio dell'altro oppure incomincia
        // dopo la fine dell'altro non c'è conflitto
        if (conflict.timerange.endTime.compareTo(timeRange.startTime) <=0 ||
                conflict.timerange.startTime.compareTo(timeRange.endTime) >= 0)
            return null;

        if (conflict.scenario.id == scenario.id) {
            // stesso scenario
            if (conflict.program.id == program.id) {
                // stesso programma
                if (conflict.timerange.id == timeRange.id) {
                    // stesso timerange
                    if (conflict.action.id < action.id) {
                        return conflict;
                    }
                } else {
                    // programma diverso dello stesso scenario
                    if (conflict.timerange.index < timeRange.index ||
                            (conflict.timerange.index == timeRange.index && conflict.timerange.id < timeRange.id)) {
                        return conflict;
                    }
                }
            } else {
                // programma diverso dello stesso scenario
                if (conflict.program.priority < program.priority ||
                        (conflict.program.priority == program.priority && conflict.program.id < program.id)) {
                    return conflict;
                }
            }

        } else {
            // scenario diverso
            if (conflict.scenario.priority < scenario.priority ||
                    (conflict.scenario.priority == scenario.priority && conflict.scenario.id < scenario.id)) {
                return conflict;
            }
        }

        return null;
    }

    private void checkConflict(ProgramAction action) {
        // questa funzione è chiamata tutte le volte che una action si avvia

        // Scorre tutte le action di tutti gli scenari e se ne trova una con priorità inferiore aggiunge un conflic alla action
        if (scenarioList != null) {
            for (Scenario scenario : scenarioList) {
                if (scenario.programs != null)
                    for (ScenarioProgram program : scenario.programs) {
                        if (program.timeRanges != null)
                            for (ScenarioProgramTimeRange timeRange : program.timeRanges) {
                                if (timeRange.programActionList != null)
                                    for (ProgramAction programAction : timeRange.programActionList) {
                                        Conflict conflict = hasConflict(scenario, program, timeRange, programAction, action);
                                        if (conflict != null) {
                                            if (programAction.hasConflict(action)) {
                                                programAction.addConflict(conflict);
                                            }
                                        }
                                    }
                            }
                    }
            }
        }
    }

    private void checkNextConflict() {
        // controlla se ci sono conflitti tra tutte le next action

        if (nextTimeRangeActions == null) return;
        // Scorre tutte le action in un doppio loop e se ne trova una con priorità inferiore aggiunge un conflic alla action trovata
        for (NextTimeRangeAction timeRangeAction1 : nextTimeRangeActions) {
            for (NextTimeRangeAction timeRangeAction2 : nextTimeRangeActions) {
                if (!timeRangeAction1.date.equals(timeRangeAction2.date))
                    continue;
                Conflict conflict = hasConflict(timeRangeAction1.scenario, timeRangeAction1.program, timeRangeAction1.timeRange, timeRangeAction1.action, timeRangeAction2.action);
                if (conflict != null){
                    if (timeRangeAction1.action.hasConflict(timeRangeAction2.action)) {
                        timeRangeAction1.addConflict(conflict);
                    }
                }
            }
        }
    }

    private Conflict getActionConfictDataFromActionId(int actionId) {

        for (Scenario scenario : scenarioList) {
            if (scenario.programs == null) return null;
            for (ScenarioProgram program : scenario.programs) {
                if (program.timeRanges == null) return null;
                for (ScenarioProgramTimeRange timeRange : program.timeRanges) {
                    if (timeRange.programActionList == null) return null;
                    for (ProgramAction action : timeRange.programActionList) {
                        if (action.id == actionId) {
                            Conflict conflict = new Conflict(action, timeRange, program, scenario);
                            return conflict;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void removeConflict(ProgramAction action) {
        // questa funzione è chiamata tutte le volte che una action si ferma

        if (action == null) return;

        if (scenarioList != null)
            for (Scenario scenario : scenarioList) {
                if (scenario.programs != null)
                    for (ScenarioProgram program : scenario.programs) {
                        if (program.timeRanges != null)
                            for (ScenarioProgramTimeRange timeRange : program.timeRanges) {
                                if (timeRange.programActionList != null)
                                    for (ProgramAction programAction : timeRange.programActionList) {

                                        if (action.id == programAction.id) continue;

                                        if (action.actuatorid != action.actuatorid) continue;

                                        programAction.removeConflict(action);
                                    }
                            }
                    }
            }
    }

    public void initScenarios() {

        for (Scenario scenario : scenarioList) {
            scenario.stop();
        }

        scenarioList.clear();
        readScenarios();
        for (Scenario scenario : scenarioList) {
            scenario.start();
        }

        checkNextTimeRangeActions(Core.getDate());
    }

    public static JSONArray getScenariosJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (Scenario scenario : scenarioList) {
            jsonArray.put(scenario.toJson());
        }
        return jsonArray;
    }

    public static Scenario getScenarioFromId(int id) {
        for (Scenario scenario : scenarioList) {
            if (scenario.id == id) {
                return scenario;
            }
        }
        return null;
    }

    public static ScenarioProgram getScenarioProgramFromId(int id) {
        for (Scenario scenario : scenarioList) {
            for (ScenarioProgram program : scenario.programs) {
                if (program.id == id) {
                    return program;
                }
            }
        }
        return null;
    }

    public static ScenarioProgramTimeRange getScenarioProgramTimeRangeFromId(int id) {
        if (scenarioList != null) {
            for (Scenario scenario : scenarioList) {
                if (scenario.programs != null) {
                    for (ScenarioProgram program : scenario.programs) {
                        if (program.timeRanges != null) {
                            for (ScenarioProgramTimeRange timeRange : program.timeRanges) {
                                if (timeRange.id == id) {
                                    return timeRange;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public void checkNextTimeRangeActions(Date date) {

        // valuta tutte le next action a partiere dalla data 'date'
        nextTimeRangeActions = new ArrayList<>();
        for (Scenario scenario : scenarioList) {
            if (!scenario.enabled)
                continue;
            List<NextTimeRangeAction> list = scenario.getNextTimeRangeActions(date);
            if (list != null) {
                for (NextTimeRangeAction timeRangeAction : list) {
                    timeRangeAction.scenario = scenario;
                    nextTimeRangeActions.add(timeRangeAction);
                }
            }
        }

        // ordina tutte le next action per data di inizio e per data di fine
        Collections.sort(nextTimeRangeActions);
        // controlla tutti i conflitti delle next action
        checkNextConflict();
    }

    public NextTimeRangeAction getNextActuatorProgramTimeRangeAction(int actuatorid) {
        for (NextTimeRangeAction timeRangeAction:nextTimeRangeActions) {
            if (timeRangeAction.action.actuatorid == actuatorid)
                return timeRangeAction;
        }
        return null;
    }

    public List<NextTimeRangeAction> getNextActuatorProgramTimeRangeActionList(int actuatorid) {
        List<NextTimeRangeAction> list = new ArrayList<>();
        for (NextTimeRangeAction timeRangeAction:nextTimeRangeActions) {
            if (timeRangeAction.action.actuatorid == actuatorid)
                list.add(timeRangeAction);
        }
        return list;
    }
}
