package com.server.webduino.core.webduinosystem;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystemFactory {

    public WebduinoSystem createWebduinoSystem(int id, String name, String type) {
        WebduinoSystem system = null;
        if (type.equals("securitysystem")) {
            system = new SecuritySystem(id,name,type);
        } else if (type.equals("heatersystem")) {
            system = new HeaterSystem(id,name,type);
        }
        if (system != null) {
            system.init(id);
        }
        return system;
    }
}
