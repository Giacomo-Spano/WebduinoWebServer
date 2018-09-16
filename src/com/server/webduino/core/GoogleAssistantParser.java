package com.server.webduino.core;

import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.sensors.commands.IRActuatorCommand;
import com.server.webduino.core.webduinosystem.zones.Zone;
import com.server.webduino.core.webduinosystem.zones.ZoneSensor;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by giaco on 17/08/2018.
 */
public class GoogleAssistantParser {

    static public int currentzoneid = 0 ;


    public IRDevice IRDeviceFromNameAndZone(String devicename, String zonename) {
        Zone zone = Core.getZoneFromName(zonename);
        if (zone != null) {
            try {
                IRDeviceFactory factory = new IRDeviceFactory();
                IRDevice irdevice = factory.createIRDevice(devicename,zone.id);
                //IRDevice irdevice = new IRDevice(devicename,zone.id);
                return irdevice;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }

    public boolean changeChannel(IRDevice irdevice, String channel) {
        JSONObject json = new JSONObject();
        try {
            json.put("command", "changechannel");
            json.put("channel", channel);
            boolean res = irdevice.sendCommand(json);
            return res;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return false;
    }





    public boolean parseZone(Core core, String zonename) {

        zonename = zonename.toUpperCase();

        for (Zone zone : core.getZones()) {
                if (zonename.contains(zone.getName().toUpperCase())) {
                    currentzoneid = zone.getId();
                    return true;
                }
            }
        return false;
    }

    public boolean parseCommand(Core core, String command) {

        //if (device.equals("tv")) {

        command = command.toUpperCase();
        String str = command.toUpperCase();

        /*if (str.contains("SONO IN") || str.contains("SONO NELLO") || str.contains("SONO NELLA") || str.contains("IN")) {

            List<Zone> zones = core.getZones();
            for (Zone zone: zones) {
                if (str.contains(zone.getName().toUpperCase())) {
                    currentzoneid = zone.getId();
                }
            }


        } else */if (str.contains("TV") || str.contains("CAMBIA CANALE")) {
            str = command.replace("TV", "");
            str = str.replace("CAMBIA CANALE", "");
            //String[] list = str.split(" ");
            str = str.trim();

            try {
                IRCommand ircommand = new IRCommand(str);

                IRDevice irdevice = new IRDevice("televisione", currentzoneid);
                if (irdevice != null) {
                    Zone zone = core.getZoneFromId(currentzoneid);
                    if (zone != null) {
                        ZoneSensor zoneSensor = zone.zoneSensorFromId(irdevice.zonesensorid);
                        if (zoneSensor != null) {
                            SensorBase sensor = core.getSensorFromId(zoneSensor.getSensorId());
                            if (sensor != null) {
                                IRActuatorCommand cmd = new IRActuatorCommand("send", sensor.getShieldId(), sensor.getId(), ircommand.irsequence);
                                cmd.send();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (str.contains("VOLUME")) {
            if (str.contains("AUMENTA") || str.contains("SU") || str.contains("PIU")) {
                str = command.replace("AUMENTA", "");
                str = command.replace("su", "");
                str = command.replace("piu", "");
                int n = 1;
                if (str.contains("due")) n = 2;
                if (str.contains("tre")) n = 3;
                if (str.contains("quattro")) n = 4;
                if (str.contains("cinque")) n = 5;
            }
            if (str.contains("diminuisci") || str.contains("giu") || str.contains("meno")) {
                str = command.replace("diminuisci", "");
                str = command.replace("giu", "");
                str = command.replace("meno", "");
                int n = 1;
                if (str.contains("due")) n = 2;
                if (str.contains("tre")) n = 3;
                if (str.contains("quattro")) n = 4;
                if (str.contains("cinque")) n = 5;
            }
        } else if (str.contains("accendi") || str.contains("on")) {
            ;
        } else if (str.contains("spengi") || str.contains("spegni") || str.contains("off")) {
            ;
        }

        //}
        return true;
    }
}
