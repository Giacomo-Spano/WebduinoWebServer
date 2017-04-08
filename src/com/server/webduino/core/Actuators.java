package com.server.webduino.core;

import com.server.webduino.servlet.SendPushMessages;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Actuators implements Shields.ShieldsListener {

    private static final Logger LOGGER = Logger.getLogger(Actuators.class.getName());

    private static ArrayList<Actuator> mActuatorList;// = new ArrayList<Actuator>();

    public Actuators() {
        if (mActuatorList == null) { // leggi da db solo all'avvio
            mActuatorList = new ArrayList<Actuator>();
            read();
        }

    }

    public ArrayList<Actuator> getActuatorList() {

        return mActuatorList;
    }

    public Actuator getFromShieldId(int shieldid, String subaddress) {
        Iterator<Actuator> iterator = mActuatorList.iterator();
        while (iterator.hasNext()) {
            Actuator actuator = iterator.next();
            if (actuator.shieldid == shieldid && actuator.subaddress.equals(subaddress))
                return actuator;
        }
        return null;
    }

    public Actuator getFromId(int id) {
        for (Actuator actuator : mActuatorList) {
            if (actuator.id == id)
                return actuator;
        }
        return null;
    }

    public void read() {

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM actuators";
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            while (rs.next()) {

                HeaterActuator actuator = new HeaterActuator();
                actuator.id = rs.getInt("id");
                actuator.name = rs.getString("name");
                actuator.subaddress = rs.getString("subaddress");
                actuator.shieldid = rs.getInt("shieldid");
                /*Shields shields = new Shields();
                URL url;
                url = shields.getURL(shieldid);*/


                mActuatorList.add(actuator);
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

    /*boolean requestActuatorsUpdate() {

        for(Actuator actuator : mActuatorList) {
            actuator.requestStatusUpdate();
        }
        return  true;
    }*/

    public SensorBase getFromShieldIdandSubaddress(int shieldid, String subaddress) {
        for (SensorBase sensor: mActuatorList) {
            if (sensor.subaddress.equals(subaddress) && sensor.shieldid == shieldid)
                return sensor;
        }
        return null;
    }


    /*public void requestActuatorsUpdate() {

        ///java.util.Date date = Core.getDate();

        for(Actuator actuator : mActuatorList){

            if (!actuator.isUpdated()) {
            //if (actuator.lastUpdate == null || (date.getTime() - actuator.lastUpdate.getTime()) > 2 * 60 * 1000) { // se l'ultimo aggiornamento è più
                                                                                                // vecchi0 di due minuti chiama getStatus dell'attuatore
                String res = actuator.requestStatusUpdate();
                if (res == null) {
                    LOGGER.severe("actuator " + actuator.id + " OFFLINE");
                    Core.sendPushNotification(SendPushMessages.notification_error, "errore", "ACTUATOR " + actuator.id + " OFFLINE", "0");
                } else {
                    LOGGER.info(res);
                }
            }
        }
    }*/

    @Override
    public void addedActuator(Actuator actuator) {
        mActuatorList.add(actuator);
    }

    @Override
    public void addedSensor(SensorBase sensor) {

    }

    @Override
    public void addedShield(Shield shield) {

    }

    @Override
    public void updatedActuator(Actuator actuator) {

    }

    @Override
    public void updatedSensor(SensorBase sensor) {

    }

    @Override
    public void updatedShield(Shield shield) {

    }


    public Actuator getActuatorFromId(int id) {
        for (Actuator actuator : mActuatorList) {
            if (actuator.id == id)
                return actuator;
        }
        return null;
    }
}
