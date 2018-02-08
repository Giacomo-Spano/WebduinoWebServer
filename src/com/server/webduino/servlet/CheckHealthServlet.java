package com.server.webduino.servlet;

import com.quartz.QuartzListener;
import com.server.webduino.core.Core;
import com.server.webduino.core.Shield;
import com.server.webduino.core.Triggers;
import com.server.webduino.core.datalog.DataLog;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.SensorFactory;
import com.server.webduino.core.webduinosystem.scenario.*;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramAction;
import com.server.webduino.core.webduinosystem.scenario.actions.ProgramActionFactory;
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
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

//import com.server.webduino.core.SensorData;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */


public class CheckHealthServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CheckHealthServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            LOGGER.info("CheckHealthServlet");

            PrintWriter out = response.getWriter();
            out.println("{\"status\" : \"alive\" }");
            response.setStatus(HttpServletResponse.SC_OK);
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
