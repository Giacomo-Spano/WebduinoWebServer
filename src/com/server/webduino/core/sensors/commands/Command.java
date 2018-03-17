package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.Core;
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
        Thread thread = new Thread(commandThread, "commandThread");
        thread.start();

        // il thread esegue la chiamata alla shield webduino ed aspetta una risposta (join) dal thead per x secondi
        try {
            thread.join(timeout); // timeout è il timeout di attesa fine thread in millisecondi
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (commandDataLog != null) {
            commandDataLog.success = commandThread.commandSuccess;
            commandDataLog.result = commandThread.commandResult;
            commandDataLog.writelog("send", this);
        }
        return commandThread.commandSuccess;
    }


    public class CommandThread implements Runnable/*, Core.CoreListener*/ {

        public Command command;
        private volatile boolean execute; // variabile di sincronizzazione
        public String commandResult = "";
        public boolean commandSuccess = false;

        public CommandThread(Command command/*int shieldid, String command*/) {
            this.command = command;
        }

        @Override
        public void run() {
            Thread t = Thread.currentThread();
            LOGGER.info("Thread started: " + t.getName());
            //System.out.println("Thread started: " + t.getName());

            SimpleMqttClient smc = new SimpleMqttClient(command.uuid);
            smc.runClient();
            String topic = "toServer/response/#";
            smc.subscribe(topic);
            smc.addListener(new SimpleMqttClient.SimpleMqttClientListener() {
                @Override
                public synchronized void messageReceived(String topic, String message) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(message);
                        SensorBase sensor = Core.getSensorFromId(command.actuatorid);
                        if (sensor != null) {
                            sensor.updating = false;
                            sensor.updateFromJson(Core.getDate(), jsonObject);
                            execute = false; // ferma il thread di attesa
                            commandSuccess = true;
                            commandResult = message;
                        }
                    } catch (JSONException e) {
                        commandSuccess = false;
                        commandResult = e.toString();
                        e.printStackTrace();
                    }

                    smc.unsubscribe(topic);

                }
            });
            Core.postCommand(command);

            // il thread si mette in attesa di aggiornamento per 10 secondi e poi esce
            this.execute = true;
            while (this.execute) {
                try {
                    //checkStatusUpdate();
                    Thread.sleep((long) 1000);
                } catch (InterruptedException e) {
                    commandSuccess = false;
                    commandResult = "timeout";
                    this.execute = false;
                }
            }



            smc.unsubscribe(topic);

        }

        public boolean getResult() {
            return commandSuccess;
        }


    }

}


