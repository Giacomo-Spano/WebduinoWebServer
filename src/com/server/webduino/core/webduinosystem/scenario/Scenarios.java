package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.Core;
import com.server.webduino.core.Program;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramAction;
import org.json.JSONArray;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/10/2017.
 */
public class Scenarios {

    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());
    private static List<Scenario> scenarioList = new ArrayList<>();

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
        // se è la stessa action passa alla successiva
        if (action.id == programAction.id)
            return null;

        Conflict conflict = getActionConfictDataFromActionId(action.id);

        if (conflict.scenario.id == scenario.id) {
            if (conflict.program.id == program.id) {
                if (conflict.timerange.id == timeRange.id) {
                    if (conflict.action.id < action.id) {
                        return conflict;
                    }
                } else {
                    if (conflict.timerange.index < timeRange.index ||
                            (conflict.timerange.index == timeRange.index && conflict.timerange.id < timeRange.id)) {
                        return conflict;
                    }
                }
            } else {
                if (conflict.program.priority < program.priority ||
                        (conflict.program.priority == program.priority &&conflict.program.id < program.id)) {
                    return conflict;
                }
            }

        } else {
            if (conflict.scenario.priority < scenario.priority ||
                    (conflict.scenario.priority == scenario.priority && conflict.scenario.id < scenario.id)) {
                return conflict;
            }
        }

        return null;
    }

    private void checkConflict(ProgramAction action) {

        //Conflict conflict = getActionConfictDataFromActionId(action.id);

        // Scorre tutte le action di tutti gli scenari e se ne trova una con priorità inferiore aggiunge un conflic alla action trovata
        if (scenarioList != null)
            for (Scenario scenario : scenarioList) {
                if (scenario.programs != null)
                    for (ScenarioProgram program : scenario.programs) {
                        if (program.timeRanges != null)
                            for (ScenarioProgramTimeRange timeRange : program.timeRanges) {
                                if (timeRange.programActionList != null)
                                    for (ProgramAction programAction : timeRange.programActionList) {
                                        Conflict conflict = hasConflict(scenario,program,timeRange,programAction,action);
                                        if(conflict != null)
                                            programAction.addConflict(conflict);
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
                            Conflict conflict = new Conflict(action,timeRange,program,scenario);
                            return conflict;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static void removeConflict(ProgramAction action) {
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




    public List<NextScenario> getNextScenarios(java.util.Date date) {

        List<NextScenario> nextScenarios = new ArrayList<>();
        for (Scenario scenario : scenarioList) {
            List<NextProgram> programList = scenario.getNextPrograms(date);
            if (programList != null) {
                NextScenario nextScenario = new NextScenario();
                nextScenario.scenario = scenario;
                nextScenario.nextPrograms = programList;
                nextScenarios.add(nextScenario);
            }
        }
        return nextScenarios;
    }
}
