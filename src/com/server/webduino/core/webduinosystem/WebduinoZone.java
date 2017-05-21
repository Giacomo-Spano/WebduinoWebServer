package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;
import com.server.webduino.core.WebduinoTrigger;
import com.server.webduino.core.sensors.SensorBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoZone implements SensorBase.SensorListener {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    public interface WebduinoZoneListener {
        void onTrigger(WebduinoTrigger trigger);
        void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature);
    }
    protected List<WebduinoZoneListener> listeners = new ArrayList<WebduinoZoneListener>();
    public void addListener(WebduinoZoneListener toAdd) {
        listeners.add(toAdd);
    }

    private int id;
    private String name;
    protected List<ZoneSensor> zoneSensors = new ArrayList<>();
    protected List<ZoneProgram> zonePrograms = new ArrayList<>();

    private double temperature = 0.0;

    public WebduinoZone(int id, String name) {
        this.id = id;
        this.name = name;
        read(id);
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

    protected ZoneProgram getActiveProgram() {
        if (zonePrograms != null && zonePrograms.size() > 0)
            return zonePrograms.get(0);
        return null;
    }

    public void addSensorListeners() {
        for(ZoneSensor zonesensor: zoneSensors) {
            SensorBase sensor = Core.getSensorFromId(zonesensor.getSensorId());
            if (sensor != null)
                sensor.addListener(this);
        }
    }
    public void clearSensorListeners() {
        for(ZoneSensor zonesensor: zoneSensors) {
            SensorBase sensor = Core.getSensorFromId(zonesensor.getSensorId());
            sensor.deleteListener(this);
        }
    }

    @Override
    public void onChangeTemperature(int sensorId, double temperature, double oldtemperature) {
        for(WebduinoZoneListener listener: listeners) {
            listener.onTemperatureChange(id,temperature,oldtemperature);
        }
        this.temperature = temperature;
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

        ZoneProgram program = getActiveProgram();

        if (program != null)
            program.triggerAlarm();
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
            sql = "SELECT * FROM zonesensors WHERE zoneid=" + zoneid;
            ResultSet rs = stmt.executeQuery(sql);
            // Extract data from result set
            zoneSensors.clear();
            while (rs.next()) {
                ZoneSensor zoneSensor = new ZoneSensor();
                zoneSensor.setId(rs.getInt("id"));
                zoneSensor.setSensorId(rs.getInt("sensorid"));
                zoneSensors.add(zoneSensor);
            }
            // Clean-up environment
            rs.close();
            stmt.close();

            // read zone programs
            stmt = conn.createStatement();
            sql = "SELECT * FROM zoneprograms WHERE id=" + zoneid;
            rs = stmt.executeQuery(sql);
            // Extract data from result set
            zonePrograms.clear();
            while (rs.next()) {
                String type = rs.getString("type");
                WebduinoZoneProgramFactory factory = new WebduinoZoneProgramFactory();
                int programId = rs.getInt("id");
                String programName = rs.getString("name");
                Time time = rs.getTime("time");
                Calendar cal = Calendar.getInstance();
                cal.setTime(time);
                ZoneProgram zoneProgram = factory.createZoneProgram(programId,programName,type,cal.get(Calendar.SECOND));

                zonePrograms.add(zoneProgram);
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

    public void init(int id) {

    }
}
