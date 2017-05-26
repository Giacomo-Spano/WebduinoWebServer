package com.server.webduino.core.webduinosystem.keys;

/**
 * Created by giaco on 12/05/2017.
 */
public class Key {
    int id;
    String type;
    int actuatorid;
    String name;

    public Key(int id, String type, int actuatorid, String name) {
        this.id = id;
        this.type = type;
        this.actuatorid = actuatorid;
        this.name = name;
    }

    public void init() {

    }
}
