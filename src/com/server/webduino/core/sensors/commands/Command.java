package com.server.webduino.core.sensors.commands;

import com.server.webduino.core.Core;
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

    public interface CommandListener {
        void onCommandResponse(String response);
    }

    protected List<CommandListener> listeners = new ArrayList<CommandListener>();

    public void addListener(CommandListener toAdd) {
        Core.addListener(new commandResponse());
        listeners.add(toAdd);
    }

    private static final Logger LOGGER = Logger.getLogger(Command.class.getName());
    public String command;
    public String uuid;
    public int shieldid;
    public int actuatorid;
    public CommandResult result;

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

    public CommandResult send() { //

        CommandThread commandThread = new CommandThread(this);
        Thread thread = new Thread(commandThread, "commandThread");
        thread.start();

        // il thread esegue la chiamata alla shield webduino ed aspetta una risposta (join) dal thead per x secondi
        try {
            thread.join(15000); // 100000 è il timeout di attesa fine thread in millisecondi
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // recupera il risulktato della chiamamata che a questo punto è disponibile
        // forse bisiognerebbe metttere syncronized
        String json = commandThread.getResultJson();
        /*CommandResult*/ result = new CommandResult();
        if (json == null) {
            result.success = false;
            result.result = "timeout";
        } else {
            result.success = true;
            result.result = json;
        }
        HeaterCommandDataLog dl = new HeaterCommandDataLog();
        dl.writelog("send",this);
        return result;
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



    public void post(CommandListener listener) {
        addListener(listener);
        Core.postCommand(this);
    }

    private class commandResponse implements Core.CoreListener {

        @Override
        public void onCommandResponse(String commanduuid, String response) {

            if (uuid.equals(commanduuid)) {
                for (CommandListener listener : listeners) {
                    listener.onCommandResponse(response);
                }
                Core.removeListener(this);
            }
        }
    }
}

