package com.server.webduino.core.webduinosystem;

import com.server.webduino.DBObject;
import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.webduinosystem.scenario.ScenarioProgram;
import com.server.webduino.core.webduinosystem.scenario.ScenarioTimeInterval;
import com.server.webduino.core.webduinosystem.scenario.ScenarioTrigger;
import com.server.webduino.core.webduinosystem.services.Service;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystemService extends DBObject {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private int id;
    public int serviceid;
    public int webduinosystemid;

    public WebduinoSystemService(int id, int serviceid, int webduinosystemid) {
        this.id = id;
        this.serviceid = serviceid;
        this.webduinosystemid = webduinosystemid;
    }

    public WebduinoSystemService(JSONObject json) throws JSONException {
        fromJson(json);
    }

    public void fromJson(JSONObject json) throws JSONException {

        if (json.has("id")) id = json.getInt("id");
        if (json.has("serviceid")) serviceid = json.getInt("serviceid");
        if (json.has("webduinosystemid")) webduinosystemid = json.getInt("webduinosystemid");
    }

    @Override
    public void delete(Statement stmt) throws SQLException {
        String sql = "DELETE FROM webduino_system_services WHERE serviceid=" + serviceid;
        stmt.executeUpdate(sql);
    }

    @Override
    public void write(Connection conn) throws SQLException {

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sql = "INSERT INTO webduino_system_services (id, serviceid, webduinosystemid)" +
                " VALUES ("
                + id + ","
                + serviceid + ","
                + webduinosystemid + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "serviceid=" + serviceid + ","
                + "webduinosystemid=" + webduinosystemid + ";";
        Statement stmt = conn.createStatement();
        Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
    }


    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("webduinosystemid", webduinosystemid);
        Service service = Core.getServiceFromId(serviceid);
        if (service != null)
            json.put("name", service.name);
        json.put("serviceid", service.getId());
        return json;
    }


}
