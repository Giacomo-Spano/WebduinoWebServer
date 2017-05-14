package com.server.webduino.core.securitysystem;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by giaco on 12/05/2017.
 */
public class DelayedSecurityZoneProgram extends SecurityZoneProgram {


    public DelayedSecurityZoneProgram(int id, String name, String type,int seconds) {
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
            }
        }, 5*1000);
    }
}
