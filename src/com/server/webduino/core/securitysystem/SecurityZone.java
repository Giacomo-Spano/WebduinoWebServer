package com.server.webduino.core.securitysystem;

import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.sensors.SensorBase;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class SecurityZone implements SensorBase.SensorListener {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    public interface ZoneListener {
        void onSecurityAlarm(SecurityAlarm securityAlarm);
    }

    protected List<SensorBase.SensorListener> listeners = new ArrayList<SensorBase.SensorListener>();

    public void addListener(SensorBase.SensorListener toAdd) {
        listeners.add(toAdd);
    }

    private int id;
    private String name;
    private List<SecurityZoneSensor> securityZoneSensors = new ArrayList<>();
    private List<SecurityZoneProgram> securityZonePrograms = new ArrayList<>();

    public SecurityZone(int id) {
        this.id = id;
        read(id);
    }

    int getId() {
        return id;
    }

    void setName(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    SecurityZoneProgram getActiveProgram() {
        if (securityZonePrograms != null && securityZonePrograms.size() > 0)
            return securityZonePrograms.get(0);
        return null;
    }

    public void addSensorListeners() {
        for(SecurityZoneSensor zonesensor: securityZoneSensors) {
            SensorBase sensor = Core.getSensorFromId(zonesensor.getSensorId());
            sensor.addListener(this);
        }
    }
    public void clearSensorListeners() {
        for(SecurityZoneSensor zonesensor: securityZoneSensors) {
            SensorBase sensor = Core.getSensorFromId(zonesensor.getSensorId());
            sensor.deleteListener(this);
        }
    }

    public void read(int zoneid) {

        LOGGER.info(" read Security Zone Sensors");

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query

            // read zone sensors
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM security_zonesensors WHERE zoneid=" + zoneid;
            ResultSet rs = stmt.executeQuery(sql);
            // Extract data from result set
            securityZoneSensors.clear();
            while (rs.next()) {
                SecurityZoneSensor zoneSensor = new SecurityZoneSensor();
                zoneSensor.setId(rs.getInt("id"));
                zoneSensor.setSensorId(rs.getInt("sensorid"));
                securityZoneSensors.add(zoneSensor);
                //SensorBase sensor = Core.getSensorFromId(zoneSensor.getSensorId());
                //sensor.addListener(this);
            }
            // addSensorListeners();
            // Clean-up environment
            rs.close();
            stmt.close();

            // read zone programs
            stmt = conn.createStatement();
            sql = "SELECT * FROM security_zoneprograms WHERE id=" + id;
            rs = stmt.executeQuery(sql);
            // Extract data from result set
            securityZoneSensors.clear();
            while (rs.next()) {
                String type = rs.getString("type");
                SecurityProgramFactory factory = new SecurityProgramFactory();
                int programId = rs.getInt("id");
                String programName = rs.getString("name");
                Time time = rs.getTime("time");
                Calendar cal = Calendar.getInstance();
                cal.setTime(time);
                SecurityZoneProgram securityZoneProgram = factory.createSecurityZone(programId,programName,type,cal.get(Calendar.SECOND));

                securityZonePrograms.add(securityZoneProgram);
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

    @Override
    public void changeTemperature(int sensorId, double temperature) {

    }

    @Override
    public void changeAvTemperature(int sensorId, double avTemperature) {

    }

    @Override
    public void changeOnlineStatus(boolean online) {

    }

    @Override
    public void changeOnlineStatus(int sensorId, boolean online) {

    }

    @Override
    public void changeDoorStatus(int sensorId, boolean open) {

        SecurityZoneProgram program = getActiveProgram();

        if (program != null)
            program.triggerAlarm();
    }
}
