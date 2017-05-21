package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.webduinosystem.WebduinoSystem;
import com.server.webduino.core.webduinosystem.heater.HeaterZone;
import com.server.webduino.core.webduinosystem.security.SecuritySystem;
import com.server.webduino.core.webduinosystem.security.SecurityZone;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoZoneFactory {

    public WebduinoZone createWebduinoZone(int id, String name, String type) {
        WebduinoZone zone = null;
        if (type.equals("securityzone")) {
            zone = new SecurityZone(id,name);
        } else if (type.equals("heaterzone")) {
            zone = new HeaterZone(id,name);
        }
        if (zone != null) {
            zone.init(id);
        }
        return zone;
    }
}
