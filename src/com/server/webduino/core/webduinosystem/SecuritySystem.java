package com.server.webduino.core.webduinosystem;

import com.server.webduino.core.webduinosystem.WebduinoSystem;
import com.server.webduino.core.Core;
import com.server.webduino.core.Devices;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class SecuritySystem extends com.server.webduino.core.webduinosystem.WebduinoSystem {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    public SecuritySystem(int id, String name) {
        super(id, name);
    }

}
