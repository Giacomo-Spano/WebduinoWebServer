package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.WebduinoSystemScenario;
import com.server.webduino.core.webduinosystem.scenario.actions.Action;
import org.json.JSONArray;

import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/10/2017.
 */
public class Scenarios {

    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());
    public static List<WebduinoSystemScenario> scenarioList = new ArrayList<>();
    public ArrayList<NextTimeRangeAction> nextTimeRangeActions;

    /*private void readScenarios() {
        scenarioList = Core.getWebduinoSystemScenarios();

        for (WebduinoSystemScenario scenario:scenarioList) {
            scenario.setActionListener(new Action.ActionListener() {
                @Override
                public void onStart(Action action) {
                    checkConflict(action);
                }

                @Override
                public void onStop(Action action) {
                    removeConflict(action);
                }
            });
        }
    }*/
    private void _readScenarios() {
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
                WebduinoSystemScenario scenario = new WebduinoSystemScenario();
                scenario.fromResulSet(conn, scenariosResultSet);
                scenario.setActionListener(new Action.ActionListener() {

                    @Override
                    public void onStart(Action action) {
                        checkConflict(action);
                    }

                    @Override
                    public void onStop(Action action) {
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

    private Conflict hasConflict(WebduinoSystemScenario scenario, ScenarioProgram program, ScenarioProgramTimeRange timeRange,
                                 Action scenarioAction, Action action) {

        if (scenario == null || program == null || timeRange == null || scenarioAction == null)
            return null;

        // se è la stessa action non c'è conflitto
        if (scenarioAction.id == action.id)
            return null;

        Conflict conflict = getActionConfictDataFromActionId(action.id);
        if (conflict == null)
            return null;

        // Se il timerange finisce prima dell'inizio dell'altro oppure incomincia
        // dopo la fine dell'altro non c'è conflitto
        if (conflict.timerange.endTime.compareTo(timeRange.startTime) <= 0 ||
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

    public void checkConflict(Action action) {
        // questa funzione è chiamata tutte le volte che una action si avvia

        // Scorre tutte le action di tutti gli scenari e se ne trova una con priorità inferiore alla action passata come param aggiunge un conflic alla action di quello scenario
        if (scenarioList != null) {
            for (WebduinoSystemScenario scenario : scenarioList) {
                if (scenario.programs != null)
                    for (ScenarioProgram program : scenario.programs) {
                        if (program.timeRanges != null)
                            for (ScenarioProgramTimeRange timeRange : program.timeRanges) {
                                if (timeRange.actions != null)
                                        for (Action scenarioAction : timeRange.actions) {
                                            Conflict conflict = hasConflict(scenario, program, timeRange, scenarioAction, action);
                                            if (conflict != null) {
                                                if (scenarioAction.hasConflict(action)) {
                                                    scenarioAction.addConflict(conflict);
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
                Conflict conflict = hasConflict(timeRangeAction1.scenario, timeRangeAction1.program, timeRangeAction1.timeRange,
                        timeRangeAction1.action, timeRangeAction2.action);
                if (conflict != null) {
                    if (timeRangeAction1.action.hasConflict(timeRangeAction2.action)) {
                        timeRangeAction1.addConflict(conflict);
                    }
                }
            }
        }
    }

    private Conflict getActionConfictDataFromActionId(int actionId) {

        for (WebduinoSystemScenario scenario : scenarioList) {
            if (scenario.programs == null) return null;
            for (ScenarioProgram program : scenario.programs) {
                if (program.timeRanges == null) return null;
                for (ScenarioProgramTimeRange timeRange : program.timeRanges) {
                    if (timeRange.actions == null) return null;
                    for (Action action : timeRange.actions) {
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

    public static void removeConflict(Action action) {
        // questa funzione è chiamata tutte le volte che una action si ferma

        if (action == null) return;

        if (scenarioList != null)
            for (WebduinoSystemScenario scenario : scenarioList) {
                if (scenario.programs != null)
                    for (ScenarioProgram program : scenario.programs) {
                        if (program.timeRanges != null)
                            for (ScenarioProgramTimeRange timeRange : program.timeRanges) {
                                if (timeRange.actions != null)
                                    for (Action scenarioAction : timeRange.actions) {
                                        if (action.id == scenarioAction.id)
                                            continue;
                                        scenarioAction.removeConflict(action);
                                    }
                            }
                    }
            }
    }

    /*public void initScenarios() {

        for (WebduinoSystemScenario scenario : scenarioList) {
            scenario.stop();
        }

        scenarioList.clear();
        readScenarios();
        for (WebduinoSystemScenario scenario : scenarioList) {
            scenario.start();
        }

        checkNextTimeRangeActions(Core.getDate());
    }*/

    public static JSONArray getScenariosJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (WebduinoSystemScenario scenario : scenarioList) {
            jsonArray.put(scenario.toJson());
        }
        return jsonArray;
    }

    public static WebduinoSystemScenario getScenarioFromId(int id) {
        for (WebduinoSystemScenario scenario : scenarioList) {
            if (scenario.id == id) {
                return scenario;
            }
        }
        return null;
    }



    public static ScenarioProgram getScenarioProgramFromId(int id) {
        for (WebduinoSystemScenario scenario : scenarioList) {
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
            for (WebduinoSystemScenario scenario : scenarioList) {
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
        for (WebduinoSystemScenario scenario : scenarioList) {
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
        for (NextTimeRangeAction timeRangeAction : nextTimeRangeActions) {
            //DA RIFARE
            /*if (timeRangeAction.action.actuatorid == actuatorid)
                return timeRangeAction;*/
        }
        return null;
    }

    public List<NextTimeRangeAction> getNextActuatorProgramTimeRangeActionList(int actuatorid) {
        List<NextTimeRangeAction> list = new ArrayList<>();
        for (NextTimeRangeAction timeRangeAction : nextTimeRangeActions) {
            //DA RIFARE
            /*if (timeRangeAction.action.actuatorid == actuatorid)
                list.add(timeRangeAction);*/
        }
        return list;
    }
}
