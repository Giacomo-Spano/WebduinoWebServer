package com.server.webduino.core.webduinosystem.exits;

/**
 * Created by giaco on 12/05/2017.
 */
public class SecurityExit extends Exit {
    int id;
    String type;
    int actuatorid;
    String name;

    public SecurityExit(int id, String type, int actuatorid, String name) {
        this.id = id;
        this.type = type;
        this.actuatorid = actuatorid;
        this.name = name;
    }


}
