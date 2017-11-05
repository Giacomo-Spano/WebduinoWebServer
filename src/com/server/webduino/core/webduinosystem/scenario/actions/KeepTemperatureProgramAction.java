package com.server.webduino.core.webduinosystem.scenario.actions;

import com.server.webduino.core.Command;
import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepTemperatureProgramAction extends ProgramAction /*implements Command.CommandListener*/ {

    private double targetTemperature;
    private int duration = 1000;
    //private int scenario;
    //private int timeInterval;
    private double temperature;

    public KeepTemperatureProgramAction(int id, int programtimerangeid, String type, String name, String description, int priority, int actuatorid, double targevalue, double thresholdvalue,
                                        int zoneId, int seconds, boolean enabled) {
        super(id, programtimerangeid, type, name, description, priority, actuatorid, targevalue, thresholdvalue,
                zoneId, seconds, enabled);

        targetTemperature = targetvalue;
        Zone zone = Core.getZoneFromId(zoneId);
        if (zone != null) {
            zone.addListener(this);
        }
    }

    @Override
    public String getStatus() {
        String status;
        Zone zone = Core.getZoneFromId(zoneId);
        if (zone != null) {
            status = "Zona: (" + zoneId + ")" + zone.getName() + " Temperatura: " + zone.getTemperature() + " °C";
            status += " Target: " + targetTemperature + " °C";
        } else {
            status = " error: zone " + zone + "not found";
        }
        return status;
    }

    @Override
    public void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature) {

        temperature = newTemperature;
        if (!active) return;

        try {
            JSONObject json = new JSONObject();
            json.put("actuatorid", actuatorid);

            SensorBase sensor = Core.getSensorFromId(actuatorid);
            if (sensor != null)
                json.put("shieldid", sensor.getShieldId());
            json.put("command", HeaterActuatorCommand.Command_KeepTemperature);
            json.put("duration", duration);
            json.put("target", targetTemperature);
            json.put("actionid", id);
            SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm");
            json.put("date", df.format(Core.getDate()));
            json.put("zone", zoneId);
            json.put("temperature", temperature);

            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);
            //cmd.addListener(this);
            Core.postCommand(cmd);
            /*cmd.post(new Command.CommandListener() {
                @Override
                public void onCommandResponse(String response) {

                }
            });*/
            //String result = cmd.send();

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }

    /*@Override
    public void onCommandResponse(String response) {

    }*/
}
