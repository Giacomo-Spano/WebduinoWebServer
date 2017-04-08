package com.server.webduino.servlet;

import com.quartz.QuartzListener;
import com.server.webduino.core.Core;
import com.server.webduino.core.Shield;
import com.server.webduino.core.Shields;
import com.server.webduino.core.TemperatureSensor;
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
import java.util.List;
import java.util.logging.Logger;

//import com.server.webduino.core.SensorData;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
//@WebServlet(name = "SensorServlet")
public class TimeServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(TimeServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOGGER.info("TimeServlet:doGet");

        StringBuffer jb = new StringBuffer();

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
        //create Json Response Object
        JSONObject jsonResponse = new JSONObject();

        try {

            jsonResponse.put("result", "success");

            Date date = Core.getDate();
            SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            jsonResponse.put("date", df.format(date));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int tzOffsetSec = (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)) / (1000);
            jsonResponse.put("timesec", date.getTime() / 1000 + tzOffsetSec);


        } catch (JSONException e) {
            try {
                jsonResponse.put("result", "error");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
        }
        // finally output the json string
        out.print(jsonResponse.toString());
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


    }
}
