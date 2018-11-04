package com.server.webduino.core.webduinosystem.services;

import com.server.webduino.core.Core;
import com.server.webduino.core.Device;
import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;
import com.server.webduino.servlet.SendPushMessages;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 10/03/2018.
 */
public class AndroidNotificationService extends Service {
    private static final Logger LOGGER = Logger.getLogger(AndroidNotificationService.class.getName());
    public AndroidNotificationService(int id, String name, String type, String param) {
        super(id, name, type, param);
        ActionCommand cmd = new ActionCommand("androidnotification","Notifica Android");
        cmd.addDevice(0,"Device");
        cmd.addParam("Notification description", 50);
        cmd.addCommand(new ActionCommand.Command() {
            @Override
            public boolean execute(JSONObject json) {
                try {
                    if (json.has("deviceid")) {
                        int deviceid = json.getInt("deviceid");
                        return sendNotification(deviceid);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return false;
            }
            @Override
            public void end() {

            }
        });
        actionCommandList.add(cmd);
    }

    boolean sendNotification(int deviceid) {

        Device device = Core.getDevicesFromId(deviceid);
        List<Device> list = new ArrayList<>();
        list.add(device);
        String description = "androidnotification";
        Core.sendPushNotification(SendPushMessages.notification_alarm, "Allarme", description, "0", 0,list);
        return true;
    }
}
