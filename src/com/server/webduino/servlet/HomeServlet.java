package com.server.webduino.servlet;
import com.server.webduino.core.Core;
//import com.server.webduino.core.SensorData;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.server.webduino.core.SampleAsyncCallBack;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Giacomo Span� on 07/11/2015.
 */
//@WebServlet(name = "HomeServlet")
public class HomeServlet extends HttpServlet  {




    // Run a simple task once every 5, starting 1 seconds from now.
    // Cancel the task after 60 seconds.
    //static AlarmClock sensorAlarmClock;// = new AlarmClock(1, 5, 60);

    public void init(ServletConfig servletConfig) throws ServletException{
        //this.myParam = servletConfig.getInitParameter("myParam");
        //sensorAlarmClock = new AlarmClock(1, 5, 600);
        //sensorAlarmClock.activateAlarmThenStop();

		//prova2();

	}




	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        Date date = Core.getDate();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        String str = dateFormat.format(date);

        /*TemperatureSensor sensors;
        URL remoteURL1 = new URL("http://"+"192.168.1.95:80"); // studio
        sensors = new TemperatureSensor(remoteURL1);
        String txt = sensors.requestStatusUpdate();*/
        //SensorData data = Shields.get(0);

        PrintWriter out = response.getWriter();
        out.println("<html>");
        out.println("<body>");
        out.println("<h1>Hello Servlet Get ver 1.0</h1>");
        out.println(str);

        out.print("temperatura: ");
        //out.println(data.temperature);


        out.println("</body>");
        out.println("</html>");



    }



}
