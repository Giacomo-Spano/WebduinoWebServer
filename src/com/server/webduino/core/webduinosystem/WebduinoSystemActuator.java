package com.server.webduino.core.webduinosystem;

import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystemActuator extends DBObject {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private int id;
    public int sensorid;
    public int webduinosystemid;
    public String name;

    public WebduinoSystemActuator(int id, int actuatorid, int webduinosystemid, String name) {
        this.id = id;
        this.sensorid = actuatorid;
        this.webduinosystemid = webduinosystemid;
        this.name = name;
    }

    public WebduinoSystemActuator(JSONObject json) throws JSONException {
        fromJson(json);
    }

    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("webduinosystemid", webduinosystemid);
        json.put("sensorid", sensorid);
        json.put("actuatorid", sensorid); // da cancellare. mantenuto per compatibilità
        //json.put("name", name);
        SensorBase sensor = Core.getSensorFromId(sensorid);
        if (sensor != null) {
            json.put("status", sensor.getStatus().toJson());
            json.put("name", sensor.getName());
        }
        return json;
    }

    public void fromJson(JSONObject json) throws JSONException {
        if (json.has("id")) id = json.getInt("id");
        if (json.has("sensorid")) sensorid = json.getInt("sensorid");
        if (json.has("webduinosystemid")) webduinosystemid = json.getInt("webduinosystemid");
        if (json.has("name")) name = json.getString("name");
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM webduino_system_actuators WHERE id=" + id;
        stmt.executeUpdate(sql);
    }

    @Override
    public void write(Connection conn) throws SQLException {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "INSERT INTO webduino_system_actuators (id, sensorid, name, webduinosystemid)" +
                " VALUES ("
                + id + ","
                + sensorid + ","
                + "'" + name + "',"
                + webduinosystemid + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "sensorid=" + sensorid + ","
                + "name='" + name + "',"
                + "webduinosystemid=" + webduinosystemid + ";";
        Statement stmt = conn.createStatement();
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
    }
}
