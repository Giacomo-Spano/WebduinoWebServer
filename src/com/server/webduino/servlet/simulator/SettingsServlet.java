package com.server.webduino.servlet.simulator;

import com.server.webduino.core.*;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
//@WebServlet(name = "SensorServlet")
public class SettingsServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(SettingsServlet.class.getName());

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String id = request.getParameter("id");

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        String json = "{\"localport\":3584,\"shieldname\":\"192.168.1.3\",\"ssid\":\"TP-LINK_3BD796\",\"password\":\"giacomocasa\",\"servername\":\"192.168.1.3\",\"serverport\":8080,\"localip\":\"192.168.1.9\",\"macaddress\":\"18:fe:34:d4:c6:87\",\"shieldid\":32,\"datetime\":\"15-02-2017 20:54:08\",\"power\":\"on\",\"lastrestart\":\"15-02-2017 20:51:14\",\"heap\":\"26944\",\"swversion\":\"1.02\"}";
        out.print(json);
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
