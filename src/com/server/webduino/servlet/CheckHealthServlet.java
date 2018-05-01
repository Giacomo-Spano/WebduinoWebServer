package com.server.webduino.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
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
            out.println("{\"zonesensorstatus\" : \"alive\" }");
            response.setStatus(HttpServletResponse.SC_OK);
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
