package com.server.webduino.core;

import com.server.webduino.DBObject;
import com.server.webduino.core.datalog.ShieldDataLog;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.SensorFactory;
import com.server.webduino.core.sensors.commands.ShieldCommand;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sun.management.Sensor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;


public class VirtualShield extends Shield {

    private static Logger LOGGER = Logger.getLogger(VirtualShield.class.getName());

    /*public VirtualShield() {
        datalog = new ShieldDataLog(id);
    }

    public VirtualShield(JSONObject json) throws Exception {
        fromJson(json);
    }*/


    public boolean checkHealth() { //

        /*LOGGER.info("checkHealth:");
        JSONObject json = new JSONObject();
        try {
            json.put("shieldid", id);
            json.put("command", ShieldCommand.Command_CheckHealth);
            ShieldCommand cmd = new ShieldCommand(json);
            return cmd.send();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;*/
        //ping();
        //return super.checkHealth();
        for (SensorBase sensor: sensors) {
            sensor.requestStatusUpdate();
        }
        return true;
    }



}
