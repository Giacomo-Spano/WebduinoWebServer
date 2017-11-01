package com.server.webduino.core.webduinosystem.scenario.programinstructions;

import com.server.webduino.core.Core;
import com.server.webduino.core.sensors.commands.HeaterActuatorCommand;
import com.server.webduino.core.sensors.SensorBase;
import com.server.webduino.core.webduinosystem.zones.Zone;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by giaco on 17/05/2017.
 */
public class KeepTemperatureProgramActions extends ProgramAction {

    private double targetTemperature;
    private int duration = 1000;
    private int scenario;
    private int timeInterval;
    private double temperature;

    public KeepTemperatureProgramActions(int id, int programtimerangeid, String type, String name, String description, int priority, int actuatorid, double targevalue, double thresholdvalue,
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
        status = "Zona: (" + zoneId + ")" + zone.getName() + " Temperatura: " + zone.getTemperature() + " °C";
        status += " Target: " + targetTemperature + " °C";
        return status;
    }

    @Override
    public void onTemperatureChange(int zoneId, double newTemperature, double oldTemperature) {

        try {
            JSONObject json = new JSONObject();
            json.put("actuatorid", actuatorid);

            SensorBase sensor = Core.getSensorFromId(actuatorid);
            if (sensor != null)
                json.put("shieldid", sensor.getShieldId());
            json.put("command", HeaterActuatorCommand.Command_KeepTemperature);
            json.put("duration", duration);
            json.put("target", targetTemperature);
            json.put("scenario", scenario);
            json.put("timeinterval", timeInterval);
            json.put("zone", zoneId);
            json.put("temperature", temperature);

            HeaterActuatorCommand cmd = new HeaterActuatorCommand(json);

            Core.postCommand(cmd);

        } catch (JSONException e1) {
            e1.printStackTrace();
        }
    }
}
