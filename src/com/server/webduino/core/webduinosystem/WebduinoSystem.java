package com.server.webduino.core.webduinosystem;

import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.webduinosystem.keys.SecurityKey;
import com.server.webduino.core.webduinosystem.scenario.actions.Action;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.server.webduino.core.webduinosystem.Status.*;

//import static com.server.webduino.core.webduinosystem.Status.STATUS_OFFLINE;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystem extends DBObject {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private List<SecurityKey> keys = new ArrayList<>();
    protected List<ActionCommand> actionCommandList = new ArrayList<>();
    protected List<Status> statusList = new ArrayList<>();
    public Status status;
    Status status_enabled , status_disabled;

    public int id;
    private String name;
    private String type;
    private boolean enabled;
    public List<WebduinoSystemZone> zones = new ArrayList<>();
    public List<WebduinoSystemActuator> actuators = new ArrayList<>();
    public List<WebduinoSystemService> services = new ArrayList<>();
    public List<WebduinoSystemScenario> scenarios = new ArrayList<>();

    public Boolean endCommand() {
        return false;
    }

    /*private class Status {
        String status;
        String description;
        public Status(String status, String description) {
            this.status = status;
            this.description = description;
        }
    }*/

    public WebduinoSystem(int id, String name, String type, boolean enabled) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.enabled = enabled;
        initCommandList();
        createStatusList();
    }

    public WebduinoSystem(JSONObject json) throws Exception {
        fromJson(json);
        initCommandList();
        createStatusList();
    }

    protected void createStatusList() {
        status_enabled = new Status(STATUS_ENABLED,STATUS_DESCRIPTION_ENABLED);
        statusList.add(status_enabled);
        status_disabled = new Status(STATUS_DISABLED,STATUS_DESCRIPTION_DISABLED);
        statusList.add(status_disabled);

        this.status = status_enabled;
    }

    public Action getActionFromId(int id) {
        for (WebduinoSystemScenario scenario : scenarios) {
            Action action = scenario.getActionFromId(id);
            if (action != null)
                return action;
        }
        return null;
    }

    public JSONArray getStatusListJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (Status status : statusList) {
            jsonArray.put(status.status);
        }
        return jsonArray;
    }

    public JSONArray getActionCommandListJSONArray() throws JSONException {
        JSONArray jsonArray = new JSONArray();
        for (ActionCommand command : actionCommandList) {
            jsonArray.put(command.toJson());
        }
        return jsonArray;
    }

    protected void initCommandList() {
        ActionCommand cmd = new ActionCommand(ActionCommand.ACTIONCOMMAND_ENABLE, ActionCommand.ACTIONCOMMAND_ENABLE_DESCRIPTION);
        cmd.addStatus("Stato");
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    setStatus(status_enabled);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            @Override
            public void end() {

            }
        });
        actionCommandList.add(cmd);

        cmd = new ActionCommand(ActionCommand.ACTIONCOMMAND_DISABLE, ActionCommand.ACTIONCOMMAND_DISABLE_DESCRIPTION);
        cmd.addStatus("Stato");
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    setStatus(status_disabled);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            @Override
            public void end() {

            }
        });
        actionCommandList.add(cmd);

        cmd = new ActionCommand(ActionCommand.ACTIONCOMMAND_PAUSE, ActionCommand.ACTIONCOMMAND_PAUSE_DESCRIPTION);
        cmd.addStatus("Stato");
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    //setStatus(status_disabled);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
            @Override
            public void end() {

            }
        });
        actionCommandList.add(cmd);
    }

    public List<WebduinoSystemScenario> getScenarios() {
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
            //String name = rs.getString("name");
            WebduinoSystemZone webduinosystemzone = new WebduinoSystemZone(id, zoneid, webduinosystemid/*, name*/);
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
            String name = rs.getString("name");
            WebduinoSystemActuator webduinosystemactuator = new WebduinoSystemActuator(id, actuatorid, webduinosystemid, name);
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

    public void readWebduinoSystemsScenarios(Connection conn, int webduinosystemid) throws SQLException {
        LOGGER.info("readWebduinoSystemsScenarios");
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            String sql = "SELECT * FROM scenarios WHERE webduinosystemid=" + webduinosystemid + " ORDER BY priority ASC;";
            ;
            ResultSet scenariosResultSet = stmt.executeQuery(sql);
            scenarios = new ArrayList<>();
            while (scenariosResultSet.next()) {
                WebduinoSystemScenario scenario = new WebduinoSystemScenario();
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
        json.put("enabled", enabled);

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
        for (WebduinoSystemScenario scenario : scenarios) {
            JSONObject j = scenario.toJson();
            scenarioarray.put(j);
        }
        json.put("scenarios", scenarioarray);

        json.put("actioncommandlist", getActionCommandListJSONArray());
        json.put("statuslist", getStatusListJSONArray());

        json.put("status", status.toJson());

        return json;
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM webduino_systems WHERE id=" + id;
        stmt.executeUpdate(sql);
    }

    @Override
    public void write(Connection conn) throws SQLException {

        String sql = "INSERT INTO webduino_systems (id, type, name, enabled)" +
                " VALUES ("
                + id + ","
                + "\"" + type + "\","
                + "\"" + name + "\","
                + Core.boolToString(enabled)
                + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "type=\"" + type + "\","
                + "name=\"" + name + "\","
                + "enabled=" + Core.boolToString(enabled) + ";";

        Statement stmt = conn.createStatement();
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
        ResultSet rs = stmt.getGeneratedKeys();
        if (rs.next()) {
            id = rs.getInt(1);
        }

        for (WebduinoSystemActuator actuator : actuators) {
            if (actuator.webduinosystemid == 0) actuator.webduinosystemid = id;
            actuator.write(conn);
        }

        for (WebduinoSystemZone actuator : zones) {
            if (actuator.webduinosystemid == 0) actuator.webduinosystemid = id;
            actuator.write(conn);
        }

        for (WebduinoSystemService service : services) {
            if (service.webduinosystemid == 0) service.webduinosystemid = id;
            service.write(conn);
        }

        for (WebduinoSystemScenario scenario : scenarios) {
            if (scenario.webduinosystemid == 0) scenario.webduinosystemid = id;
            scenario.write(conn);
        }
    }

    public void fromJson(JSONObject json) throws Exception {

        if (json.has("id"))
            id = json.getInt("id");
        if (json.has("type"))
            type = json.getString("type");
        if (json.has("name"))
            name = json.getString("name");
        if (json.has("enabled"))
            enabled = json.getBoolean("enabled");

        if (json.has("actuators")) {
            JSONArray jArray = json.getJSONArray("actuators");
            for (int k = 0; k < jArray.length(); k++) {
                WebduinoSystemActuator actuator = new WebduinoSystemActuator(jArray.getJSONObject(k));
                actuators.add(actuator);
            }
        }
        if (json.has("zones")) {
            JSONArray jArray = json.getJSONArray("zones");
            for (int k = 0; k < jArray.length(); k++) {
                WebduinoSystemZone zone = new WebduinoSystemZone(jArray.getJSONObject(k));
                zones.add(zone);
            }
        }
        if (json.has("services")) {
            JSONArray jArray = json.getJSONArray("services");
            for (int k = 0; k < jArray.length(); k++) {
                WebduinoSystemService service = new WebduinoSystemService(jArray.getJSONObject(k));
                services.add(service);
            }
        }
        if (json.has("scenarios")) {
            JSONArray jArray = json.getJSONArray("scenarios");
            for (int k = 0; k < jArray.length(); k++) {
                WebduinoSystemScenario scenario = new WebduinoSystemScenario(jArray.getJSONObject(k));
                scenarios.add(scenario);
            }
        }
    }

    public ActionCommand.Command sendCommand(JSONObject json) {
        for (ActionCommand actionCommand : actionCommandList) {
            String cmd = null;
            try {
                cmd = json.getString("command");
                if (cmd.equals(actionCommand.command)) {
                    actionCommand.commandMethod.execute(json);
                    return actionCommand.commandMethod;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Status getStatus() {
        return status;
    }

    public boolean setStatus(Status status) throws Exception {
        for (Status webduinosystemstatus: statusList) {
            if (webduinosystemstatus.status.equals(status.status)) {
                this.status = status;
                return true;
            }
        }
        return false;
    }


}
