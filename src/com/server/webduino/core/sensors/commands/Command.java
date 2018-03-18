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
    }

    public Command(String command, int shieldid, int actuatorid) {
        this.command = command;
        this.shieldid = shieldid;
        this.actuatorid = actuatorid;
        uuid = UUID.randomUUID().toString();
    }

    public Command(JSONObject json) throws JSONException {
        fromJson(json);
        uuid = UUID.randomUUID().toString();
    }

    public void fromJson(JSONObject json) throws JSONException {
    }

    public JSONObject getJSON() {
        return null;
    }

    // spedisce un comando e attende la risposta per il tempo definito in timeout
    public boolean send() {

        int timeout = 10000; // 10 secondi in millisecondi

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

        if (commandDataLog != null) {
            commandDataLog.success = commandThread.commandSuccess;
            commandDataLog.result = commandThread.commandResult;
            commandDataLog.writelog("send", this);
        }
        return commandThread.commandSuccess;
    }

    private /*synchronized */boolean messageReceived(String message) {
        LOGGER.info("Command response received: " + uuid);
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(message);
            if (jsonObject.has("sensorid")) {
                int sensorid = jsonObject.getInt("sensorid");
                SensorBase sensor = Core.getSensorFromId(sensorid);
                if (sensor != null) {
                    sensor.updating = false;
                    sensor.updateFromJson(Core.getDate(), jsonObject);
                    return true;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
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
            smc.runClient();
            smc.subscribe(responseTopic);
            smc.addListener(new SimpleMqttClient.SimpleMqttClientListener() {
                @Override
                public synchronized void messageReceived(String topic, String message) {

                    Command.this.messageReceived(message);
                    commandSuccess = true;
                    commandResult = message;
                    execute = false; // ferma il thread di attesa
                }
            });

            String topic = "fromServer/shield/" + shield.MACAddress + "/command";
            smc.publish(topic,command.getJSON().toString());
            //Core.postCommand(command);

            // il thread si mette in attesa di aggiornamento per 10 secondi e poi esce
            this.execute = true;
            while (this.execute) {
                try {
                    LOGGER.info("COMMAND SENT. Waiting for response: " + command.uuid);
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


