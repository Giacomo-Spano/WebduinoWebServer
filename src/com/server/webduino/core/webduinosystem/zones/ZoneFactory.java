package com.server.webduino.core.webduinosystem.zones;

/**
 * Created by giaco on 12/05/2017.
 */
public class ZoneFactory {

    public Zone createWebduinoZone(int id, String name, String type) {
        Zone zone = null;
        if (type.equals("securityzone")) {
            zone = new SecurityZone(id,name,type);
        } else if (type.equals("heaterzone")) {
            zone = new HeaterZone(id,name,type);
        } else {
            zone = new Zone(id,name);
        }
        if (zone != null) {
            zone.init();
        }
        return zone;
    }
}
