package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.webduinosystem.scenario.Scenario;
import com.server.webduino.core.webduinosystem.services.Service;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.keys.SecurityKey;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystem {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private List<SecurityKey> keys = new ArrayList<>();
    private int id;
    private String name;
    private String type;
    public List<WebduinoSystemZone> zones = new ArrayList<>();
    public List<WebduinoSystemActuator> actuators = new ArrayList<>();
    public List<WebduinoSystemService> services = new ArrayList<>();
    public List<Scenario> scenarios = new ArrayList<>();

    public WebduinoSystem(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void init(int system) {
    }

    public void readWebduinoSystemsZones(Connection conn, int webduinosystemid) throws SQLException {
        LOGGER.info(" readWebduinoSystemZoness");

        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM webduino_system_zones WHERE webduinosystemid=" + webduinosystemid;
        ResultSet rs = stmt.executeQuery(sql);
        // Extract data from result set
        zones = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            int zoneid = rs.getInt("zoneid");
            WebduinoSystemZone webduinosystemzone = new WebduinoSystemZone(id,zoneid,webduinosystemid);
            zones.add(webduinosystemzone);
        }
        rs.close();
        stmt.close();
    }

    public void readWebduinoSystemsActuators(Connection conn, int webduinosystemid) throws SQLException {
        LOGGER.info(" readWebduinoSystemActuatorss");

        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM webduino_system_actuators WHERE webduinosystemid=" + webduinosystemid;
        ResultSet rs = stmt.executeQuery(sql);
        // Extract data from result set
        actuators = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            int actuatorid = rs.getInt("sensorid");
            WebduinoSystemActuator webduinosystemactuator = new WebduinoSystemActuator(id, actuatorid, webduinosystemid);
            actuators.add(webduinosystemactuator);
        }
        rs.close();
        stmt.close();
    }

    public void readWebduinoSystemsServices(Connection conn, int webduinosystemid) throws SQLException {
        LOGGER.info(" readWebduinoSystemServices");

        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM webduino_system_services WHERE webduinosystemid=" + webduinosystemid;
        ResultSet rs = stmt.executeQuery(sql);
        // Extract data from result set
        services = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            int serviceid = rs.getInt("serviceid");
            WebduinoSystemService webduinosystemservice = new WebduinoSystemService(id, serviceid, webduinosystemid);
            services.add(webduinosystemservice);
        }
        rs.close();
        stmt.close();
    }

    /*public void readWebduinoSystemsScenarios(Connection conn, int webduinosystemid) throws SQLException {
        LOGGER.info(" readWebduinoSystemServices");

        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM scenarios WHERE webduinosystemid=" + webduinosystemid;
        ResultSet rs = stmt.executeQuery(sql);
        // Extract data from result set
        scenarios = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            int scenarioid = rs.getInt("scenarioid");
            WebduinoSystemScenario webduinosystemsscenario = new WebduinoSystemScenario(id, scenarioid);
            scenarios.add(webduinosystemsscenario);
        }
        rs.close();
        stmt.close();
    }*/

    public void readWebduinoSystemsScenarios(Connection conn, int webduinosystemid) throws SQLException {
        LOGGER.info("readWebduinoSystemsScenarios");
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String sql = "SELECT * FROM scenarios WHERE webduinosystemid=" + webduinosystemid + " ORDER BY priority ASC;";
            ;
            ResultSet scenariosResultSet = stmt.executeQuery(sql);
            scenarios = new ArrayList<>();
            while (scenariosResultSet.next()) {
                Scenario scenario = new Scenario();
                scenario.fromResulSet(conn, scenariosResultSet);
                scenarios.add(scenario);
            }
            scenariosResultSet.close();
            stmt.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("type", type);

        JSONArray zonearray = new JSONArray();
        for (WebduinoSystemZone zone : zones) {
            JSONObject j = zone.toJson();
            zonearray.put(j);
        }
        json.put("zones", zonearray);

        JSONArray actuatorarray = new JSONArray();
        for (WebduinoSystemActuator actuator : actuators) {
            JSONObject j = actuator.toJson();
            actuatorarray.put(j);
        }
        json.put("actuators", actuatorarray);

        JSONArray servicearray = new JSONArray();
        for (WebduinoSystemService service : services) {
            JSONObject j = service.toJson();
            servicearray.put(j);
        }
        json.put("services", servicearray);

        JSONArray scenarioarray = new JSONArray();
        for (Scenario scenario : scenarios) {
            JSONObject j = scenario.toJson();
            scenarioarray.put(j);
        }
        json.put("scenarios", scenarioarray);

        return json;
    }

}
