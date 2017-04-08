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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
//@WebServlet(name = "SensorServlet")
public class CommandServlet extends HttpServlet {

    String temperaturepin = "D4";
    boolean temperaturesensorenabled = true;
    private class sensor {
        String addr = "";
        String name = "";
    }

    private List sensors = new ArrayList();

    private static final Logger LOGGER = Logger.getLogger(CommandServlet.class.getName());




    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String id = request.getParameter("id");

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        String json = "{\"id\":0,\"temperaturesensorsenabled\":true,\"temperaturesensorspin\":\"D4\",\"sensors\":[{\"temperature\":23.60,\"avtemperature\":23.79,\"name\":\"sensor0\",\"type\":\"temperature\",\"addr\":\"28:11:1E:08:05:00:00:85\"},{\"temperature\":23.70,\"avtemperature\":23.85,\"name\":\"sensor1\",\"type\":\"temperature\",\"addr\":\"28:FF:7B:43:92:15:04:28\"}]}";
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


        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);

            JSONObject json = new JSONObject(jb.toString());
            if (json.has("command")) {
                String command = json.getString("command");
                if (command.equals("power")) {
                    JSONObject jsonResult = new JSONObject();
                    jsonResult.put("answer", "success");
                    String status = "";
                    if (json.has("status"))
                        status = json.getString("status");
                    jsonResult.put("power", status);
                    out.print(jsonResult.toString());
                    return;
                } else if (command.equals("temperaturesensorsettings")) {
                    JSONObject jsonResult = new JSONObject();
                    jsonResult.put("answer", "success");
                    if (json.has("temperaturesensorsenabled")) {
                        boolean enabled = json.getBoolean("temperaturesensorsenabled");
                        jsonResult.put("temperaturesensorenabled", enabled);
                    }
                    if (json.has("temperaturepin")) {
                        String pin = json.getString("temperaturepin");
                        jsonResult.put("temperaturepin", pin);
                    }
                    out.print(jsonResult.toString());
                    return;
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        JSONObject jsonResult = new JSONObject();


        response.setStatus(HttpServletResponse.SC_OK);
        try {
            jsonResult.put("answer", "success");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        out.print(jsonResult.toString());

    }
}
