package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.Schedule;
import com.server.webduino.core.webduinosystem.security.SecurityKey;
import com.server.webduino.core.webduinosystem.security.SecurityZone;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystem {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    public List<WebduinoZone> zones = new ArrayList<>();
    private List<SecurityKey> keys = new ArrayList<>();
    private int id;
    private String name;
    private Schedule schedule ;


    public WebduinoSystem(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void init(int system) {
        read(system);
    }

    public void addZoneSensorListeners() {
        for(WebduinoZone zone: zones) {
            zone.addSensorListeners();
        }
    }

    public void clearZoneSensorListeners() {
        for(WebduinoZone zone: zones) {
            zone.clearSensorListeners();
        }
    }

    public void read(int systemid) {

        LOGGER.info(" read Security zones");

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM zones WHERE systemid=" + systemid;
            ResultSet rs = stmt.executeQuery(sql);
            // Extract data from result set
            zones.clear();
            while (rs.next()) {
                WebduinoZoneFactory factory = new WebduinoZoneFactory();
                int id = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type");
                WebduinoZone zone = factory.createWebduinoZone(id, name, type);
                if (zone != null)
                    zones.add(zone);
            }
            // Clean-up environment
            rs.close();
            stmt.close();

            schedule = new Schedule();
            schedule.read(id);

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
