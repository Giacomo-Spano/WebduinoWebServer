package com.server.webduino.core.webduinosystem;

import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONArray;
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
public class WebduinoSystemZone extends DBObject {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private int id;
    public int zoneid;
    public int webduinosystemid;

    public WebduinoSystemZone(int id,int zoneid, int webduinosystemid) {
        this.id = id;
        this.zoneid = zoneid;
        this.webduinosystemid = webduinosystemid;
     }

    public WebduinoSystemZone(JSONObject json) throws JSONException {
        fromJson(json);
    }

    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("webduinosystemid", webduinosystemid);
        Zone zone = Core.getZoneFromId(zoneid);
        if (zone != null) {
            json.put("name", zone.getName());
            json.put("status", zone.getStatus());
        }
        json.put("zoneid", zone.id);
        return json;
    }

    public void fromJson(JSONObject json) throws JSONException {
        if (json.has("id")) id = json.getInt("id");
        if (json.has("zoneid")) zoneid = json.getInt("zoneid");
        if (json.has("webduinosystemid")) webduinosystemid = json.getInt("webduinosystemid");
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM webduino_system_zones WHERE zoneid=" + zoneid;
        stmt.executeUpdate(sql);
    }

    @Override
    public void write(Connection conn) throws SQLException {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "INSERT INTO webduino_system_zones (id, zoneid, webduinosystemid)" +
                " VALUES ("
                + id + ","
                + zoneid + ","
                + webduinosystemid + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "zoneid=" + zoneid + ","
                + "webduinosystemid=" + webduinosystemid + ";";
        Statement stmt = conn.createStatement();
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
    }
}
