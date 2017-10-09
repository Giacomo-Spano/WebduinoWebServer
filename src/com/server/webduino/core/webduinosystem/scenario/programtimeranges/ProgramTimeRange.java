package com.server.webduino.core.webduinosystem.scenario.programtimeranges;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.scenario.programinstructions.ProgramInstructions;
import com.server.webduino.core.webduinosystem.scenario.programinstructions.ProgramInstructionsFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by giaco on 17/05/2017.
 */
public class ProgramTimeRange {

    public int id;
    public int programid;
    public String name;
    public Date startTime;
    public Date endTime;
    public boolean enabled;
    public List<ProgramInstructions> programInstructionsList = new ArrayList<>();

    public ProgramTimeRange(int id, int programid, String name, Date startTime, Date endTime, boolean enabled, List<ProgramInstructions> instructions) {
        this.id = id;
        this.programid = programid;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.enabled = enabled;
        this.programInstructionsList = instructions;
    }

    public ProgramTimeRange(JSONObject json) {
        fromJson(json);
    }

    public void init() {

    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("programid", programid);
            json.put("name", name);
            json.put("starttime", startTime);
            json.put("endtime", endTime);
            json.put("enabled", enabled);

            JSONArray jarray = new JSONArray();
            if (programInstructionsList != null) {
                for (ProgramInstructions programInstructions : programInstructionsList) {
                    jarray.put(programInstructions.toJson());
                }
                json.put("programinstructions", jarray);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    public boolean fromJson(JSONObject json) {

        try {
            if (json.has("id"))
                id = json.getInt("id");
            if (json.has("programid"))
                programid = json.getInt("programid");
            if (json.has("name"))
                name = json.getString("name");
            SimpleDateFormat df = new SimpleDateFormat("HH:mm");
            if (json.has("starttime")) {
                String str = json.getString("starttime");
                try {
                    startTime = df.parse(str);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (json.has("endtime")) {
                String str = json.getString("endtime");
                try {
                    endTime = df.parse(str);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            if (json.has("enabled"))
                enabled = json.getBoolean("enabled");


            if (json.has("programinstructions")) {

                ProgramInstructionsFactory factory = new ProgramInstructionsFactory();
                JSONArray jsonArray = json.getJSONArray("programinstructions");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);

                    String type = "";
                    if (jo.has("type"))
                        type = jo.getString("type");
                    else
                        continue;

                    int id = 0, /*programid = 0, */actuatorid = 0, zoneId = 0, seconds = 0, priority = 0;
                    String name = "";
                    double targetValue = 0.0;
                    boolean schedule = false, sunday = true, monday = true, tuesday = true, wednesday = true, thursday = true, friday = true, saturday = true;
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
                    Calendar myCal = Calendar.getInstance();
                    myCal.set(Calendar.YEAR, 0);
                    myCal.set(Calendar.MONTH, 0);
                    myCal.set(Calendar.DAY_OF_MONTH, 0);
                    myCal.set(Calendar.HOUR_OF_DAY, 0);
                    myCal.set(Calendar.MINUTE, 0);
                    myCal.set(Calendar.SECOND, 0);
                    Date startTime = myCal.getTime();
                    Date endTime = startTime;

                    if (jo.has("id"))
                        id = jo.getInt("id");
                    //if (jo.has("programid")) programid = jo.getInt("programid");
                    if (jo.has("name")) name = jo.getString("name");
                    if (jo.has("actuatorid")) actuatorid = jo.getInt("actuatorid");
                    if (jo.has("targetvalue")) targetValue = jo.getDouble("targetvalue");
                    if (jo.has("zoneid")) zoneId = jo.getInt("zoneid");

                    if (jo.has("time")) {
                        DateFormat timeFormat = new SimpleDateFormat("HH:mm");
                        String timeStr = jo.getString("time");
                        try {
                            Date time = timeFormat.parse(timeStr);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(time);
                            seconds = cal.get(Calendar.SECOND);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (jo.has("starttime")) {
                        String datestr = jo.getString("starttime");
                        try {
                            if (!datestr.equals(""))
                                startTime = dateFormat.parse(datestr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (jo.has("endtime")) {
                        String datestr = jo.getString("endtime");
                        try {
                            if (!datestr.equals(""))
                                endTime = dateFormat.parse(datestr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }

                    if (jo.has("sunday")) sunday = jo.getBoolean("sunday");
                    if (jo.has("monday")) monday = jo.getBoolean("monday");
                    if (jo.has("tuesday")) tuesday = jo.getBoolean("tuesday");
                    if (jo.has("wednesday")) wednesday = jo.getBoolean("wednesday");
                    if (jo.has("thursday")) thursday = jo.getBoolean("thursday");
                    if (jo.has("friday")) friday = jo.getBoolean("friday");
                    if (jo.has("saturday")) saturday = jo.getBoolean("saturday");
                    if (jo.has("priority")) priority = jo.getInt("priority");

                    ProgramInstructions programInstructions = factory.createProgramInstructions(id, programid, name, type, actuatorid, targetValue, zoneId, seconds,
                            schedule, sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority);
                    if (programInstructions != null) {
                        programInstructionsList.add(programInstructions);
                    }
                }
            }
            return true;

        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean save() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection(Core.getDbUrl(), Core.getUser(), Core.getPassword());
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_UPDATABLE);

            if (!write(stmt)) {
                stmt.close();
                conn.commit();
                return false;
            }

            stmt.close();
            conn.commit();
            return true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean write(Statement stmt) {

        Calendar myCal = Calendar.getInstance();
        myCal.set(Calendar.YEAR, 0);
        myCal.set(Calendar.MONTH, 0);
        myCal.set(Calendar.DAY_OF_MONTH, 0);
        myCal.set(Calendar.HOUR_OF_DAY, 0);
        myCal.set(Calendar.MINUTE, 0);
        myCal.set(Calendar.SECOND, 0);

        if (startTime == null)
            startTime = myCal.getTime();
        if (endTime == null)
            endTime = myCal.getTime();;

        String sql;
        DateFormat df = new SimpleDateFormat("HH:mm:ss");
        //DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        sql = "INSERT INTO scenarios_programtimeranges (id, programid, name, starttime, endtime, enabled)" +
                " VALUES ("
                + id + ","
                + programid + ","
                + "\"" + name + "\","
                + "'" + df.format(startTime) + "',"
                + "'" + df.format(endTime) + "',"
                + "" + Core.boolToString(enabled) + ") " +
                "ON DUPLICATE KEY UPDATE "
                + "id=" + id + ","
                + "programid=" + programid + ","
                + "name=\"" + name + "\","
                + "starttime='" + df.format(startTime) + "',"
                + "endtime='" + df.format(endTime) + "',"
                + "enabled=" + Core.boolToString(enabled) + ";";
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }

        for (ProgramInstructions instructions : programInstructionsList) {
            Date time = new Date(instructions.seconds * 1000);
            sql = "INSERT INTO scenarios_programinstructions (id, timerangeid, type, name, actuatorid, targetvalue, zoneid, time, schedule, sunday, monday, tuesday,wednesday, thursday, friday, saturday, priority)" +
                    " VALUES ("
                    + instructions.id + ","
                    + instructions.timerangeid + ","
                    + "\"" + instructions.type + "\","
                    + "\"" + instructions.name + "\","
                    + instructions.actuatorid + ","
                    + instructions.targetValue + ","
                    + instructions.zoneId + ","
                    + "'" + df.format(time) + "',"
                    + "" + Core.boolToString(instructions.schedule) + ","
                    + "" + Core.boolToString(instructions.sunday) + ","
                    + "" + Core.boolToString(instructions.monday) + ","
                    + "" + Core.boolToString(instructions.tuesday) + ","
                    + "" + Core.boolToString(instructions.wednesday) + ","
                    + "" + Core.boolToString(instructions.thursday) + ","
                    + "" + Core.boolToString(instructions.friday) + ","
                    + "" + Core.boolToString(instructions.saturday) + "," +
                    + instructions.priority + ") " +
                    "ON DUPLICATE KEY UPDATE "
                    + "id=" + instructions.id + ","
                    + "timerangeid=" + instructions.timerangeid + ","
                    + "type=\"" + instructions.type + "\","
                    + "name=\"" + instructions.name + "\","
                    + "actuatorid=" + instructions.actuatorid + ","
                    + "targetValue=" + instructions.targetValue + ","
                    + "zoneId=" + instructions.zoneId + ","
                    + "time='" + df.format(time) + "',"
                    + "schedule=" + Core.boolToString(instructions.schedule) + ","
                    + "sunday=" + Core.boolToString(instructions.sunday) + ","
                    + "monday=" + Core.boolToString(instructions.monday) + ","
                    + "tuesday=" + Core.boolToString(instructions.tuesday) + ","
                    + "wednesday=" + Core.boolToString(instructions.wednesday) + ","
                    + "thursday=" + Core.boolToString(instructions.thursday) + ","
                    + "friday=" + Core.boolToString(instructions.friday) + ","
                    + "saturday=" + Core.boolToString(instructions.saturday) + ","
                    + "priority=" + instructions.priority + ";";

            try {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
}
