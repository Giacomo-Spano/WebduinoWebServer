package com.server.webduino.core;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Devices {

    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private static ArrayList<Device> mDeviceList = new ArrayList<Device>();

    public Devices() {

    }

    public ArrayList<Device> getList() {

        return mDeviceList;
    }

    public Device getFromId(int id) {
        Iterator<Device> iterator = mDeviceList.iterator();
        while (iterator.hasNext()) {
            Device device = iterator.next();
            if (device.id == id)
                return device;
        }
        return null;
    }

    public void read() {

        LOGGER.info(" read devices");

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM devices";
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            mDeviceList.clear();
            while (rs.next()) {


                Device device = new Device();
                device.id = rs.getInt("id");
                device.tokenId = rs.getString("tokenid");
                device.name = rs.getString("name");
                device.date = rs.getDate("date");

                mDeviceList.add(device);
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

    public int insert(Device device) {

        int lastid;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            //Statement stmt = conn.createStatement();

            DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String date = "NULL";
            date = "'" + df.format((device.date)) + "'";



            String sql;

            sql = "INSERT INTO devices (tokenid, date, name)" +
                    " VALUES (" + "\"" + device.tokenId + "\"," + date + ",\"" + device.name + "\") " +
                    "ON DUPLICATE KEY UPDATE tokenid=\"" + device.tokenId + "\", date=" + date + ", name=\"" + device.name + "\"";

            Statement stmt = conn.createStatement();
            Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = -1;
            }

            if (affectedRows == 2) { // row updated
                device.id = lastid;
            } else if (affectedRows == 1) { // row inserted
                device.id = lastid;
            } else { // error
            }
            stmt.close();
            conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            return 0;

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
            return 0;
        }

        read(); // reload data
        return device.id;
    }

}
