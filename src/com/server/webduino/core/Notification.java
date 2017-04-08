package com.server.webduino.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * Created by Giacomo Spanò on 08/12/2015.
 */
public class Notification {

    public void writelog(java.util.Date date, double temperature, double avTemperature, boolean releStatus, int sensorId, String msg) {

        try {

            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String strdate = dateFormat.format(date);
            DateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            String strtime = timeFormat.format(date);

            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "INSERT INTO notification (date, time, temperature, avtemperature, relestatus, sensorID, message) VALUES" +
                    " ("+ "'" + strdate + "','" + strtime + "'," + temperature + "," + avTemperature + "," + releStatus + "," + sensorId + ",'" + msg +  "');";
            stmt.executeUpdate(sql);

            // Extract data from result set
            // Clean-up environment
            //rs.close();
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
