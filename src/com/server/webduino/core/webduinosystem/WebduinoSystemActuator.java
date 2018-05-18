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
    public int actuatorid;
    public int webduinosystemid;

    public WebduinoSystemActuator(int id, int actuatorid, int webduinosystemid) {
        this.id = id;
        this.actuatorid = actuatorid;
        this.webduinosystemid = webduinosystemid;
    }

    public WebduinoSystemActuator(JSONObject json) throws JSONException {
        fromJson(json);
    }

    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("webduinosystemid", webduinosystemid);
        json.put("actuatorid", actuatorid);
        SensorBase sensor = Core.getSensorFromId(actuatorid);
        if (sensor != null) {
            json.put("name", sensor.getName());
        }
        return json;
    }

    public void fromJson(JSONObject json) throws JSONException {
        if (json.has("id")) id = json.getInt("id");
        if (json.has("actuatorid")) actuatorid = json.getInt("actuatorid");
        if (json.has("webduinosystemid")) webduinosystemid = json.getInt("webduinosystemid");
    }


    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM webduino_system_actuators WHERE sensorid=" + actuatorid;
        stmt.executeUpdate(sql);
    }

    @Override
    public void write(Connection conn) throws SQLException {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "INSERT INTO webduino_system_actuators (id, sensorid, webduinosystemid)" +
                " VALUES ("
                + id + ","
                + actuatorid + ","
                + webduinosystemid + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "sensorid=" + actuatorid + ","
                + "webduinosystemid=" + webduinosystemid + ";";
        Statement stmt = conn.createStatement();
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
    }
}
