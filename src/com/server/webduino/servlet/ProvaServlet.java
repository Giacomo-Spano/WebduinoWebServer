package com.server.webduino.servlet;

import com.server.webduino.core.Core;
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
import java.util.logging.Logger;

//import com.server.webduino.core.SensorData;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
//@WebServlet(name = "SensorServlet")
public class ProvaServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ProvaServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOGGER.info("TimeServlet:doGet");

        StringBuilder buffer = new StringBuilder();
        BufferedReader reader = request.getReader();
        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }
        String data = buffer.toString();

        String activeParamValue = request.getParameter("status");

        response.setContentType("text/xml");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();

        response.setStatus(HttpServletResponse.SC_OK);

        String str = "<result><rele>0</rele><status>1</status></result>";
        out.print(str);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    }
}
