package com.server.webduino.core.webduinosystem.exits;

import com.server.webduino.core.webduinosystem.exits.HeaterExit;
import com.server.webduino.core.webduinosystem.exits.SecurityExit;
import com.server.webduino.core.webduinosystem.exits.Exit;

/**
 * Created by giaco on 12/05/2017.
 */
public class ExitFactory {

    public Exit createWebduinoExit(int id, String name, String type, int actuatorid) {
        Exit exit = null;
        if (type.equals("notification")) {
            exit = new SecurityExit(id,type,actuatorid,name);
        } else if (type.equals("actuator")) {
            exit = new HeaterExit(id,type,actuatorid,name);
        }
        if (exit != null) {
            exit.init(id);
        }
        return exit;
    }


}
