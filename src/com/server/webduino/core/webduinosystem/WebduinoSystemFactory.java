package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.webduinosystem.heater.HeaterSystem;
import com.server.webduino.core.webduinosystem.security.DelayedZoneProgram;
import com.server.webduino.core.webduinosystem.security.SecuritySystem;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystemFactory {

    public WebduinoSystem createWebduinoSystem(int id, String name, String type) {
        WebduinoSystem system = null;
        if (type.equals("securitysystem")) {
            system = new SecuritySystem(id,name);
        } else if (type.equals("heatersystem")) {
            system = new HeaterSystem(id,name);
        }
        if (system != null) {
            system.init(id);
        }
        return system;
    }
}
