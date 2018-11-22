package com.server.webduino.core.webduinosystem.services;

import com.server.webduino.core.Core;
import com.server.webduino.core.Device;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.servlet.SendPushMessages;
//import com.sun.org.apache.xpath.internal.operations.String;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 10/03/2018.
 */
public class AlarmNotificationService extends Service {
    private static final Logger LOGGER = Logger.getLogger(AlarmNotificationService.class.getName());
    public AlarmNotificationService(int id, String name, String type, String param) {
        super(id, name, type, param);
        ActionCommand cmd = new ActionCommand("alarmnotification","Notifica Allarme");
        cmd.addDevice("Device");
        cmd.addParam("Alarm description", 50);
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    return sendNotification(json);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            @Override
            public void end() {

            }
        });
        actionCommandList.add(cmd);
    }

    boolean sendNotification(JSONObject json) {
        Core.sendPushNotification(json);
        return true;
    }
}
