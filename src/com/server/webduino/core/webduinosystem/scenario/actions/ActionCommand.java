package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.SampleAsyncCallBack;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by giaco on 02/04/2018.
 */
public class ActionCommand {
    public String command;
    public String name;

    public String result;

    public static final String ACTIONCOMMAND_STATUSUPDATE = "statusupdate";
    public static final String ACTIONCOMMAND_STATUSUPDATE_DESCRIPTION = "Aggiorna";

    public static final String ACTIONCOMMAND_ENABLE = "enable";
    public static final String ACTIONCOMMAND_ENABLE_DESCRIPTION = "Abilita";
    public static final String ACTIONCOMMAND_DISABLE = "disable";
    public static final String ACTIONCOMMAND_DISABLE_DESCRIPTION = "Disabilita";
    public static final String ACTIONCOMMAND_PAUSE = "pause";
    public static final String ACTIONCOMMAND_PAUSE_DESCRIPTION = "Pause";
    public static final String ACTIONCOMMAND_MANUAL = "manual";
    public static final String ACTIONCOMMAND_MANUAL_DESCRIPTION = "Modalità manuale";
    public static final String ACTIONCOMMAND_AUTO = "auto";
    public static final String ACTIONCOMMAND_AUTO_DESCRIPTION = "Modalità automatica";

    public static final String ACTIONCOMMAND_KEEPTEMPERATURE = "keeptemperature";
    public static final String ACTIONCOMMAND_KEEPTEMPERATURE_DESCRIPTION = "Mantieni temperatura";
    public static final String ACTIONCOMMAND_STOP_KEEPTEMPERATURE = "stopkeeptemperature";
    public static final String ACTIONCOMMAND_STOP_KEEPTEMPERATURE_DESCRIPTION = "Fine Mantieni temperatura";
    public static final String ACTIONCOMMAND_SWITCHON = "switchon";
    public static final String ACTIONCOMMAND_SWITCHON_DESCRIPTION = "Accendi";
    public static final String ACTIONCOMMAND_SWITCHOFF = "switchoff";
    public static final String ACTIONCOMMAND_SWITCHOFF_DESCRIPTION = "Spengi";
    public static final String ACTIONCOMMAND_OUT_OF_HOME = "outofhome";
    public static final String ACTIONCOMMAND_OUT_OF_HOME_DESCRIPTION = "out of home";

    public interface Command
    {
        public boolean execute(JSONObject json);
        public void end();
        JSONObject getResult();
    }

    public Command commandMethod;

    boolean hastarget = false;
    double mintargetvalue = 0.0;
    double maxtargetvalue = 30.0;
    String targetunit = "unità";
    String targetname = "Target";
    String targetstep = "0.1";
    String targetplaceholder = "0.1";


    boolean haszone = false;
    String zonename = "Target";
    String zonesensortype = "";

    boolean hasparam = false;
    String paramname = "paramname";
    double paramlen = 100;

    boolean hasstatus = false;
    String statusname = "stato";

    boolean hasduration = false;
    int duration = 0;
    String durationname = "Durata";

    boolean hasdevice = false;
    String devicename = "stato";

    boolean hasmediaplayer = false;
    String mediaplayername = "stato";


    public ActionCommand(String command, String name) {
        this.command = command;
        this.name = name;
    }

    public void addCommand(Command command) {
        //command.execute(data);
        commandMethod = command;
    }

    /*public void callCommand(JSONObject json) {
        commandMethod.execute(json);
    }*/

    public void addTarget(String name, int min, int max, String unit) {
        hastarget = true;
        targetname = name;
        mintargetvalue = min;
        maxtargetvalue = max;
        haszone = false;
        hasparam = false;
        hasstatus = false;
        targetunit = unit;
    }
    public void addZone(String name, String zoneSensorType) {
        haszone = true;
        zonename = name;
        zonesensortype = zoneSensorType;
    }
    public void addParam(String name, int len) {
        hasparam = true;
        paramname = name;
        paramlen = len;
    }

    public void addDuration(String name) {
        hasduration = true;
        //duration = seconds;
        durationname = name;
    }

    public void addDevice(String name) {
        hasdevice = true;
        devicename = name;
    }

    public void addMediaplayer(String name) {
        hasmediaplayer = true;
        mediaplayername = name;
    }

    public void addStatus(String name) {
        hasstatus = true;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("command", command);
        json.put("name", name);

        if (hastarget) {
            json.put("target",true);
            json.put("mintargetvalue",mintargetvalue);
            json.put("maxtargetvalue",maxtargetvalue);
            json.put("targetname",targetname);
            json.put("targetunit",targetunit);
            json.put("targetstep",targetstep);
            json.put("targetplaceholder",targetplaceholder);
        }

        if (haszone) {
            json.put("zone",true);
            json.put("zonename",zonename);
            json.put("zonesensortype",zonesensortype);
        }

        if (hasduration) {
            json.put("duration",true);
            json.put("durationname",durationname);
        }


        if (hasparam) {
            json.put("param",true);
            json.put("paramname",paramname);
            json.put("paramlen",paramlen);
        }

        if (hasdevice) {
            json.put("device",true);
            json.put("devicename",devicename);
        }

        if (hasmediaplayer) {
            json.put("mediaplayer",true);
            json.put("mediaplayername",mediaplayername);
        }

        if (hasstatus) {
            json.put("status",true);
            json.put("statusname",statusname);
        }


        return json;
    }


}
