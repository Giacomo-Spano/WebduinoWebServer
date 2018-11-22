package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.Core;
import com.server.webduino.core.Shield;
import com.server.webduino.core.SimpleMqttClient;
import com.server.webduino.core.datalog.CommandDataLog;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 29/12/2016.
 */
public class Command {

    public CommandDataLog commandDataLog = null;

    private static final Logger LOGGER = Logger.getLogger(Command.class.getName());
    public String command;
    public String uuid;
    public int shieldid;
    public int actuatorid;
    public boolean success = false;
    public String result = "";

    //public CommandResult result;
    CommandThread commandThread;

    public Command(int shieldid, int actuatorid) {
        this.shieldid = shieldid;
        this.actuatorid = actuatorid;
        uuid = UUID.randomUUID().toString();
    }

    public Command(String command, int shieldid, int actuatorid) {
        this.command = command;
        this.shieldid = shieldid;
        this.actuatorid = actuatorid;
        uuid = UUID.randomUUID().toString();
    }

    public Command(JSONObject json) throws Exception {
        fromJson(json);
        uuid = UUID.randomUUID().toString();
    }

    public void fromJson(JSONObject json) throws Exception {
    }

    public JSONObject toJSON() {
        return null;
    }

    // spedisce un comando e attende la risposta per il tempo definito in timeout
    public boolean send() {

        int timeout = 15000; // 10 secondi in millisecondi
                        // questo valore n on serve a nulla. Vedi anche
                        // il timeout della classe CommandThread. C'è un doppio timeout, questo dovrebbe esser più
                        // grande e non scattare mai

        commandThread = new CommandThread(this);
        Thread thread = new Thread(commandThread, "commandThread" + uuid);
        thread.start();

        // il thread esegue la chiamata alla shield webduino ed aspetta una risposta (join) dal thead per x secondi
        try {
            thread.join(timeout); // timeout è il timeout di attesa fine thread in millisecondi
        } catch (InterruptedException e) {

            e.printStackTrace();
        }

        if (commandThread.commandSuccess == false)
            LOGGER.info("TIMEOUT - Command response not received: " + uuid);

        commandThread.diconnect();
        commandThread.execute = false;

        this.success = commandThread.commandSuccess;
        this.result = commandThread.commandResult;

        if (commandDataLog != null) {

            commandDataLog.writelog("send", this);
        }
        return commandThread.commandSuccess;
    }

    private boolean messageReceived(String message) {
        LOGGER.info("Command response received: " + uuid);
        return processResponseReceived(message);
    }

    public boolean processResponseReceived(String message) {
        return true;
    }

    public class CommandThread implements Runnable/*, Core.CoreListener*/ {

        public Command command;
        private volatile boolean execute; // variabile di sincronizzazione
        public String commandResult = "";
        public boolean commandSuccess = false;
        private String responseTopic = "";//"toServer/response/#";
        private SimpleMqttClient smc;

        public CommandThread(Command command) {
            this.command = command;
            responseTopic = "toServer/response/" + command.uuid + "/#";
        }

        @Override
        public void run() {


            Shield shield = Core.getShieldFromId(command.shieldid);
            if (shield == null)
                return;

            Thread t = Thread.currentThread();
            LOGGER.info("Thread started: " + t.getName());

            smc = new SimpleMqttClient(command.uuid);
            if (!smc.runClient())
                return;
            smc.subscribe(responseTopic);
            smc.addListener(new SimpleMqttClient.SimpleMqttClientListener() {
                @Override
                public synchronized void messageReceived(String topic, String message) {

                    Command.this.messageReceived(message);
                    commandSuccess = true;
                    commandResult = message;
                    execute = false; // ferma il thread di attesa
                }

                @Override
                public void connectionLost() {

                }
            });

            String topic = "fromServer/shield/" + shield.MACAddress + "/command";
            smc.publish(topic,command.toJSON().toString());
            //Core.postCommand(command);

            // il thread si mette in loop infinito addormentandosi 500 millisec ad ogni giro
            // in attesa di ricevere risposta
            // Il loop finisce quando arriva la risposta (this.execute = false) oppure
            // quanto finisce il timeout della trhead.join in Command.send()

            SensorBase sensor = Core.getSensorFromId(command.actuatorid);
            String sensorStr = "";
            if (sensor != null) {
                sensorStr += "sensor" + sensor.getId() + ". " + sensor.getName();
            }


            this.execute = true;
            while (this.execute) {
                LOGGER.info("COMMAND SENT shield:" + shield.id + " actuator: " + sensorStr + " command" + command.command + " Waiting for response: " + command.uuid);
                try {
                    Thread.sleep((long) 1000);
                } catch (InterruptedException e) {
                    this.execute = false;
                }
            }
            diconnect();
        }

        private void diconnect() {

            if (smc.isConnected()) {
                smc.unsubscribe(responseTopic);
                smc.disconnect();
            }
        }

        public boolean getResult() {
            return commandSuccess;
        }
    }
}


