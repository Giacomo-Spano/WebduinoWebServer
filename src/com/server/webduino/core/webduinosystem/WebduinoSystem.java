package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.Schedule;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.keys.SecurityKey;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneFactory;
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
    private List<WebduinoSystemZone> zones = new ArrayList<>();
    private List<WebduinoSystemActuator> actuators = new ArrayList<>();

    public WebduinoSystem(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
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
        LOGGER.info(" readWebduinoSystems");

        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM webduino_system_zones WHERE webduinosystemid=" + webduinosystemid;
        ResultSet rs = stmt.executeQuery(sql);
        // Extract data from result set
        zones = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            int zoneid = rs.getInt("zoneid");
            Zone zone = Core.getZoneFromId(zoneid);
            WebduinoSystemZone webduinosystemzone = new WebduinoSystemZone(id,name,zone,type);
            zones.add(webduinosystemzone);
        }
        rs.close();
        stmt.close();
    }

    public void readWebduinoSystemsActuators(Connection conn, int webduinosystemid) throws SQLException {
        LOGGER.info(" readWebduinoSystems");

        Statement stmt = conn.createStatement();
        String sql = "SELECT * FROM webduino_system_actuators WHERE webduinosystemid=" + webduinosystemid;
        ResultSet rs = stmt.executeQuery(sql);
        // Extract data from result set
        actuators = new ArrayList<>();
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            int actuatorid = rs.getInt("sensorid");
            SensorBase actuator = Core.getSensorFromId(actuatorid);
            WebduinoSystemActuator webduinosystemactuator = new WebduinoSystemActuator(id, name, actuator);
            actuators.add(webduinosystemactuator);
        }
        rs.close();
        stmt.close();
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

        return json;
    }

}
