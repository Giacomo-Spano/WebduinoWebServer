package com.server.webduino.core;

import com.server.webduino.servlet.SendPushMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Giacomo Spanò on 15/02/2016.
 */
public class PushNotificationThread extends Thread {

    private static final Logger LOGGER = Logger.getLogger(PushNotificationThread.class.getName());

    String type;
    String title;
    String description;
    String value;
    int id;
    List<Device> devices;// = new ArrayList<>();

    public PushNotificationThread(String type, String title, String description, String value, int id, List<Device> devices) {
        super("str");
        this.type = type;
        this.title = title;
        this.description = description;
        this.value = value;
        this.id = id;
        this.devices = devices;

    }
    public void run() {

        LOGGER.info("PushNotificationThread type=" + type + "title=" + title + "value=" + value);
        SendNotification notification = new SendNotification();
        notification.send(title, description + " type=" + type + ",value=" + value, type, id, devices);
        LOGGER.info("PushNotificationThread type=" + type + "title=" + title + "value=" + value);
    }
}

