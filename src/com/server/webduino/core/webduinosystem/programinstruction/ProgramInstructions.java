package com.server.webduino.core.webduinosystem.programinstruction;

import com.server.webduino.core.WebduinoTrigger;
import com.server.webduino.core.webduinosystem.WebduinoZone;

/**
 * Created by giaco on 17/05/2017.
 */
public class ProgramInstructions implements WebduinoZone.WebduinoZoneListener {

    int id;
    String type;
    int actuatorid;
    double targetValue;
    int zoneId;

    public ProgramInstructions(int id, String type, int actuatorid, double targetValue, int zoneId) {
        this.id = id;
        this.type = type;
        this.actuatorid = actuatorid;
        this.targetValue = targetValue;
        this.zoneId = zoneId;
    }

    @Override
    public void onTrigger(WebduinoTrigger trigger) {
    }

    @Override
    public void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature) {

    }

    public void init() {

    }
}
