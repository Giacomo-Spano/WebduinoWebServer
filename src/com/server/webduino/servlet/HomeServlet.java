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

	public void prova2() {

		// Default settings:
		boolean quietMode = false;
		String action = "publish";
		String topic = "messaggio1";
		String message = "Message from async callback Paho MQTTv3 Java client sample";
		int qos = 2;
		String broker = "192.168.1.3";
		int port = 1883;
		String clientId = "Webduinoclient";
		String subTopic = "Sample/#";
		String pubTopic = "Sample/Java/v3";
		boolean cleanSession = true; // Non durable subscriptions
		boolean ssl = false;
		String password = null;
		String userName = null;

		String protocol = "tcp://";
		String url = protocol + broker + ":" + port;

		try {
			// Create an instance of the Sample client wrapper
			SampleAsyncCallBack sampleClient = new SampleAsyncCallBack(url, clientId, cleanSession, quietMode, userName,
					password);
			
		//sampleClient.publish(topic, qos, message.getBytes());
		sampleClient.subscribe(topic, qos);

			// Perform the specified action
			/*if (action.equals("publish")) {
				sampleClient.publish(topic, qos, message.getBytes());
			} else if (action.equals("subscribe")) {
				sampleClient.subscribe(topic, qos);
			}*/
		} catch (MqttException me) {
			// Display full details of any exception that occurs
			System.out.println("reason " + me.getReasonCode());
			System.out.println("msg " + me.getMessage());
			System.out.println("loc " + me.getLocalizedMessage());
			System.out.println("cause " + me.getCause());
			System.out.println("excep " + me);
			me.printStackTrace();
		} catch (Throwable th) {
			System.out.println("Throwable caught " + th);
			th.printStackTrace();
		}

	}

	public void prova() {
		String topic = "messaggio1";// "MQTT Examples";
		String content = "Message from MqttPublishSample";
		int qos = 2;
		// String broker = "tcp://iot.eclipse.org:1883";
		String broker = "tcp://127.1.1.1:1883";
		String clientId = "JavaSample";
		MemoryPersistence persistence = new MemoryPersistence();

		try {
			MqttClient sampleClient = new MqttClient(broker, clientId, persistence);
			MqttConnectOptions connOpts = new MqttConnectOptions();
			connOpts.setCleanSession(true);
			System.out.println("Connecting to broker: " + broker);
			sampleClient.connect(connOpts);
			System.out.println("Connected");
			System.out.println("Publishing message: " + content);
			MqttMessage message = new MqttMessage(content.getBytes());
			message.setQos(qos);
			sampleClient.publish(topic, message);
			System.out.println("Message published");
			sampleClient.disconnect();
			System.out.println("Disconnected");
			System.exit(0);
		} catch (MqttException me) {
			System.out.println("reason " + me.getReasonCode());
			System.out.println("msg " + me.getMessage());
			System.out.println("loc " + me.getLocalizedMessage());
			System.out.println("cause " + me.getCause());
			System.out.println("excep " + me);
			me.printStackTrace();
		}
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
