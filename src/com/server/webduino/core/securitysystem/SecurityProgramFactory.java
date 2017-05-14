package com.server.webduino.core.securitysystem;

/**
 * Created by giaco on 12/05/2017.
 */
public class SecurityProgramFactory {

    SecurityZoneProgram createSecurityZone(int id, String name, String type, int seconds) {
        SecurityZoneProgram zone = null;
        if (type.equals("delayed")) {
            zone = new DelayedSecurityZoneProgram(id,name,type,seconds);
        }
        return zone;
    }
}
