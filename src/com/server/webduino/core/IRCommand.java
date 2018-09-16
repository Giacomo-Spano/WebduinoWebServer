package com.server.webduino.core;

import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by giaco on 18/08/2018.
 */
public class IRCommand {
    int id;
    int deviceid;
    String command;
    String name;
    IRCodeSequence irsequence;

    public IRCommand(String command) throws Exception {
        this.command = command;
        readIRCommand(command);
    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        json.put("command", command);
        json.put("deviceid", deviceid);
        json.put("name", name);
        return json;
    }

    private void readIRCommand(String command) throws Exception {

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
        String sql;
        Statement stmt = conn.createStatement();
        sql = "SELECT * FROM ircommands WHERE command='" + command +"';";
        ResultSet resultSet = stmt.executeQuery(sql);
        if (resultSet.next()) {
            id = resultSet.getInt("id");
            name = resultSet.getString("name");
            deviceid = resultSet.getInt("deviceid");
            resultSet.close();
            stmt.close();
            irsequence = new IRCodeSequence(id);
            conn.close();
        } else {
            resultSet.close();
            stmt.close();
            conn.close();
            throw new Exception("not found");
        }
        resultSet.close();
        stmt.close();
        conn.close();
    }
}
