package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Core;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystemFactory {

    public WebduinoSystem createWebduinoSystem(int id, String name, String type, boolean enabled) {
        WebduinoSystem system = null;
        if (type.equals("securitysystem")) {
            system = new SecuritySystem(id,name,type,enabled);
        } else if (type.equals("heatersystem")) {
            system = new HeaterSystem(id,name,type,enabled);
        } else {
            system = new WebduinoSystem(id,name,type,enabled);
        }
        if (system != null) {
            system.init(id);
        }
        return system;
    }


    public static JSONArray getWebduinoSystemTypesJSONArray() {
        JSONArray jsonArray = new JSONArray();
        try {
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);

            String sql;
            sql = "SELECT * FROM webduinosystemtypes";
            ResultSet webduinotypesResultSet = stmt.executeQuery(sql);
            JSONObject json;
            while (webduinotypesResultSet.next()) {
                json = new JSONObject();
                json.put("id", webduinotypesResultSet.getInt("id"));
                json.put("type", webduinotypesResultSet.getString("type"));
                json.put("description", webduinotypesResultSet.getString("description"));
                jsonArray.put(json);

            }
            webduinotypesResultSet.close();
            stmt.close();
            conn.close();

        } catch (SQLException se) {
            se.printStackTrace();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return jsonArray;
    }
}
