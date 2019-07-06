package com.server.webduino.core;

import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
public class Mediaplayers {

    private static final Logger LOGGER = Logger.getLogger(Mediaplayers.class.getName());

    public Mediaplayers() {

    }

    public Mediaplayer getMediaplayerFromId(int id) {

        LOGGER.info(" getMediaplayerFromId");
        ArrayList<Mediaplayer> mediaplayers = new ArrayList<Mediaplayer>();

        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM mediaplayers WHERE id="+id;
            ResultSet rs = stmt.executeQuery(sql);
            // Extract data from result set
            if (rs.next()) {
                Mediaplayer mediaplayer = mediaplayerFromResultset(rs);
                rs.close();
                stmt.close();
                conn.close();
                return mediaplayer;
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
        return null;
    }

    private Mediaplayer mediaplayerFromResultset(ResultSet rs) throws SQLException {
        Mediaplayer mediaplayer = new Mediaplayer();
        mediaplayer.id = rs.getInt("id");
        mediaplayer.name = rs.getString("name");
        mediaplayer.description = rs.getString("description");
        return mediaplayer;
    }

    public ArrayList<Mediaplayer> get() {

        LOGGER.info("get");
        ArrayList<Mediaplayer> mediaplayers = new ArrayList<Mediaplayer>();

        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            Statement stmt = conn.createStatement();
            String sql;
            sql = "SELECT * FROM mediaplayers";
            ResultSet rs = stmt.executeQuery(sql);

            // Extract data from result set
            while (rs.next()) {
                Mediaplayer mediaplayer = mediaplayerFromResultset(rs);
                mediaplayers.add(mediaplayer);
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
        return mediaplayers;
    }

    public int insert(Mediaplayer mediaplayer) {

        int lastid;
        try {
            // Register JDBC driver
            Class.forName("com.mysql.jdbc.Driver");
            // Open a connection
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            // Execute SQL query
            String sql;
            sql = "INSERT INTO devices (tokenid, date, name)" +
                    " VALUES (" + "\"" + mediaplayer.name + "\",\"" + mediaplayer.description + "\") " +
                    "ON DUPLICATE KEY UPDATE id=\"" + mediaplayer.id + ", name=\"" + mediaplayer.name + "\"" + ", description=\"" + mediaplayer.description + "\"";

            Statement stmt = conn.createStatement();
            Integer affectedRows = stmt.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                lastid = rs.getInt(1);
            } else {
                lastid = -1;
            }

            if (affectedRows == 2) { // row updated
                mediaplayer.id = lastid;
            } else if (affectedRows == 1) { // row inserted
                mediaplayer.id = lastid;
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
        return mediaplayer.id;
    }
}
