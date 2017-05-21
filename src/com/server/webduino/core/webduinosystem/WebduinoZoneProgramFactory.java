package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.webduinosystem.ZoneProgram;
import com.server.webduino.core.webduinosystem.security.DelayedZoneProgram;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoZoneProgramFactory {

    public ZoneProgram createZoneProgram(int id, String name, String type, int seconds) {
        ZoneProgram zone = null;
        if (type.equals("delayed")) {
            zone = new DelayedZoneProgram(id,name,type,seconds);
        }
        return zone;
    }
}
