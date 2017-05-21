package com.server.webduino.core.webduinosystem.security;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.ZoneProgram;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by giaco on 12/05/2017.
 */
public class DelayedZoneProgram extends ZoneProgram {


    public DelayedZoneProgram(int id, String name, String type, int seconds) {
        super(id,name,type,seconds);
    }

    @Override
    public void changeDoorStatus(int sensorId, boolean open) {

    }

    @Override
    public void triggerAlarm() {
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
