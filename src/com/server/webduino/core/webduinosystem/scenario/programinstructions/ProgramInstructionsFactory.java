package com.server.webduino.core.webduinosystem.scenario.programinstructions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by giaco on 12/05/2017.
 */
public class ProgramInstructionsFactory {

    public ProgramInstructions createProgramInstructions(int id, int programtimerangeid, String name, String type, int actuatorid, double targetValue, int zoneId, int seconds, boolean schedule,
                                                         boolean sunday, boolean monday, boolean tuesday, boolean wednesday, boolean thursday, boolean friday, boolean saturday, int priority) {
        ProgramInstructions programInstructions = null;
        if (type.equals("delayalarm")) {
            programInstructions = new DelayAlarmProgramInstructions(id,programtimerangeid,name,type,actuatorid,targetValue,zoneId,seconds, schedule,
                    sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority);
        } else if (type.equals("keeptemperature")) {
            programInstructions = new KeepTemperatureProgramInstructions(id,programtimerangeid,name,type,actuatorid,targetValue,zoneId, seconds, schedule,
                    sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority);
        } else if (type.equals("keepoff")) {
            programInstructions = new KeepOffProgramInstructions(id,programtimerangeid,name,type,actuatorid,targetValue,zoneId, seconds, schedule,
                    sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority);
        } else if (type.equals("immediatealarm")||type.equals("perimetrale")||type.equals("path")||type.equals("24hours")) {
            programInstructions = new ProgramInstructions(id,programtimerangeid,name,type,actuatorid,targetValue,zoneId, seconds, schedule,
                    sunday, monday, tuesday, wednesday, thursday, friday, saturday, priority);
        }
        if (programInstructions != null) {
            programInstructions.init();
        }
        return programInstructions;
    }

    public static JSONArray getProgramIntructionTypesJSONArray() {
        JSONArray jsonArray = new JSONArray();
        JSONObject json;
        try {
            json = new JSONObject();
            json.put("instruction","delayalarm");
            json.put("description","Delayalarm");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("instruction","keeptemperature");
            json.put("description","Keeptemperature");
            jsonArray.put(json);

            json = new JSONObject();
            json.put("instruction","keepoff");
            json.put("description","Keepoff");
            jsonArray.put(json);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }
}
