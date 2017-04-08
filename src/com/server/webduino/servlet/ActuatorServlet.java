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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
//@WebServlet(name = "SensorServlet")
public class ActuatorServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ActuatorServlet.class.getName());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String id = request.getParameter("id");

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);

        if (id != null) {

            Actuator actuator = core.getFromShieldId(Integer.valueOf(id), null);
            JSONObject json = actuator.getJson();
            out.print(json.toString());

        } else {

            ArrayList<Actuator> list = core.getActuators();
            //create Json Object
            JSONArray jsonarray = new JSONArray();
            Iterator<Actuator> iterator = list.iterator();
            while (iterator.hasNext()) {
                Actuator actuator = iterator.next();
                JSONObject json = actuator.getJson();
                jsonarray.put(json);
            }
            out.print(jsonarray.toString());
        }

    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        //questa servlet riceve command dalla app, dalle pagine wed e riceve status update dagli actuator diorettamente

        StringBuffer jb = new StringBuffer();
        String line = null;
        int shieldId;
        String subaddress;

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        JSONObject jsonResult = new JSONObject();

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

            boolean res = false;

            if (json.has("event") && json.getString("event").equals("update")) { // receive status update

                if (json.has("actuator")) {
                    JSONObject jsonActuator = json.getJSONObject("actuator");
                    if (!jsonActuator.has("shieldid") || !jsonActuator.has("addr"))
                        return;
                    shieldId = jsonActuator.getInt("shieldid");
                    subaddress = jsonActuator.getString("addr");
                    out.print(json.toString());
                    new UpdateActuatorThread(getServletContext(), jsonActuator).start();
                    response.setStatus(HttpServletResponse.SC_OK);
                }
                return;

            } else if (json.has("command")) {

                if (!json.has("actuatorid")) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                int actuatorId = json.getInt("actuatorid");
                Actuator actuator = Core.getActuatorFromId(actuatorId);
                ActuatorCommand cmd = actuator.getCommandFromJson(json);
                if (cmd == null) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    return;
                }
                new SendActuatorCommandThread(actuatorId, cmd).start();

                response.setStatus(HttpServletResponse.SC_OK);
                jsonResult.put("answer", "success");
                out.print(jsonResult.toString());

            }

        } catch (JSONException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.severe("BAD REQUEST");
            return;
        }
    }

}
