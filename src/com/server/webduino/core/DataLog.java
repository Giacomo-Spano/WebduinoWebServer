package com.server.webduino.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class DataLog {
    public Date date = new Date();


    protected String getStrDate() {

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = Core.getDate();
        String strDate = "'" + df.format(date) + "'";
        return strDate;
    }

    public String getSQLInsert(String event, SensorBase sensor) {
        /*String sql;
        sql = "INSERT INTO sensordatalog (shieldid, subaddress, date, temperature, avtemperature) VALUES ("+ shieldid + ",'" + subaddress + "',"  + strDate + "," + temperature + "," + avTemperature + ");";
        return sql;*/
        return "";
    }

    public void writelog(String event, SensorBase sensor) {

        try {

            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            //sql = "INSERT INTO sensordatalog (shieldid, subaddress, date, temperature, avtemperature) VALUES ("+ shieldid + ",'" + subaddress + "',"  + strDate + "," + temperature + "," + avTemperature + ");";
            sql = getSQLInsert(event, sensor);
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
    public ArrayList<DataLog> getDataLog(int id, Date startDate, Date endDate) {
        return null;
    }

    DataLog getInterpolatedDataLog(Date t, DataLog dataA, DataLog dataB)
    {

        return this;
    }
}
