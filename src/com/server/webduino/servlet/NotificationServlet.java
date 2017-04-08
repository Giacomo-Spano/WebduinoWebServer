package com.server.webduino.servlet;

import com.google.android.gcm.server.Message;
import com.server.webduino.core.Core;
import com.server.webduino.core.Notification;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

/**
 * Created by Giacomo Span� on 13/11/2015.
 */


//@WebServlet(name = "NotificationServlet")
public class NotificationServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NotificationServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOGGER.info("notification request");

        String ip = request.getRemoteAddr();
        StringBuffer requestURL = request.getRequestURL();
        if (request.getQueryString() != null) {
            requestURL.append("?").append(request.getQueryString());
        }
        String completeURL = requestURL.toString();

        StringBuffer jb = new StringBuffer();
        String line = null;

        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line);
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }


        Core.sendPushNotification("type","title","description","value",0);

        /*Notification notification = new Notification();

        java.util.Date date = Core.getDate();
        notification.writelog(date,.0,.0,false,0, jb.toString());

        try {
            JSONObject jsonObj = new JSONObject(jb.toString());

            int id = jsonObj.getInt("id");
            String type = "" + jsonObj.getInt("type");
            int value = jsonObj.getInt("value");

            LOGGER.info("notification- id=" + id + "type=" + type+ "value=" + value);

            String title = "notifica";
            Core.sendPushNotification(type,title,"description",""+value);


        } catch (JSONException e) {
            e.printStackTrace();
        }*/


        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("<HTML>");
        out.println("<HEAD><TITLE>Hello World</TITLE></HEAD>");
        out.println("<BODY>");
        out.println("result=1");
        out.println("</BODY></HTML>");
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
