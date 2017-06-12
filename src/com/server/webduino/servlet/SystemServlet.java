package com.server.webduino.servlet;

import com.quartz.QuartzListener;
import com.server.webduino.core.Core;
import com.server.webduino.core.Shield;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.DoorSensorCommand;
import com.server.webduino.core.webduinosystem.programinstructions.ProgramInstructions;
import com.server.webduino.core.webduinosystem.scenario.Scenario;
import com.server.webduino.core.webduinosystem.zones.Zone;
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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

//import com.server.webduino.core.SensorData;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */

//@WebServlet(name = "SensorServlet")
public class SystemServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SystemServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOGGER.info("SystemServlet:doPost");

        String data = request.getParameter("data");

        PrintWriter out = response.getWriter();
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        JSONObject jsonResponse;

        StringBuffer jb = new StringBuffer();
        String line;

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
            JSONObject json = new JSONObject(jb.toString());
            if(data != null && data.equals("scenario")) {
                if (!Core.saveScenario(json)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    jsonResponse = new JSONObject();
                    try {
                        json.put("result", "failed");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // finally output the json string
                    out.print(jsonResponse.toString());
                }

            } else if(data != null && data.equals("zone")) {
                if (!Core.saveZone(json)) {
                    response.setStatus(HttpServletResponse.SC_OK);
                    jsonResponse = new JSONObject();
                    try {
                        json.put("result", "failed");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    // finally output the json string
                    out.print(jsonResponse.toString());
                }

            }

            boolean res = false;


        } catch (JSONException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.severe("BAD REQUEST");
            return;
        }

        response.setStatus(HttpServletResponse.SC_OK);
        JSONObject json = new JSONObject();
        try {
            json.put("result", "success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // finally output the json string
        out.print(json.toString());
    }


    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String id = request.getParameter("id");
        String requestCommand = request.getParameter("requestcommand");

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);

        if (requestCommand != null && requestCommand.equals("sensors")) {
            JSONArray jarray = Core.getSensorsJSONArray();
            if (jarray != null)
                out.print(jarray.toString());

        } else if (requestCommand != null && requestCommand.equals("scenarios")) {

            JSONArray jarray = Core.getScenariosJSONArray();
            if (jarray != null)
                out.print(jarray.toString());

        } else if (requestCommand != null && requestCommand.equals("zones")) {

            JSONArray jarray = Core.getZonesJSONArray();
            if (jarray != null)
                out.print(jarray.toString());

        } else if (requestCommand != null && requestCommand.equals("zone") && id != null) {
            int zoneid = Integer.parseInt(id);
            Zone zone = Core.getZoneFromId(zoneid);
            if (zone != null)
                out.print(zone.toJSON());
        } else if (requestCommand != null && requestCommand.equals("shields")) {
            List<Shield> list = core.getShields();
            //create Json Object
            JSONArray jsonarray = new JSONArray();
            Iterator<Shield> iterator = list.iterator();
            while (iterator.hasNext()) {
                Shield shield = iterator.next();
                JSONObject json = shield.getJson();
                jsonarray.put(json);
            }
            if (jsonarray != null)
                out.print(jsonarray.toString());
        } else if (requestCommand != null && requestCommand.equals("scenario") && id != null) {
            int scenarioid = Integer.parseInt(id);
            Scenario scenario = Core.getScenarioFromId(scenarioid);
            if (scenario != null)
                out.print(scenario.toJSON());
        } else if (requestCommand != null && requestCommand.equals("instructions") && id != null) {
            String scenarioid = request.getParameter("scenarioid");
            JSONArray jsonArray = new JSONArray();
            if (scenarioid != null) {
                int sid = Integer.parseInt(scenarioid);
                List<ProgramInstructions> programInstructions = Core.getProgramInstructions(sid);

                for (ProgramInstructions instruction : programInstructions) {
                    jsonArray.put(instruction.toJson());
                }
            }
            if (jsonArray != null)
                out.print(jsonArray.toString());
        }
    }
}
