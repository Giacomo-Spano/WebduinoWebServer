package com.server.webduino.core.virtual;

import com.server.webduino.core.Core;
import com.server.webduino.core.SimpleMqttClient;

import java.util.UUID;

/**
 * Created by gs163400 on 09/06/2018.
 */
public class VirtualShield implements Runnable {
    private String responseTopic = "";
    private SimpleMqttClient smc;
    private String MACAddress;

    public VirtualShield() {
        MACAddress = UUID.randomUUID().toString();
        responseTopic = "fromServer/shield/" + MACAddress + "/settings";
    }

    @Override
    public void run() {


        smc = new SimpleMqttClient(UUID.randomUUID().toString());
        smc.runClient(Core.getMQTTUrl(), Core.getMQTTPort(), Core.getMQTTUser(), Core.getMQTTPassword());
        smc.subscribe(responseTopic);
        smc.addListener(new SimpleMqttClient.SimpleMqttClientListener() {
            @Override
            public synchronized void messageReceived(String topic, String message) {

            }

            @Override
            public void connectionLost() {

            }
        });

        loadSettings();

    }

    private void loadSettings() {
        String topic = "toServer/shield/loadsettings";
        String message = MACAddress;
        smc.publish(topic,message);
    }
}
