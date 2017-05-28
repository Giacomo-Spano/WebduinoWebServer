package com.server.webduino.core;

/**
 * Created by giaco on 25/04/2017.
 */

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MqttDefaultFilePersistence;

import java.util.ArrayList;
import java.util.List;

public class SimpleMqttClient implements MqttCallback {


    public SimpleMqttClient() {


    }

    MqttClient myClient;
    MqttConnectOptions connOpt;

    String BROKER_URL = "";//tcp://192.168.1.41:1883";
    //String BROKER_URL_TEST = "tcp://192.168.1.3:1883";

    private String clientId = "WebserverClient";
    static final String M2MIO_USERNAME = "";
    static final String M2MIO_PASSWORD_MD5 = "";

    public SimpleMqttClient(String clientId) {
        this.clientId = clientId;
    }

    public interface SimpleMqttClientListener {

        void messageReceived(String topic, String message);
    }

    protected List<SimpleMqttClientListener> listeners = new ArrayList<>();

    public void addListener(SimpleMqttClientListener toAdd) {
        listeners.add(toAdd);
    }


    @Override
    public void connectionLost(Throwable t) {
        System.out.println("Connection lost!");
        // code to reconnect to the broker would go here if desired
        connect();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) /*throws Exception*/ {

        try {
            String payloadMessage = new String(mqttMessage.getPayload());
            for (SimpleMqttClientListener listener : listeners) {
                listener.messageReceived(topic, payloadMessage);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void runClient() {
        // setup MQTT Client
        String clientID = clientId;//M2MIO_THING;
        connOpt = new MqttConnectOptions();
        connOpt.setCleanSession(true);
        connOpt.setKeepAliveInterval(600);
        //connOpt.setUserName(M2MIO_USERNAME);
        //connOpt.setPassword(M2MIO_PASSWORD_MD5.toCharArray());

        // Connect to Broker
        try {
            //String tmpDir = ("c:\\scratch");
            String tmpDir = System.getProperty("java.io.tmpdir");

            // questo fa schifo. Da cambiare
            // in base al valore della var java.io.tmpdir capisce se è su linux o su windows e cambia il path del file temp
            //if (tmpDir.equals("C:\\Program Files\\Apache Software Foundation\\Tomcat 9.0\\temp"))
            if(!Core.isProduction())
                tmpDir = System.getenv("tmp");
            else
                tmpDir = System.getProperty("java.io.tmpdir");
            //String tmpDir = System.getenv("tmp");
            //System.out.println("GIACOMO --xx-- tmpDir = " + tmpDir);
            MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);


            if(!Core.isProduction())
                //myClient = new MqttClient("tcp://192.168.1.3:1883", clientID, dataStore);
                myClient = new MqttClient("tcp://localhost:1883", clientID, dataStore);
            else
                myClient = new MqttClient("tcp://192.168.1.41:1883", clientID, dataStore);
            //myClient = new MqttClient(BROKER_URL, clientID, dataStore);

            myClient.setCallback(this);
            myClient.connect(connOpt);
        } catch (MqttException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        System.out.println("Connected to " + BROKER_URL);


    }


    public boolean publish(String myTopic, String pubMsg) {

        MqttTopic topic = myClient.getTopic(myTopic);

        int pubQoS = 0;
        MqttMessage message = new MqttMessage(pubMsg.getBytes());
        message.setQos(pubQoS);
        message.setRetained(false);

        // Publish the message
        System.out.println("Publishing to topic \"" + topic + "\" qos " + pubQoS);
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