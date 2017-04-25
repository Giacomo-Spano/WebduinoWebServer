package com.server.webduino.core;

import com.quartz.QuartzListener;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;

import javax.servlet.ServletContext;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 15/02/2016.
 */
public class MQTTThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(MQTTThread.class.getName());

    private Core coreref;
    private boolean receive;

    private boolean quietMode = false;
    private String sendtopic = "toServer/#";
    private String message = "Message from async callback Paho MQTTv3 Java client sample";
    private int qos = 2;
    private String broker = "192.168.1.3";
    private int port = 1883;
    private String clientIdReceive = "WebduinoclientReceive";
    private String clientIdSend = "WebduinoclientSend";
    private boolean cleanSession = true; // Non durable subscriptions
    private boolean ssl = false;
    private String password = null;
    private String userName = null;
    private String protocol = "tcp://";

    private SampleAsyncCallBack sampleClient;

    public MQTTThread(Core core, boolean receive) {
        super("str");
        this.coreref = core;
        this.receive = receive;
    }

    public void run() {

        /*SimpleMqttClient smc = new SimpleMqttClient();
        smc.runClient();*/



        try {
            String url = protocol + broker + ":" + port;
        //if (receive)
            sampleClient = new SampleAsyncCallBack(url, clientIdReceive, cleanSession, quietMode, userName,password);
        /*else
            sampleClient = new SampleAsyncCallBack(url, clientIdSend, cleanSession, quietMode, userName,password);*/

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

        sampleClient.addListener(coreref);
        //if (receive)
            initMQTTReceive();


    }

    public void initMQTTReceive() {
        try {
            // Create an instance of the Sample client wrapper
            /*SampleAsyncCallBack sampleClient = new SampleAsyncCallBack(url, clientId, cleanSession, quietMode, userName,
                    password);*/

            //sampleClient.addListener(coreref);
            //sampleClient.publish(receivetopic, qos, message.getBytes());
            sampleClient.subscribe(sendtopic, qos);

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

    /*public void publish(String topic, String message) {
        try {
            sampleClient.publish(receivetopic, qos, message.getBytes());


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

    }*/

}

