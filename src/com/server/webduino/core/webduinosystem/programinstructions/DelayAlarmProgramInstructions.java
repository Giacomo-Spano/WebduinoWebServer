package com.server.webduino.core.webduinosystem.programinstructions;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.WebduinoTrigger;
import com.server.webduino.core.webduinosystem.zones.Zone;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by giaco on 17/05/2017.
 */
public class DelayAlarmProgramInstructions extends ProgramInstructions {
    public DelayAlarmProgramInstructions(int id, String name, String type, int actuatorid, double targetValue, int zoneId, int seconds) {
        super(id, name, type, actuatorid, targetValue, zoneId, seconds);

        Zone zone = Core.getZoneFromId(zoneId);
        zone.addListener(this);
    }

    @Override
    public void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus) {
        Timer timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                // Your database code here
                Core.sendPushNotification("porta", "aperta", "porta soggiorno aperta", "0", 5);
            }
        }, 5*1000);

    }
}
