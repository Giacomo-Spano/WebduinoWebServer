package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.Core;
import com.server.webduino.core.datalog.CommandDataLog;
import com.server.webduino.core.datalog.HeaterCommandDataLog;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
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
    public CommandResult result;
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

        /*CommandThread */commandThread = new CommandThread(this);
        Thread thread = new Thread(commandThread, "commandThread");
        thread.start();

        // il thread esegue la chiamata alla shield webduino ed aspetta una risposta (join) dal thead per x secondi
        try {
            thread.join(timeout); // timeout è il timeout di attesa fine thread in millisecondi
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // recupera il risulktato della chiamamata che a questo punto è disponibile
        // forse bisiognerebbe metttere syncronized
        /*String json = commandThread.getResultJson();
        result = new CommandResult();
        if (json == null) {
            result.success = false;
            result.result = "timeout";
        } else {
            result.success = true;
            result.result = json;
        }*/
        //HeaterCommandDataLog dl = new HeaterCommandDataLog();
        if (commandDataLog != null)
            commandDataLog.writelog("send",this);
        //return result;
        String json = commandThread.getResultJson();
        if (json == null)
            return false;
        else
            return true;
    }

    public String getResult() {
        return commandThread.getResultJson();
    }

    class CommandThread implements Runnable, Core.CoreListener {

        private Command command;
        private volatile boolean execute; // variabile di sincronizzazione
        private String resultJson = null;
        String jsonResult = null;

        public CommandThread(Command command/*int shieldid, String command*/) {
            this.command = command;

        }

        @Override
        public void run() {
            Thread t = Thread.currentThread();
            System.out.println("Thread started: " + t.getName());

            Core.addListener(this);
            Core.postCommand(command);
            // il thread si mette in attesa di aggiornamento
            // AGGIUNGERE TIMEOU
            this.execute = true;
            while (this.execute) {
                try {
                    //checkStatusUpdate();
                    Thread.sleep((long) 1000);
                } catch (InterruptedException e) {
                    this.execute = false;
                }
            }
            // aggiornamento ricevuto
            Core.removeListener(this);
        }

        public String getResultJson() {
            return jsonResult;
        }

        @Override
        public void onCommandResponse(String uuid, String response) {
            if (uuid.equals(command.uuid))
                jsonResult = response;
            this.execute = false;
        }
    }

    public class CommandResult {
        public boolean success;
        public String result;
    }
}


