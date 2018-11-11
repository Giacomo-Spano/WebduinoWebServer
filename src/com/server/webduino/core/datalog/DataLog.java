package com.server.webduino.core.datalog;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.DoorSensor;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.Command;
import com.server.webduino.servlet.SystemServlet;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DataLog {
    public Date date = new Date();
    public String tableName = "sensordatalog";

    protected String getStrDate() {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = Core.getDate();
        String strDate = "'" + df.format(date) + "'";
        return strDate;
    }

    public String getSQLInsert(String event, Object object) {

        SensorBase sensor = (SensorBase) object;
        String sql;
        sql = "INSERT INTO " + tableName + " (sensorid, date, status) VALUES ("
                + sensor.getId() + ","  + getStrDate() + ",\"" + sensor.getStatus().status + "\");";
        return sql;
    }

    public int writelog(String event, Object object) {

        String sql = "";
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

            //conn.close();
        } catch (SQLException se) {
            //Handle errors for JDBC
            System.out.print(sql);
            se.printStackTrace();

        } catch (Exception e) {
            //Handle errors for Class.forName
            e.printStackTrace();
        }
        return id;
    }

    public List<DataLog> getDataLog(int id,Date startDate, Date endDate) {
        return null;
    }

    public JSONObject toJson() throws JSONException {
        return null;
    }
}
