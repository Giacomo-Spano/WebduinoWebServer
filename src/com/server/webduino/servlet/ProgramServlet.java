package com.server.webduino.servlet;

import com.quartz.QuartzListener;
import com.server.webduino.core.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
//@WebServlet(name = "SensorServlet")
public class ProgramServlet extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(Actuator.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        StringBuffer jb = new StringBuffer();
        String line = null;

        String IDValue = request.getParameter("id");
        String deleteValue = request.getParameter("delete");
        if (IDValue != null && deleteValue != null && deleteValue.equals("true")) {

            deleteProgram(response, IDValue);
            return;
        }


        int lastid;
        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Core core = (Core)getServletContext().getAttribute(QuartzListener.CoreClass);

            Program program = getProgramFromJson(jb);
            lastid = core.updatePrograms(program);

        } catch (JSONException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        JSONObject json = new JSONObject();

        try {
            json.put("answer", "success");
            json.put("id", lastid);

            Core core = (Core)getServletContext().getAttribute(QuartzListener.CoreClass);
            Program program = core.getProgramFromId(lastid);
            json.put("program", getJsonFromProgram(program).toString());

            // finally output the json string
            out.print(json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void deleteProgram(HttpServletResponse response, String paramID) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        Core core = (Core)getServletContext().getAttribute(QuartzListener.CoreClass);

        JSONObject json = new JSONObject();
        try {
            int id = core.deleteProgram(Integer.valueOf(paramID));
            if (id > 0) {
                json.put("answer", "deleted");
                json.put("id", paramID);
            } else {
                json.put("answer", "error");
            }
            out.print(json.toString());

        } catch (JSONException e) {
            e.printStackTrace();
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    private Program getProgramFromJson(StringBuffer jb) throws JSONException {
        JSONObject jsonObj = new JSONObject(jb.toString());

        Program program = new Program();
        program.id = jsonObj.getInt("id");
        program.name = jsonObj.getString("name");
        program.active = jsonObj.getBoolean("active");
        program.dateEnabled = jsonObj.getBoolean("dateenabled");

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");

        if (!jsonObj.getString("startdate").isEmpty()) {
            String dateInString = jsonObj.getString("startdate");
            try {
                program.startDate = formatter.parse(dateInString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if (!jsonObj.getString("enddate").isEmpty()) {
            String dateInString = jsonObj.getString("enddate");
            try {
                program.endDate = formatter.parse(dateInString);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        if (!jsonObj.getString("starttime").isEmpty()) {
            String timeInString = jsonObj.getString("starttime") + ":00";
            program.startTime = Time.valueOf(timeInString);
        }
        if (!jsonObj.getString("endtime").isEmpty()) {
            String timeInString = jsonObj.getString("endtime") + ":00";
            program.endTime = Time.valueOf(timeInString);
        }

        program.Sunday = jsonObj.getBoolean("sunday");
        program.Monday = jsonObj.getBoolean("monday");
        program.Tuesday = jsonObj.getBoolean("tuesday");
        program.Wednesday = jsonObj.getBoolean("wednesday");
        program.Thursday = jsonObj.getBoolean("thursday");
        program.Friday = jsonObj.getBoolean("friday");
        program.Saturday = jsonObj.getBoolean("saturday");

        JSONArray timeranges = jsonObj.getJSONArray("timeranges");
        for (int i = 0; i < timeranges.length(); i++) {
            JSONObject JSONrange = timeranges.getJSONObject(i);

            TimeRange tr = new TimeRange();
            if (!JSONrange.isNull(("id")))
                tr.ID = JSONrange.getInt("id");
            if (!JSONrange.isNull(("name")))
                tr.name = JSONrange.getString("name");
            if (!JSONrange.isNull(("endtime")))
                tr.endTime = Time.valueOf(JSONrange.getString("endtime") + ":00");
            if (!JSONrange.isNull(("temperature")))
                tr.temperature = JSONrange.getDouble("temperature");
            /*if (!JSONrange.isNull(("sensor")))
                tr.shieldId = JSONrange.getInt("sensor");*/
            if (!JSONrange.isNull(("sensorid")))
                tr.sensorId = JSONrange.getInt("sensorid");
            if (!JSONrange.isNull(("priority")))
                tr.priority = JSONrange.getInt("priority");

            program.mTimeRanges.add(tr);
        }

        return program;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        String paramName = "id";
        String idParamValue = request.getParameter(paramName);
        String activeParamValue = request.getParameter("active");
        String nextParamValue = request.getParameter("next");


        Core core = (Core)getServletContext().getAttribute(QuartzListener.CoreClass);

        if (nextParamValue != null) {

            ArrayList<ActiveProgram> list = core.getNextActiveProgramlist();
            JSONArray jsonarray = new JSONArray();
            try {
                Iterator<ActiveProgram> iterator = list.iterator();
                while (iterator.hasNext()) {
                    ActiveProgram active = iterator.next();

                    JSONObject json = new JSONObject();
                    SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyy HH:mm");

                    json.put("id", active.program.id);
                    json.put("name", active.program.name);
                    json.put("timerangeid", active.timeRange.ID);
                    json.put("timerangename", active.timeRange.name);
                    if (active.startDate != null)
                        json.put("startdate", df.format(active.startDate));
                    if (active.endDate != null)
                        json.put("enddate", df.format(active.endDate));
                    json.put("temperature", active.timeRange.temperature);
                    json.put("sensor", active.timeRange.sensorId);


                    jsonarray.put(json);
                }
                out.print(jsonarray.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else if (idParamValue != null) {

            Program program = core.getProgramFromId(Integer.valueOf(idParamValue));
            if (program == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                try {
                    JSONObject json = getJsonFromProgram(program);
                    out.print(json.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else if (activeParamValue != null) {

            ActiveProgram activeProgram = core.getActiveProgram();
            if (activeProgram == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            } else {
                try {
                    JSONObject json = new JSONObject();
                    if (core.getLastActiveProgramUpdate() != null)
                        json.put("lastupdate", core.getLastActiveProgramUpdate());
                    json.put("programid", activeProgram.program.id);
                    json.put("programname", activeProgram.program.name);
                    json.put("timerangeid", activeProgram.timeRange.ID);
                    json.put("timerangename", activeProgram.timeRange.name);
                    json.put("endtime", activeProgram.timeRange.endTime);
                    json.put("temperature", activeProgram.timeRange.temperature);
                    json.put("sensor", activeProgram.timeRange.sensorId);
                    String sensorName = "local";
                    Double sensorTemperature = 0.0;
                    if (activeProgram.timeRange.sensorId != 0) {
                        SensorBase sensor = core.getSensorFromId(activeProgram.timeRange.sensorId);
                        if(sensorName != null) {
                            try {
                                TemperatureSensor ts = (TemperatureSensor) sensor;
                                sensorName = ts.getName();
                                sensorTemperature = ts.getAvTemperature();
                                json.put("sensorname", sensorName);
                                json.put("sensortemperature", sensorTemperature);
                                out.print(json.toString());
                            } catch (ClassCastException e) {
                                e.printStackTrace();
                            }
                        } else {
                            LOGGER.severe("Sensor name null");
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

        } else {

            ArrayList<Program> list = core.getPrograms();
            JSONArray jsonarray = new JSONArray();
            try {
                Iterator<Program> iterator = list.iterator();
                while (iterator.hasNext()) {
                    Program program = iterator.next();
                    JSONObject json = getJsonFromProgram(program);

                    jsonarray.put(json);
                }
                out.print(jsonarray.toString());

            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
    }

    private JSONObject getJsonFromProgram(Program program) throws JSONException {
        JSONObject json = new JSONObject();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm");
        json.put("id", program.id);
        json.put("active", program.active);
        json.put("name", program.name);
        json.put("dateenabled", program.dateEnabled);
        json.put("startdate", program.startDate);
        if (program.startTime != null)
            json.put("starttime", df.format(program.startTime));
        json.put("enddate", program.endDate);
        if (program.endTime != null)
            json.put("endtime", df.format(program.endTime));
        json.put("sunday", program.Sunday);
        json.put("monday", program.Monday);
        json.put("tuesday", program.Tuesday);
        json.put("wednesday", program.Wednesday);
        json.put("thursday", program.Thursday);
        json.put("friday", program.Friday);
        json.put("saturday", program.Saturday);
        json.put("priority", program.priority);

        JSONArray timerange = new JSONArray();
        Iterator<TimeRange> timeiterator = program.mTimeRanges.iterator();
        while (timeiterator.hasNext()) {
            TimeRange tr = timeiterator.next();

            JSONObject range = new JSONObject();
            range.put("id", tr.ID);
            range.put("name", tr.name);
            if (tr.endTime != null)
                range.put("endtime", df.format(tr.endTime));
            range.put("sensorid", tr.sensorId);
            //range.put("subaddress", tr.subAddress);
            range.put("temperature", tr.temperature);
            range.put("priority", tr.priority);
            timerange.put(range);
        }
        json.put("timeranges", timerange);
        return json;
    }
}
