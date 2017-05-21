package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.webduinosystem.heater.HeaterExit;
import com.server.webduino.core.webduinosystem.heater.HeaterSystem;
import com.server.webduino.core.webduinosystem.security.SecurityExit;
import com.server.webduino.core.webduinosystem.security.SecuritySystem;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoExitFactory {

    public WebduinoExit createWebduinoExit(int id, String name, String type, int actuatorid) {
        WebduinoExit webduinoExit = null;
        if (type.equals("notification")) {
            webduinoExit = new SecurityExit(id,type,actuatorid,name);
        } else if (type.equals("actuator")) {
            webduinoExit = new HeaterExit(id,type,actuatorid,name);
        }
        if (webduinoExit != null) {
            webduinoExit.init(id);
        }
        return webduinoExit;
    }


}
