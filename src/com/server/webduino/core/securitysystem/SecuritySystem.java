package com.server.webduino.core.securitysystem;

import com.server.webduino.core.Core;
import com.server.webduino.core.Device;
import com.server.webduino.core.Devices;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class SecuritySystem {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private List<SecurityZone> securityZones = new ArrayList<>();
    private List<SecurityKey> securityKeys = new ArrayList<>();

    public void init() {
        read();
    }

    public void addZoneSensorListeners() {
        for(SecurityZone zone: securityZones) {
            zone.addSensorListeners();
        }
    }

    public void clearZoneSensorListeners() {
        for(SecurityZone zone: securityZones) {
            zone.clearSensorListeners();
        }
    }

    public void read() {

        LOGGER.info(" read Security Zones");

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM security_zones";
            ResultSet rs = stmt.executeQuery(sql);
            // Extract data from result set
            securityZones.clear();
            while (rs.next()) {
                SecurityZone zone = new SecurityZone(rs.getInt("id"));
                zone.setName(rs.getString("name"));
                securityZones.add(zone);
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
