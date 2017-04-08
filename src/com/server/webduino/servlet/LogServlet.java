package com.server.webduino.servlet;

import com.server.webduino.core.Core;
import com.server.webduino.core.Shield;
import com.server.webduino.core.Shields;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

//import com.server.webduino.core.SensorData;

/**
 * Created by Giacomo Span� on 08/11/2015.
 */
//@WebServlet(name = "SensorServlet")
public class LogServlet extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LogServlet.class.getName());

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        LOGGER.info("Logger:doPost");

        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        StringBuffer jb = new StringBuffer();
        String line = null;

        try {
            BufferedReader reader = request.getReader();
            while ((line = reader.readLine()) != null)
                jb.append(line + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            try {
                jsonResponse.put("result", "error");
            } catch (JSONException e1) {
                e1.printStackTrace();
            }
            return;
        }

        String text = jb.toString();


        try {

            String log = "";
            int id, packetnumber;
            int start = text.indexOf(":");
            if (start >= 0) {
                text = text.substring(start + 1);
                int end = text.indexOf(":");
                if (end > 0) {
                    packetnumber = Integer.valueOf(text.substring(0, end));
                    text = text.substring(end + 1);
                    //start = text.indexOf(":");
                    end = text.indexOf(":");
                    if (end > 0) {
                        id = Integer.valueOf(text.substring(0, end));
                        text = text.substring(end + 1);

                        String tempDir = getServletContext().getAttribute(ServletContext.TEMPDIR).toString();
                        String fileName = "NODE" + id;

                        writelog(fileName, text);
                        try {
                            jsonResponse.put("result", "success");
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            } else {
                // put some value pairs into the JSON object .

                try {
                    jsonResponse.put("result", "error");
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // finally output the json string
        out.print(jsonResponse.toString());

    }

    private void writelog(String filename, String log) throws JSONException {

        String tempDir = getServletContext().getAttribute(ServletContext.TEMPDIR).toString();//"/temp";
        String file = tempDir + "/" + filename + ".log";

        File f = new File(file);

        if (f.length() > 5000000) {

            SimpleDateFormat df = new SimpleDateFormat("-yyyyMMddHHmmss");
            String newfilename = tempDir + "/" + filename + df.format(Core.getDate()) + ".log";
            f.renameTo(new File(newfilename));
            f = new File(file);
        }



        try (PrintWriter output = new PrintWriter(new FileWriter(f, true))) {
            output.printf("%s", log);
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }
}
