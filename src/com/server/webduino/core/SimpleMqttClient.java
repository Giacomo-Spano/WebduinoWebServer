package com.server.webduino.core;

/**
 * Created by giaco on 25/04/2017.
 */

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SimpleMqttClient implements MqttCallback {

    private static final Logger LOGGER = Logger.getLogger(Core.class.getName());

    public SimpleMqttClient() {


    }

    MqttClient myClient;
    MqttConnectOptions connOpt;

    String BROKER_URL = "";//tcp://192.168.1.41:1883";
    //String BROKER_URL_TEST = "tcp://192.168.1.3:1883";

    private String clientId = "WebserverClient";
    static final String M2MIO_USERNAME = "giacomo";
    static final String M2MIO_PASSWORD_MD5 = "giacomo";

    public SimpleMqttClient(String clientId) {
        this.clientId = clientId;
    }

    public interface SimpleMqttClientListener {

        void messageReceived(String topic, String message);
        void connectionLost();
    }

    protected List<SimpleMqttClientListener> listeners = new ArrayList<>();

    public void addListener(SimpleMqttClientListener toAdd) {
        listeners.add(toAdd);
    }


    @Override
    public void connectionLost(Throwable t) {
        System.out.println("Connection lost!");
        // code to reconnectCoreMQTTClient to the broker would go here if desired

        //connect();

        //Core core = (Core) getServletContext().getAttribute(QuartzListener.CoreClass);
        //core.init();

        for (SimpleMqttClientListener listener : listeners) {
            if (listener != null)
                listener.connectionLost();
        }

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) /*throws Exception*/ {

        System.out.println("clientid " + clientId + " messageArrived \"" + topic +  " mqttMessage " + mqttMessage);

        try {
            String payloadMessage = new String(mqttMessage.getPayload());

            synchronized (listeners) {
            if (listeners != null) {
                for (SimpleMqttClientListener listener : listeners) {

                        if (listener != null)
                            listener.messageReceived(topic, payloadMessage);
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.info("messageArrived error: " + e.toString());
            e.printStackTrace();
        }

    }

    /*public boolean runClient(String mqttUrl, long mqttPort, String mqttUser, String mqttPassword) {
        return runClient(Core.getMQTTUrl(),Core.getMQTTPort(), Core.getMQTTUser(), Core.getMQTTPassword());
    }*/

    public boolean runClient(String serveraddress, long serverport, String user, String password) {
        // setup MQTT Client
        double x = Math.random();
        String str = ("" + x).replace("0.","-");
        String clientID = clientId + str;//M2MIO_THING;
        connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(600);
        connOpt.setUserName(/*M2MIO_USERNAME*/user);
        connOpt.setPassword(/*M2MIO_PASSWORD_MD5.toCharArray()*/password.toCharArray());

        // Connect to Broker
        try {
            //String tmpDir = ("c:\\scratch");
            String tmpDir = System.getProperty("java.io.tmpdir");

            // questo fa schifo. Da cambiare
            // in base al valore della var java.io.tmpdir capisce se è su linux o su windows e cambia il path del file temp
            //if (tmpDir.equals("C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\temp"))
            if (!Core.isProduction())
                tmpDir = System.getenv("tmp");
            else
                tmpDir = System.getProperty("java.io.tmpdir");

            String debugenv = System.getenv("debugenvironment");
            if (debugenv != null && debugenv.equals("true"))
                tmpDir = "c:\\scratch";

            MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);
            myClient = new MqttClient(/*"tcp://localhost:1883"*/"tcp://" + serveraddress + ":" + serverport, clientID, dataStore);
            myClient.setCallback(this);
            myClient.connect(connOpt);
        } catch (MqttException e) {
            e.printStackTrace();
            //System.exit(-1);
            return false;
        }

        System.out.println("Connected to " + BROKER_URL);
        return true;
    }


    public boolean publish(String myTopic, String pubMsg) {

        MqttTopic topic = myClient.getTopic(myTopic);

        int pubQoS = 0;
        MqttMessage message = new MqttMessage(pubMsg.getBytes());
        message.setQos(pubQoS);
        message.setRetained(false);

        // Publish the message
        System.out.println("Publishing to topic: \"" + topic + "\" qos: " + pubQoS + " message: \"" + message + "\"");
        MqttDeliveryToken token = null;
        try {
            // publish message to broker
            /*if(!myClient.isConnected())
                myClient.connect();*/
            token = topic.publish(message);
            // Wait until the message has been delivered to the broker
            //token.waitForCompletion();
            //Thread.sleep(100);
        } catch (Exception e) {
            e.printStackTrace();
            connect();
            return false;
        }

        return true;
    }

    public boolean subscribe(String myTopic) {

        // subscribe to topic if subscriber
        try {
            int subQoS = 0;
            myClient.subscribe(myTopic, subQoS);
        } catch (Exception e) {
            e.printStackTrace();
            connect();
            return false;
        }
        return true;
    }

    public boolean unsubscribe(String myTopic) {

        // subscribe to topic if subscriber
        try {
            myClient.unsubscribe(myTopic);
        } catch (Exception e) {
            e.printStackTrace();
            connect();
            return false;
        }
        return true;
    }

    public void disconnect() {
        // disconnect
        try {
            // wait to ensure subscribed messages are delivered
            //if (subscriber) {
            //Thread.sleep(5000);
            //}
            myClient.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isConnected() {
        // disconnect
        return myClient.isConnected();
    }

    public void connect() {
        // disconnect
        try {
            myClient.connect();

            if (!isConnected()) {
                ;
            }
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}