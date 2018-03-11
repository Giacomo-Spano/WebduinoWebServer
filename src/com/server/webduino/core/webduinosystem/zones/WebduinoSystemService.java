package com.server.webduino.core.webduinosystem.zones;

import com.server.webduino.core.Devices;
import com.server.webduino.core.webduinosystem.services.Service;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.logging.Logger;

/**
 * Created by giaco on 12/05/2017.
 */
public class WebduinoSystemService {
    private static final Logger LOGGER = Logger.getLogger(Devices.class.getName());

    private int id;
    private String name;
    private Service service;

    public WebduinoSystemService(int id, String name, Service service) {
        this.id = id;
        this.name = name;
        this.service = service;
    }
    public JSONObject toJson() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", name);
        json.put("serviceid", service.getId());
        return json;
    }
}
