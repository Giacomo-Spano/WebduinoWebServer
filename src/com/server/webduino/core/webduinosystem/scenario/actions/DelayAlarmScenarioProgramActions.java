package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.Core;
import com.server.webduino.core.webduinosystem.zones.Zone;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by giaco on 17/05/2017.
 */
public class DelayAlarmScenarioProgramActions extends ScenarioProgramInstruction {

    private boolean alarmActive = false;

    public DelayAlarmScenarioProgramActions(int id, int programtimerangeid, String type, String name, String description, int priority, int actuatorid, double targevalue, double thresholdvalue,
                                            int zoneId, int seconds, boolean enabled) {
        super(id, programtimerangeid,  name, description, priority, enabled);

        Zone zone = Core.getZoneFromId(zoneId);
        if (zone != null) {
            //zone.addListener(this);
        }
    }

    /*@Override
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

    }*/

    @Override
    public String getStatus() {
        /*String status = "";
        int zoneId;
        //Zone zone = Core.getZoneFromId(zoneId);

        if (zone != null) {
            status = "Zona: (" + zoneId + ")" + zone.getName() + " Stato: ";
            if (zone.getDoorStatusOpen())
                status += " open";
            else
                status += " closed";
        } else {
            status = " error: zone " + zone + "not found";
        }
        status += " Alarm: ";
        if (alarmActive)
            status += " active";
        else
            status += " not active";
        return status;*/
        return "";
    }
}
