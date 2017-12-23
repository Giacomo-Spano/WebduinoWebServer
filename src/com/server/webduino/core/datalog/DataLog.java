package com.server.webduino.core.datalog;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.Command;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
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
        return "";
    }

    public String getSQLInsert(String event, Command command) {
        return "";
    }

    public void writelog(String event, SensorBase sensor) {

        String sql;
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            sql = getSQLInsert(event, sensor);
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
    }

    public void writelog(String event, Command command) {

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
    }

    public ArrayList<DataLog> getDataLog(int id, Date startDate, Date endDate) {
        return null;
    }

    DataLog getInterpolatedDataLog(Date t, DataLog dataA, DataLog dataB)
    {

        return this;
    }
}
