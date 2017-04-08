package com.server.webduino.core;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Settings {

    public int HeaterActuatorId = 0;

    public Settings() {
        load();
    }

    public void load() {

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM settings";
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            if (rs.next()) {
                HeaterActuatorId = rs.getInt("heateractuatorid");
            }
            // Clean-up environment
            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }
}
