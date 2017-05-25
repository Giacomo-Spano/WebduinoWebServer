package com.server.webduino.core.webduinosystem.programinstructions;

import com.server.webduino.core.webduinosystem.WebduinoTrigger;
import com.server.webduino.core.webduinosystem.zones.Zone;

/**
 * Created by giaco on 17/05/2017.
 */
public class ProgramInstructions implements Zone.WebduinoZoneListener {

    private int id;
    private String type;
    protected int actuatorid;
    private double targetValue;
    private int zoneId;
    private int seconds;

    public ProgramInstructions(int id, String type, int actuatorid, double targetValue, int zoneId, int seconds) {
        this.id = id;
        this.type = type;
        this.actuatorid = actuatorid;
        this.targetValue = targetValue;
        this.zoneId = zoneId;
        this.seconds = seconds;
    }

    @Override
    public void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature) {

    }

    @Override
    public void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus) {

    }

    public void init() {

    }
}
