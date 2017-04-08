package com.server.webduino;
import com.quartz.QuartzListener;
import com.server.webduino.core.Core;
import com.server.webduino.core.Program;
import com.server.webduino.core.TimeRange;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

// Loading required libraries


/**
 * Created by Giacomo Span� on 07/11/2015.
 */
public class Servlet extends javax.servlet.http.HttpServlet {
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }

    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    //static final String DB_URL = "jdbc:mysql://192.168.1.100/foo";
    static final String DB_URL = "jdbc:mysql://127.0.0.1:3306/foo";
    //static final String DB_URL = "jdbc:mysql://localhost/foo";

    //  Database credentials
    static final String USER = "root";
    static final String PASS = "giacomo";

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        /*PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<h1>Hello Servlet Get</h1>");
        out.println("</body>");

        out.println("</html>");*/




        // Set response content type
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String title = "Database Result";
        String docType =
                "<!doctype html public \"-//w3c//dtd html 4.0 " +
                        "transitional//en\">\n";
        out.println(docType +
                "<html>\n" +
                "<head><title>" + title + "</title></head>\n" +
                "<body bgcolor=\"#f0f0f0\">\n" +
                "<h1 align=\"center\">" + title + "</h1>\n");


        //Programs prgms = new Programs();
        //prgms.read();
        Core core = (Core)getServletContext().getAttribute(QuartzListener.CoreClass);
        ArrayList<Program> programs = core.mPrograms.getProgramList();

        for (int i=0; i < programs.size(); i++) {

            Program program = programs.get(i);
            out.println("id:"+ program.id + " start:"+ program.startDate + " " + program.startTime +
                    " end:"+ program.endDate + " " + program.endTime +
                    " S" + program.Sunday + " M"+ program.Monday +
                    " TU" + program.Tuesday + " WE"+ program.Wednesday +
                    " THS" + program.Thursday + " FR"+ program.Friday +
                    " SA" + program.Saturday  + "<br>");

            for (int k= 0; k < program.mTimeRanges.size(); k++) {

                TimeRange tr = program.mTimeRanges.get(k);
                out.println("-> id:"+ tr.ID + " end:"+ tr.endTime +
                        " temperature" + tr.temperature + " sensor" + tr.sensorId + "<br>");

            }
        }



    }
}

