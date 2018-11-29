package com.server.webduino.core;

import com.server.webduino.servlet.SendPushMessages;
import org.json.JSONException;
import org.json.JSONObject;

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
    List<Device> devices;
    protected JSONObject json = null;

    public PushNotificationThread(String type, String title, String description, String value, int id, List<Device> devices) {
        super("str");
        this.type = type;
        this.title = title;
        this.description = description;
        this.value = value;
        this.id = id;
        this.devices = devices;
    }

    public PushNotificationThread(JSONObject json) {
        super("str");
        this.json = json;
        /*try {
            if (json.has("type"))
                this.type = json.getString("type");
            if (json.has("title"))
                this.title = json.getString("title");;
            if (json.has("description"))
                this.description = json.getString("description");
            if (json.has("value"))
                this.value = json.getString("value");
            if (json.has("id"))
                this.id = json.getInt("id");
            this.devices = devices;
        } catch (JSONException e) {
            e.printStackTrace();
        }*/
    }

    public void run() {

        LOGGER.info("+PushNotificationThread type=" + type + " title=" + title + " value=" + value);
        LOGGER.info("json=" + json.toString());
        SendNotification notification = new SendNotification();
        if (json != null)
            notification.send(json);
        else
            notification.send(title, description + " type=" + type + ",value=" + value, type, id, devices);
        LOGGER.info("-PushNotificationThread type=" + type + "title=" + title + "value=" + value);
    }
}

