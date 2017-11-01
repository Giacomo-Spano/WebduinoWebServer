package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.scenario.programinstructions.ProgramAction;
import com.server.webduino.core.webduinosystem.scenario.programinstructions.ProgramActionFactory;
import org.json.JSONArray;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/10/2017.
 */
public class Scenarios {

    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());
    private static List<Scenario> scenarioList = new ArrayList<>();

    private static void readScenarios() {
        LOGGER.info("readScenarios");
        try {
            //Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            String sql;
            sql = "SELECT * FROM scenarios" + " ORDER BY priority ASC;";;
            ResultSet scenariosResultSet = stmt.executeQuery(sql);
            scenarioList = new ArrayList<>();
            while (scenariosResultSet.next()) {
                Scenario scenario = new Scenario();
                scenario.fromResulSet(conn, scenariosResultSet);
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

    public static void initScenarios() {
        scenarioList.clear();
        for(Scenario scenario :scenarioList) {
            scenario.removeListeners();
        }

        readScenarios();
        for (Scenario scenario: scenarioList) {
            scenario.init();
        }
    }

    public static JSONArray getScenariosJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for(Scenario scenario : scenarioList) {
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




}
