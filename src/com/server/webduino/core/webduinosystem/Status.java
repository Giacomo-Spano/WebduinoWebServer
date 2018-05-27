package com.server.webduino.core.webduinosystem;

/**
 * Created by giaco on 20/05/2018.
 */
public class Status {
    public static final String STATUS_DISABLED = "disabled";
    public static final String STATUS_OFFLINE = "offline";
    public static final String STATUS_IDLE = "idle";

    public static final String STATUS_DESCRIPTION_DISABLED = "Disabilitato";
    public static final String STATUS_DESCRIPTION_OFFLINE = "Offline";
    public static final String STATUS_DESCRIPTION_IDLE = "Idle";

    public String status;
    public String description;

    public Status(String status, String description) {
        this.status = status;
        this.description = description;

    }
}
