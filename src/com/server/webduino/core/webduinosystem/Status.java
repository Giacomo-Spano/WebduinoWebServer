package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.SensorBase;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by giaco on 20/05/2018.
 */
public class Status {
    public static final String STATUS_ENABLED = "enabled";
    public static final String STATUS_DISABLED = "disabled";
    public static final String STATUS_OFFLINE = "offline";
    public static final String STATUS_IDLE = "idle";

    public static final String STATUS_DESCRIPTION_ENABLED = "Abilitato";
    public static final String STATUS_DESCRIPTION_DISABLED = "Disabilitato";
    public static final String STATUS_DESCRIPTION_OFFLINE = "Offline";
    public static final String STATUS_DESCRIPTION_IDLE = "Idle";

    public String status;
    public String description;

    public Status(String status, String description) {
        this.status = status;
        this.description = description;

    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("status", status);
            json.put("description", description);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }
}
