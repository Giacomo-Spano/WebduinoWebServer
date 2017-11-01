package com.server.webduino.core.webduinosystem.scenario.programinstructions;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.zones.Zone;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by giaco on 17/05/2017.
 */
public class DelayAlarmProgramActions extends ProgramAction {

    private boolean alarmActive = false;

    public DelayAlarmProgramActions(int id, int programtimerangeid, String type, String name, String description, int priority, int actuatorid, double targevalue, double thresholdvalue,
                                    int zoneId, int seconds, boolean enabled) {
        super(id, programtimerangeid, type, name, description, priority, actuatorid, targevalue, thresholdvalue,
                zoneId, seconds, enabled);

        Zone zone = Core.getZoneFromId(zoneId);
        if (zone != null) {
            zone.addListener(this);
        }
    }

    @Override
    public void onDoorStatusChange(int zoneId, boolean openStatus, boolean oldOpenStatus) {

        if (openStatus && openStatus != oldOpenStatus) {
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    alarmActive = true;
                    Core.sendPushNotification("porta", "aperta", "porta soggiorno aperta", "0", 5);
                }
            }, 5 * 1000);
        } else if (!openStatus) {
            alarmActive = false;
        }

    }

    @Override
    public String getStatus() {
        String status;
        Zone zone = Core.getZoneFromId(zoneId);
        status = "Zona: (" + zoneId + ")" + zone.getName() + " Stato: ";
        if (zone.getDoorStatusOpen())
            status += " open";
        else
            status += " closed";
        status += " Allarm: ";
        if (alarmActive)
            status += " active";
        else
            status += " not active";
        return status;
    }
}
