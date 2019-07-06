package com.server.webduino.core.datalog;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONArray;
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

    public DataLogValues getDataLogValue(int id, Date startDate, Date endDate) {
        return null;
    }



    public class DataLogValues {
        List<Date> dates = new ArrayList<>();
        List<List<Double>> values = new ArrayList<>();
        List<String> valueLabels = new ArrayList<>();
        //List<String> statuses = new ArrayList<>();
        //List<String> statusLabels = new ArrayList<>();
        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            JSONArray labeljarray = new JSONArray();
            //labeljarray.put("Data");
            for(String label:valueLabels) {
                labeljarray.put(label);
            }
            json.put("labels",labeljarray);

            JSONArray datejarray = new JSONArray();
            for(Date date:dates) {
                //SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss dd-MM-yyyy-MM-dd");
                SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
                String strdate = df.format(date);
                datejarray.put(strdate);
            }
            json.put("dates",datejarray);

            JSONArray datajarray = new JSONArray();
            for(int i = 0; i < values.size(); i++) {
                JSONObject jsonObject = new JSONObject();
                JSONArray valuejarray = new JSONArray();
                for (Double value : values.get(i)) {
                    valuejarray.put(value);
                }
                //jsonObject.put(/*valueLabels.get(i)*/"values",valuejarray);
                //jsonObject.put(/*valueLabels.get(i)*/"values2",valuejarray);
                datajarray.put(valuejarray);
                //datajarray.put(valuejarray);
            }

            json.put("data",datajarray);
            return json;
        }
    }
}
