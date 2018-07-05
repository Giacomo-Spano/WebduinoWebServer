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

    public interface Command
    {
        public boolean execute(JSONObject json);
        public void end();
    }

    public Command commandMethod;

    boolean hastarget = false;
    double mintargetvalue = 0.0;
    double maxtargetvalue = 30.0;
    String targetname = "Target";

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

    public void addTarget(String name, int min, int max) {
        hastarget = true;
        targetname = name;
        mintargetvalue = min;
        maxtargetvalue = max;
        haszone = false;
        hasparam = false;
        hasstatus = false;
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
        hasduration = false;
        //duration = seconds;
        durationname = name;
    }

    public void addDevice(String name) {
        hasdevice = true;
        devicename = name;
    }

    public void addStatus(String name) {
        hasstatus = true;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("command", command);
        json.put("name", name);

        if (hastarget) {
            json.put("targetvalue",true);
            json.put("mintargetvalue",mintargetvalue);
            json.put("maxtargetvalue",maxtargetvalue);
            json.put("targetname",targetname);
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

        if (hasstatus) {
            json.put("status",true);
            json.put("statusname",statusname);
        }


        return json;
    }


}
