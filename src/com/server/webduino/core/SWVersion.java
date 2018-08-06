package com.server.webduino.core;

import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

/**
 * Created by giaco on 23/06/2017.
 */
public class SWVersion {

    private int id;
    public String name;
    public String version;
    public String path;
    public String filename;
    public String type;


    public SWVersion(int id, String name, String version, String path, String filename, String type) {
        this.id = id;
        this.name = name;
        this.version = version;
        this.path = path;
        this.filename = filename;
        this.type = type;
    }

    public static SWVersion getLatestVersion(String type) {

        if (type == null || type.equals(""))
            type = "webduino";

        SWVersion swversion = null;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM swversions WHERE type=" + type +" ORDER BY version DESC LIMIT 1";
            ResultSet swversionsResultSet = stmt.executeQuery(sql);
            if (swversionsResultSet.next()) {
                int id = swversionsResultSet.getInt("id");
                String name = swversionsResultSet.getString("name");
                String version = swversionsResultSet.getString("version");
                String path = swversionsResultSet.getString("path");
                String filename = swversionsResultSet.getString("filename");
                swversion = new SWVersion(id, name, version, path, filename,type);
            }
            swversionsResultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return swversion;
    }

    public boolean write() {

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql = "INSERT INTO swversions (id, name, version,path,type,filename)" +
                    " VALUES ("
                    + id + ","
                    + "\"" + name + "\","
                    + "\"" + version + "\","
                    + "\"" + path + "\","
                    + "\"" + type + "\","
                    + "\"" + filename + "\" ) " +
                    "ON DUPLICATE KEY UPDATE "
                    + "name=\"" + name + "\","
                    + "version=\"" + version + "\","
                    + "path=\"" + path + "\","
                    + "path=\"" + type + "\","
                    + "filename=\"" + filename + "\";";
            stmt.executeUpdate(sql);
            stmt.close();
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static JSONArray getSWVersionJSONArray() {

        JSONArray jsonArray = new JSONArray();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM swversions ORDER BY version";
            ResultSet swversionsResultSet = stmt.executeQuery(sql);
            while (swversionsResultSet.next()) {
                int id = swversionsResultSet.getInt("id");
                String name = swversionsResultSet.getString("name");
                String version = swversionsResultSet.getString("version");
                String path = swversionsResultSet.getString("path");
                String filename = swversionsResultSet.getString("filename");

                JSONObject json = new JSONObject();
                json.put("id", id);
                json.put("version", version);
                json.put("name", name);
                jsonArray.put(json);
            }
            swversionsResultSet.close();
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray;
    }
}
