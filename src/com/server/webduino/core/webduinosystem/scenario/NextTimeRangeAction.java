package com.server.webduino.core.webduinosystem.scenario;

import com.server.webduino.core.webduinosystem.scenario.actions.ProgramAction;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by gs163400 on 30/12/2017.
 */
public class NextTimeRangeAction implements Comparable<NextTimeRangeAction>{
    public LocalDate date;
    public LocalTime start;
    public LocalTime end;
    public ProgramAction action;
    public ScenarioProgramTimeRange timeRange;
    public ScenarioProgram program;
    public int timeintervalid;
    public Scenario scenario;

    List<Conflict> conflictList = new ArrayList<>();

    //NextTimeRangeAction()

    public void addConflict(Conflict newconflict) {

        // controlla che non ci sia già nella lista altrimenti
        for (Conflict conflict : conflictList) {
            if (conflict.action.id == newconflict.action.id) {
                return;
            }
        }
        conflictList.add(newconflict);
    }

    public JSONObject toJson() {

        JSONObject json = new JSONObject();
        try {
            json.put("starttime", start.toString());
            json.put("endtime", end.toString());
            json.put("date", date.toString());

            if (timeRange != null) {
                json.put("actionid", action.id);
                json.put("actionname", action.name);
                json.put("actiontype", action.type + " " + action.targetvalue);
                json.put("action", action.toJson());
                json.put("timerangeid", timeRange.id);
                json.put("timerangename", timeRange.name);
                json.put("programid", program.id);
                json.put("programname", program.name);
                json.put("scenarioid", scenario.id);
                json.put("scenarioname", scenario.name);
                json.put("timeintervalid", timeintervalid);
                json.put("actuatorid", action.actuatorid);
                //json.put("actuatorname", action.);
                String str = "";
                int i = 0;
                for(Conflict conflict:conflictList) {
                    if (i++ != 0)
                        str +=";";
                    str += conflict.action.id;
                }
                json.put("conflicts", str);
            }

            /*SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            if (date != null)
                json.put("nextjobdate", df.format(date));*/

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

    @Override
    public int compareTo(NextTimeRangeAction o) {

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR,date.getYear());
        cal.set(Calendar.MONTH,date.getMonthValue()-1);
        cal.set(Calendar.DAY_OF_MONTH,date.getDayOfMonth());
        cal.set(Calendar.HOUR_OF_DAY,start.getHour());
        cal.set(Calendar.MINUTE,start.getMinute());
        cal.set(Calendar.SECOND,start.getSecond());
        Date date1 = cal.getTime();

        cal.set(Calendar.YEAR,o.date.getYear());
        cal.set(Calendar.MONTH,o.date.getMonthValue()-1);
        cal.set(Calendar.DAY_OF_MONTH,o.date.getDayOfMonth());
        cal.set(Calendar.HOUR_OF_DAY,o.start.getHour());
        cal.set(Calendar.MINUTE,o.start.getMinute());
        cal.set(Calendar.SECOND,o.start.getSecond());
        Date date2 = cal.getTime();

        if (date1.before(date2)) {
            return -1;
        } else if (date1.after(date2)) {
            return 1;
        } else {

            cal.set(Calendar.YEAR,date.getYear());
            cal.set(Calendar.MONTH,date.getMonthValue()-1);
            cal.set(Calendar.DAY_OF_MONTH,date.getDayOfMonth());
            cal.set(Calendar.HOUR_OF_DAY,end.getHour());
            cal.set(Calendar.MINUTE,end.getMinute());
            cal.set(Calendar.SECOND,end.getSecond());
            Date enddate1 = cal.getTime();

            cal.set(Calendar.YEAR,o.date.getYear());
            cal.set(Calendar.MONTH,o.date.getMonthValue()-1);
            cal.set(Calendar.DAY_OF_MONTH,o.date.getDayOfMonth());
            cal.set(Calendar.HOUR_OF_DAY,o.end.getHour());
            cal.set(Calendar.MINUTE,o.end.getMinute());
            cal.set(Calendar.SECOND,o.end.getSecond());
            Date enddate2 = cal.getTime();

            if (enddate1.before(enddate2)) {
                return -1;
            } else if (enddate1.after(enddate2)) {
                return 1;
            }
            return 0;
        }
    }
}
