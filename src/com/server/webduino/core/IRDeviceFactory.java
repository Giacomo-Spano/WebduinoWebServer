package com.server.webduino.core;

import com.server.webduino.core.sensors.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;

/**
 * Created by giaco on 21/04/2017.
 */
public class IRDeviceFactory {

    public IRDevice createIRDevice(String aliasname, int zoneid) {

        IRDevice irdevice;
        try {
            Class.forName("com.mysql.jdbc.Driver");

            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            String sql;
            Statement stmt = conn.createStatement();
            //sql = "SELECT * FROM irdevices WHERE name=\"" + name + "\" AND zoneid=" + zoneid + ";";

            sql = "SELECT *\n" +
                    "FROM irdevices\n" +
                    "INNER JOIN irdevicealias ON irdevices.id = irdevicealias.irdeviceid\n" +
                    "WHERE irdevicealias.alias='" + aliasname + "';";

            ResultSet resultSet = stmt.executeQuery(sql);
            if (resultSet.next()) {

                String device = resultSet.getString("device");
                if (device.equals("tv")) {
                    irdevice = (IRDevice) new TVIRDevice(aliasname,zoneid);
                } else if (device.equals("airconditioner")) {
                    irdevice = (IRDevice) new TVIRDevice(aliasname,zoneid);
                } else if (device.equals("hifi")) {
                    irdevice = (IRDevice) new TVIRDevice(aliasname,zoneid);
                } else {
                    resultSet.close();
                    stmt.close();
                    conn.close();
                    return null;
                }

                irdevice.device = resultSet.getString("device");
                irdevice.description = resultSet.getString("description");
                //irdevice.type = resultSet.getString("type");
                irdevice.zoneid = resultSet.getInt("zoneid");
                irdevice.zonesensorid = resultSet.getInt("zonesensorid");

                resultSet.close();
                stmt.close();
                conn.close();

                return irdevice;
            }


        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
