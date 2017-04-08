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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Logger;

//import com.server.webduino.core.SensorData;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
//@WebServlet(name = "SensorServlet")
public class ShieldServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ShieldServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOGGER.info("SensorServlet:doPost");


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

            boolean res = false;

            if (json.has("event")) {
                if (json.getString("event").equals("register")) { // receive status update
                    if (json.has("shield")) {
                        JSONObject jsonShield = json.getJSONObject("shield");

                        jsonResponse = handleRegisterEvent(jsonShield);
                        out.print(jsonResponse.toString());
                        response.setStatus(HttpServletResponse.SC_OK);
                    }
                    return;
                } else if (json.getString("event").equals("restart")) { // receive status update
                    jsonResponse = handleRestartEvent();
                    out.print(jsonResponse.toString());
                    response.setStatus(HttpServletResponse.SC_OK);
                    return;
                }
            }

        } catch (JSONException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.severe("BAD REQUEST");
            return;
        }

        out.print("");
    }

    private JSONObject handleErrorEvent(StringBuffer jb) {

        return null;
    }

    private JSONObject handleRestartEvent() {
        String description = "Shield restart";
        Core.sendPushNotification(SendPushMessages.notification_restarted, "Restart", description, "0", 0);
        return null;
    }

    private JSONObject handleRegisterEvent(JSONObject jsonObj) {

        JSONObject jsonResponse;//create Json Response Object
        jsonResponse = new JSONObject();

        try {
            //JSONObject jsonObj = new JSONObject(jb.toString());

            LOGGER.info("SensorServlet:doPost" + jsonObj.toString());

            int id = registerShield(jsonObj);
            // put some value pairs into the JSON object .
            try {
                jsonResponse.put("result", "success");
                jsonResponse.put("id", id);

                Date date = Core.getDate();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                jsonResponse.put("date", df.format(date));
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                int tzOffsetSec = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (1000);
                jsonResponse.put("timesec", date.getTime() / 1000 + tzOffsetSec);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (JSONException e) {
            try {
                jsonResponse.put("result", "error");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        return jsonResponse;
    }

    private int registerShield(JSONObject jsonObj) throws JSONException {

        Shield shield = new Shield(/*jsonObj*/);
        if (!shield.FromJson(jsonObj))
            return 0;

        int shieldid = Core.registerShield(shield);
        return shieldid;
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String id = request.getParameter("id");

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);

        if (id != null) {

            /*Actuator actuator = core.getFromShieldId(Integer.valueOf(id), null);
            JSONObject json = actuator.getJson();
            out.print(json.toString());*/

        } else {

            List<Shield> list = core.getShields();
            //create Json Object
            JSONArray jsonarray = new JSONArray();
            Iterator<Shield> iterator = list.iterator();
            while (iterator.hasNext()) {
                Shield shield = iterator.next();
                JSONObject json = shield.toJson();
                jsonarray.put(json);
            }
            out.print(jsonarray.toString());
        }

    }
}


