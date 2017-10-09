package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.scenario.programinstructions.ProgramInstructions;
import com.server.webduino.core.webduinosystem.scenario.programinstructions.ProgramInstructionsFactory;
import com.server.webduino.core.webduinosystem.scenario.programtimeranges.ProgramTimeRange;
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
//import java.Time;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class ScenarioProgram {

    public int id;
    public int scenarioId = 0;
    public boolean enabled;
    public String name;

    public List<ProgramTimeRange> timeRanges = new ArrayList<>();

    public ScenarioProgram() {
    }

    public ScenarioProgram(JSONObject json) {
        fromJson(json);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("id", id);
            json.put("scenarioid", scenarioId);
            json.put("name", name);
            json.put("enabled", enabled);

            JSONArray jarray = new JSONArray();
            if (timeRanges != null) {
                for (ProgramTimeRange timeRange : timeRanges) {
                    jarray.put(timeRange.toJson());
                }
                json.put("timeranges", jarray);
            }



            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean fromJson(JSONObject json) {
        try {
            if (json.has("id")) {
                id = json.getInt("id");
            }
            if (json.has("scenarioid")) {
                scenarioId = json.getInt("scenarioid");
            }
            if (json.has("enabled")) {
                enabled = json.getBoolean("enabled");
            }
            if (json.has("name")) {
                name = json.getString("name");
            }

            if (json.has("programtimeranges")) {

                JSONArray jsonArray = json.getJSONArray("programtimeranges");
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);

                    ProgramTimeRange timeRange = new ProgramTimeRange(jo);


                    /*int id = 0, scenarioprogramid = 0;
                    String name = "";
                    boolean enabled = false;
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

                    if (jo.has("id")) id = jo.getInt("id");
                    if (jo.has("scenarioprogramid")) scenarioprogramid = jo.getInt("scenarioprogramid");
                    if (jo.has("name")) name = jo.getString("name");
                    if (jo.has("starttime")) {
                        String datestr = jo.getString("starttime");
                        try {
                            startTime = dateFormat.parse(datestr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (jo.has("endtime")) {
                        String datestr = jo.getString("endtime");
                        try {
                            endTime = dateFormat.parse(datestr);
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    if (jo.has("enabled")) enabled = jo.getBoolean("enabled");

                    ProgramTimeRange timeRange = new ProgramTimeRange(id, scenarioprogramid, name, startTime, endTime, enabled);
                    */
                    if (timeRange != null) {
                        timeRanges.add(timeRange);
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

            String sql = "INSERT INTO scenarios_programs (id, name, enabled, scenarioid)" +
                    " VALUES ("
                    + id + ","
                    + "\"" + name + "\","
                    + Core.boolToString(enabled) + ","
                    + scenarioId
                    + ") " +
                    "ON DUPLICATE KEY UPDATE "
                    + "name=\"" + name + "\","
                    + "enabled=" + Core.boolToString(enabled) + ","
                    + "scenarioid=" + scenarioId + ";";
            stmt.executeUpdate(sql);


            for (ProgramTimeRange timeRange : timeRanges) {
                if (!timeRange.write(stmt)) {
                    stmt.close();
                    conn.commit();
                    return false;
                }
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


}
