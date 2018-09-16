package com.server.webduino.core;

import org.json.simple.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Created by giaco on 18/08/2018.
 */
public class IRCode {
    int id;
    String command;
    String codetype;
    String code;
    int bit;

    public IRCode(int id) throws Exception {
        this.id = id;
        readIRCode(id);
    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        json.put("command", command);
        json.put("codetype", codetype);
        json.put("code", code);
        json.put("bit", bit);

        return json;
    }

    private void readIRCode(int id) throws Exception {

        Class.forName("com.mysql.jdbc.Driver");
        Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
        String sql;
        Statement stmt = conn.createStatement();
        sql = "SELECT * FROM ircodes WHERE id=" + id +";";
        ResultSet resultSet = stmt.executeQuery(sql);
        if (resultSet.next()) {
            command = resultSet.getString("command");
            code = resultSet.getString("code");
            codetype = resultSet.getString("codetype");
            bit = resultSet.getInt("bit");
            resultSet.close();
            stmt.close();
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
