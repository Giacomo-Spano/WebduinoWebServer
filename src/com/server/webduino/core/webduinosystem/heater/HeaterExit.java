package com.server.webduino.core.webduinosystem.heater;

import com.server.webduino.core.webduinosystem.WebduinoExit;

/**
 * Created by giaco on 12/05/2017.
 */
public class HeaterExit extends WebduinoExit {
    int id;
    String type;
    int actuatorid;
    String name;

    public HeaterExit(int id, String type, int actuatorid, String name) {
        this.id = id;
        this.type = type;
        this.actuatorid = actuatorid;
        this.name = name;
    }

    public void init(int id) {

    }
}
