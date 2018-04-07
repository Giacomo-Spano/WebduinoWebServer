package com.server.webduino.core.webduinosystem.services;

import com.server.webduino.core.webduinosystem.scenario.actions.ActionCommand;

/**
 * Created by giaco on 10/03/2018.
 */
public class VoipService extends Service {
    public VoipService(int id, String name, String type) {
        super(id, name, type);
        ActionCommand cmd = new ActionCommand("voipcall","Chiamata VoIP");
        cmd.addParam("Numero telefono",10);
        actionCommandList.add(cmd);
    }
}
