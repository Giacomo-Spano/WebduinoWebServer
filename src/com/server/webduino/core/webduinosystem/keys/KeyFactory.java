package com.server.webduino.core.webduinosystem.keys;

import com.server.webduino.core.webduinosystem.exits.Exit;
import com.server.webduino.core.webduinosystem.exits.HeaterExit;
import com.server.webduino.core.webduinosystem.exits.SecurityExit;

/**
 * Created by giaco on 12/05/2017.
 */
public class KeyFactory {

    public Key createWebduinoKey(int id, String name, String type, int actuatorid) {
        Key key = null;
        if (type.equals("securitykey")) {
            key = new SecurityKey(id,type,actuatorid,name);
        } /*else if (type.equals("actuator")) {
            key = new HeaterExit(id,type,actuatorid,name);
        }*/
        if (key != null) {
            key.init();
        }
        return key;
    }


}
