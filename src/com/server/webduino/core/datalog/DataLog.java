package com.server.webduino.core.datalog;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.Command;

import java.sql.*;
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

    /*public String getSQLInsert(String event, SensorBase sensor) {
        return "";
    }*/

    /*public String getSQLInsert(String event, Command command) {
        return "";
    }*/

    public String getSQLInsert(String event, Object object) {
        return "";
    }

    /*public int writelog(String event, SensorBase sensor) {

        String sql;
        int id = -1;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            sql = getSQLInsert(event, sensor);
            System.out.print(sql);

            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return id;
    }

    public int writelog(String event, Command command) {

        String sql;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            sql = getSQLInsert(event, command);
            System.out.print(sql);
            stmt.executeUpdate(sql);
            stmt.close();

            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
    }*/

    public int writelog(String event, Object object) {

        String sql;
        int id = -1;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            sql = getSQLInsert(event, object);
            System.out.print(sql);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
            Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                id = rs.getInt(1);
            }
            stmt.close();
            conn.close();

            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return id;
    }

    public ArrayList<DataLog> getDataLog(int id, Date startDate, Date endDate) {
        return null;
    }

    DataLog getInterpolatedDataLog(Date t, DataLog dataA, DataLog dataB)
    {

        return this;
    }
}
